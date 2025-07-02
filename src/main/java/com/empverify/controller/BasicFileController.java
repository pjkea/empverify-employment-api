package com.empverify.controller;

import com.empverify.dto.BlockchainResponse;
import com.empverify.dto.DocumentCollectionDto;
import com.empverify.dto.DocumentRequest;
import com.empverify.service.BasicFileService;
import com.empverify.service.DocumentExtractionHelper;
import com.empverify.service.EmploymentRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/employment-records/{employeeId}/files")
@Tag(name = "Basic File Management", description = "Simple file upload and download for employment records")
@SecurityRequirement(name = "apiKey")
public class BasicFileController {

    private static final Logger logger = LoggerFactory.getLogger(BasicFileController.class);

    private final BasicFileService fileService;
    private final EmploymentRecordService employmentRecordService;

    @Autowired
    public BasicFileController(BasicFileService fileService,
                               EmploymentRecordService employmentRecordService) {
        this.fileService = fileService;
        this.employmentRecordService = employmentRecordService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload File", description = "Upload a file and store reference on blockchain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "404", description = "Employee record not found"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    public ResponseEntity<BlockchainResponse<String>> uploadFile(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,

            @Parameter(description = "Document type (e.g., employment_contract, performance_review)")
            @RequestParam String documentType,

            @Parameter(description = "File to upload")
            @RequestParam("file") MultipartFile file) {

        logger.info("Upload request: employeeId={}, documentType={}, filename={}",
                employeeId, documentType, file.getOriginalFilename());

        try {
            // Step 1: Upload file to S3 and get document reference
            DocumentRequest documentRequest = fileService.uploadFile(employeeId, documentType, file);

            // Step 2: Store document reference on blockchain
            BlockchainResponse<String> blockchainResponse = employmentRecordService.addDocument(
                    employeeId, documentType, documentRequest);

            if (blockchainResponse.isSuccess()) {
                logger.info("File upload completed successfully: {}", file.getOriginalFilename());

                // Return success with blockchain response
                BlockchainResponse<String> response = BlockchainResponse.success(
                        "File uploaded and recorded on blockchain successfully",
                        String.format("File: %s, S3 Key: %s, Hash: %s",
                                file.getOriginalFilename(),
                                documentRequest.getS3Key(),
                                documentRequest.getFileHash())
                );

                return ResponseEntity.status(HttpStatus.CREATED).body(response);

            } else {
                logger.error("Blockchain storage failed: {}", blockchainResponse.getError());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(BlockchainResponse.error("File uploaded but blockchain recording failed: " +
                                blockchainResponse.getError()));
            }

        } catch (Exception e) {
            logger.error("File upload failed for employee: {}", employeeId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BlockchainResponse.error("File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{documentType}")
    @Operation(summary = "Download File", description = "Download a file by document type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "File or employee record not found"),
            @ApiResponse(responseCode = "500", description = "Download failed")
    })
    public ResponseEntity<ByteArrayResource> downloadFile(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Document type") @PathVariable String documentType) {

        logger.info("Download request: employeeId={}, documentType={}", employeeId, documentType);

        try {
            // Step 1: Get document references from blockchain
            BlockchainResponse<DocumentCollectionDto> documentsResponse =
                    employmentRecordService.getDocuments(employeeId);

            if (!documentsResponse.isSuccess()) {
                logger.error("Failed to get documents from blockchain: {}", documentsResponse.getError());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Step 2: Extract S3 key for the requested document type
            String s3Key = extractS3Key(documentsResponse.getData(), documentType);

            if (s3Key == null) {
                logger.warn("Document not found: employeeId={}, documentType={}", employeeId, documentType);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Step 3: Download file from S3
            byte[] fileData = fileService.downloadFile(s3Key);

            // Step 4: Return file with appropriate headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", getFilenameFromS3Key(s3Key));

            ByteArrayResource resource = new ByteArrayResource(fileData);

            logger.info("File download completed: employeeId={}, documentType={}, size={} bytes",
                    employeeId, documentType, fileData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileData.length)
                    .body(resource);

        } catch (Exception e) {
            logger.error("File download failed: employeeId={}, documentType={}", employeeId, documentType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Extract S3 key from document collection based on document type
     * This is a simple implementation - you might need to enhance based on your document structure
     */
    @Autowired
    private DocumentExtractionHelper extractionHelper;

    private String extractS3Key(DocumentCollectionDto documents, String documentType) {
        return extractionHelper.extractS3Key(documents, documentType);
    }

    /**
     * Extract filename from S3 key
     */
    private String getFilenameFromS3Key(String s3Key) {
        if (s3Key == null) {
            return "document";
        }

        int lastSlash = s3Key.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < s3Key.length() - 1) {
            return s3Key.substring(lastSlash + 1);
        }

        return s3Key;
    }
}