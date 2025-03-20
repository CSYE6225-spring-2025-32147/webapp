package edu.neu.csye6225.health_checker.controller;

import edu.neu.csye6225.health_checker.entity.S3FileMetadata;
import edu.neu.csye6225.health_checker.repository.FileMetadataRepository;
import edu.neu.csye6225.health_checker.service.AwsS3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class AwsS3Controller {

    private final AwsS3Service cloudStorageService;
    private final FileMetadataRepository metadataRepository;

    @Value("${aws.s3.bucket-name}")
    private String storageBucket;

    public AwsS3Controller(AwsS3Service cloudStorageService, FileMetadataRepository metadataRepository) {
        this.cloudStorageService = cloudStorageService;
        this.metadataRepository = metadataRepository;
    }

    @PostMapping("/file")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("profilePic") MultipartFile document) {
        String uniqueId = UUID.randomUUID().toString();
        String documentKey = uniqueId + "/" + document.getOriginalFilename();
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String documentUrl = storageBucket + "/" + documentKey;

        try {
            // Store metadata first
            S3FileMetadata fileDetails = new S3FileMetadata();
            fileDetails.setId(uniqueId);
            fileDetails.setFileName(document.getOriginalFilename());
            fileDetails.setUrl(documentUrl);
            fileDetails.setUploadDate(timestamp);
            metadataRepository.save(fileDetails); // Ensure metadata is stored before uploading

            // Upload document to S3
            cloudStorageService.uploadFileToAwsS3(document, documentKey);

            // Response map
            Map<String, String> responsePayload = new HashMap<>();
            responsePayload.put("file_name", document.getOriginalFilename());
            responsePayload.put("id", uniqueId);
            responsePayload.put("url", documentUrl);
            responsePayload.put("upload_date", timestamp);

            return ResponseEntity.status(201).body(responsePayload);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", "File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<Map<String, String>> fetchFile(@PathVariable String fileId) {
        try {
            // Retrieve metadata from DB
            Optional<S3FileMetadata> fileDetailsOpt = metadataRepository.findById(fileId);
            if (fileDetailsOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "File not found"));
            }

            S3FileMetadata fileDetails = fileDetailsOpt.get();

            // Construct response
            Map<String, String> responsePayload = new HashMap<>();
            responsePayload.put("file_name", fileDetails.getFileName());
            responsePayload.put("id", fileDetails.getId());
            responsePayload.put("url", fileDetails.getUrl());
            responsePayload.put("upload_date", fileDetails.getUploadDate());

            return ResponseEntity.ok(responsePayload);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error retrieving file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/file/{fileId}")
    public ResponseEntity<Map<String, String>> removeFile(@PathVariable String fileId) {
        try {
            Optional<S3FileMetadata> fileDetailsOpt = metadataRepository.findById(fileId);
            if (fileDetailsOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "File not found"));
            }

            S3FileMetadata fileDetails = fileDetailsOpt.get();

            // Delete file from S3 using correct key
            boolean deletionStatus = cloudStorageService.deleteFile(fileDetails.getId() + "/" + fileDetails.getFileName());
            if (!deletionStatus) {
                return ResponseEntity.status(500).body(Map.of("error", "Error deleting file from S3"));
            }

            // Remove metadata
            metadataRepository.deleteById(fileId);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error deleting file: " + e.getMessage()));
        }
    }

    // Handle unsupported methods
    @RequestMapping(value = "/file", method = {RequestMethod.GET, RequestMethod.DELETE})
    public ResponseEntity<Map<String, String>> invalidRequestForFile() {
        return ResponseEntity.status(400).body(Map.of("error", "Bad request for /v1/file"));
    }

    @RequestMapping(value = "/file", method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.HEAD})
    public ResponseEntity<Map<String, String>> unsupportedOperationsForFile() {
        return ResponseEntity.status(405).body(Map.of("error", "Method not allowed for /v1/file"));
    }

    @RequestMapping(value = "/file/{fileId}", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS})
    public ResponseEntity<Map<String, String>> unsupportedOperationsForFileId() {
        return ResponseEntity.status(405).body(Map.of("error", "Method not allowed for /v1/file/{fileId}"));
    }
}