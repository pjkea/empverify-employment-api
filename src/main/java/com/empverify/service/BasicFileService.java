package com.empverify.service;

import com.empverify.dto.DocumentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class BasicFileService {

    private static final Logger logger = LoggerFactory.getLogger(BasicFileService.class);

    @Value("${empverify.s3.bucket-name}")
    private String bucketName;

    @Value("${empverify.s3.access-key}")
    private String accessKey;

    @Value("${empverify.s3.secret-key}")
    private String secretKey;

    @Value("${empverify.s3.region:us-east-1}")
    private String region;

    private S3Client s3Client;

    /**
     * Initialize S3 client - lazy loading
     */
    private S3Client getS3Client() {
        if (s3Client == null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        }
        return s3Client;
    }

    /**
     * Upload file to S3 and return document reference
     */
    public DocumentRequest uploadFile(String employeeId, String documentType, MultipartFile file) throws IOException {
        logger.info("Uploading file: {} for employee: {}", file.getOriginalFilename(), employeeId);

        // Generate S3 key (file path)
        String s3Key = generateS3Key(employeeId, documentType, file.getOriginalFilename());

        // Upload to S3
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            getS3Client().putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

            logger.info("File uploaded to S3: {}", s3Key);

        } catch (Exception e) {
            logger.error("Failed to upload file to S3: {}", s3Key, e);
            throw new IOException("S3 upload failed: " + e.getMessage());
        }

        // Generate file hash
        String fileHash = generateFileHash(file.getBytes());

        // Create S3 URL
        String s3Url = String.format("s3://%s/%s", bucketName, s3Key);

        // Create document request for blockchain
        DocumentRequest documentRequest = new DocumentRequest();
        documentRequest.setS3Bucket(bucketName);
        documentRequest.setS3Key(s3Key);
        documentRequest.setS3Url(s3Url);
        documentRequest.setFileHash(fileHash);
        documentRequest.setFileSizeBytes(file.getSize());
        documentRequest.setAccessLevel("restricted"); // Default access level

        logger.info("Document reference created for blockchain storage");
        return documentRequest;
    }

    /**
     * Download file from S3
     */
    public byte[] downloadFile(String s3Key) throws IOException {
        logger.info("Downloading file from S3: {}", s3Key);

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            byte[] fileData = getS3Client().getObjectAsBytes(getRequest).asByteArray();

            logger.info("File downloaded successfully: {} ({} bytes)", s3Key, fileData.length);
            return fileData;

        } catch (Exception e) {
            logger.error("Failed to download file from S3: {}", s3Key, e);
            throw new IOException("S3 download failed: " + e.getMessage());
        }
    }

    /**
     * Generate S3 key (file path) - simple format
     */
    private String generateS3Key(String employeeId, String documentType, String originalFilename) {
        // Simple format: emp/{employeeId}/{documentType}/{timestamp}_{filename}
        String timestamp = LocalDateTime.now().toString().replace(":", "-");
        String sanitizedType = documentType.toLowerCase().replace(" ", "_");

        return String.format("emp/%s/%s/%s_%s",
                employeeId, sanitizedType, timestamp, originalFilename);
    }

    /**
     * Generate SHA-256 hash of file content
     */
    private String generateFileHash(byte[] fileBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return "sha256:" + hexString.toString();

        } catch (Exception e) {
            logger.error("Error generating file hash", e);
            return "sha256:unknown";
        }
    }
}