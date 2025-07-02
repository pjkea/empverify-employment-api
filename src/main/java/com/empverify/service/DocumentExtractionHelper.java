package com.empverify.service;

import com.empverify.dto.DocumentCollectionDto;
import com.empverify.dto.DocumentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Helper service to extract S3 keys from document collections
 * This bridges the gap between your blockchain document structure and file storage
 */
@Component
public class DocumentExtractionHelper {

    private static final Logger logger = LoggerFactory.getLogger(DocumentExtractionHelper.class);

    /**
     * Extract S3 key from document collection based on document type
     */
    public String extractS3Key(DocumentCollectionDto documents, String documentType) {
        if (documents == null || documents.getDocuments() == null) {
            logger.warn("No documents found");
            return null;
        }

        logger.debug("Extracting S3 key for document type: {}", documentType);

        try {
            var documentsDto = documents.getDocuments();

            switch (documentType.toLowerCase()) {
                case "employment_contract":
                    return getS3KeyFromDocument(documentsDto.getEmploymentContract());

                case "resignation_letter":
                    return getS3KeyFromDocument(documentsDto.getResignationLetter());

                case "termination_letter":
                    return getS3KeyFromDocument(documentsDto.getTerminationLetter());

                case "exit_interview":
                    return getS3KeyFromDocument(documentsDto.getExitInterview());

                case "id_verification":
                    return getS3KeyFromDocument(documentsDto.getIdVerification());

                case "performance_review":
                    // For performance reviews, return the first one (you might want to enhance this)
                    if (documentsDto.getPerformanceReviews() != null &&
                            !documentsDto.getPerformanceReviews().isEmpty()) {
                        return getS3KeyFromDocument(documentsDto.getPerformanceReviews().get(0));
                    }
                    return null;

                case "disciplinary_document":
                    // For disciplinary documents, return the first one
                    if (documentsDto.getDisciplinaryDocuments() != null &&
                            !documentsDto.getDisciplinaryDocuments().isEmpty()) {
                        return getS3KeyFromDocument(documentsDto.getDisciplinaryDocuments().get(0));
                    }
                    return null;

                case "custom_document":
                    // For custom documents, return the first one
                    if (documentsDto.getCustomDocuments() != null &&
                            !documentsDto.getCustomDocuments().isEmpty()) {
                        return getS3KeyFromDocument(documentsDto.getCustomDocuments().get(0));
                    }
                    return null;

                default:
                    logger.warn("Unknown document type: {}", documentType);
                    return null;
            }

        } catch (Exception e) {
            logger.error("Error extracting S3 key for document type: {}", documentType, e);
            return null;
        }
    }

    /**
     * Extract S3 key from a single document
     */
    private String getS3KeyFromDocument(DocumentDto document) {
        if (document == null) {
            return null;
        }

        String s3Key = document.getS3Key();
        if (s3Key != null && !s3Key.trim().isEmpty()) {
            logger.debug("Found S3 key: {}", s3Key);
            return s3Key;
        }

        logger.debug("No S3 key found in document");
        return null;
    }

    /**
     * Get all available document types for an employee
     */
    public java.util.List<String> getAvailableDocumentTypes(DocumentCollectionDto documents) {
        java.util.List<String> availableTypes = new java.util.ArrayList<>();

        if (documents == null || documents.getDocuments() == null) {
            return availableTypes;
        }

        var docs = documents.getDocuments();

        if (docs.getEmploymentContract() != null) availableTypes.add("employment_contract");
        if (docs.getResignationLetter() != null) availableTypes.add("resignation_letter");
        if (docs.getTerminationLetter() != null) availableTypes.add("termination_letter");
        if (docs.getExitInterview() != null) availableTypes.add("exit_interview");
        if (docs.getIdVerification() != null) availableTypes.add("id_verification");

        if (docs.getPerformanceReviews() != null && !docs.getPerformanceReviews().isEmpty()) {
            availableTypes.add("performance_review");
        }

        if (docs.getDisciplinaryDocuments() != null && !docs.getDisciplinaryDocuments().isEmpty()) {
            availableTypes.add("disciplinary_document");
        }

        if (docs.getCustomDocuments() != null && !docs.getCustomDocuments().isEmpty()) {
            availableTypes.add("custom_document");
        }

        logger.info("Available document types: {}", availableTypes);
        return availableTypes;
    }
}