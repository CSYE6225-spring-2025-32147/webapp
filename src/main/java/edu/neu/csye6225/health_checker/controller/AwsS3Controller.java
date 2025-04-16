package edu.neu.csye6225.health_checker.controller;

import com.timgroup.statsd.StatsDClient;
import edu.neu.csye6225.health_checker.entity.AwsS3FileMetadata;
import edu.neu.csye6225.health_checker.repository.AwsS3FileMetadataRepository;
import edu.neu.csye6225.health_checker.service.AwsS3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/v2")
public class AwsS3Controller {

    private static final Logger logger = LoggerFactory.getLogger(AwsS3Controller.class);
    private final AwsS3Service cloudStorageService;
    private final AwsS3FileMetadataRepository metadataRepository;
    private final StatsDClient statsDClient;

    @Value("${aws.s3.bucket-name}")
    private String storageBucket;

    public AwsS3Controller(AwsS3Service cloudStorageService, AwsS3FileMetadataRepository metadataRepository, StatsDClient statsDClient) {
        this.cloudStorageService = cloudStorageService;
        this.metadataRepository = metadataRepository;
        this.statsDClient = statsDClient;
    }

    @PostMapping("/file")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("profilePic") MultipartFile document) {
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("api.file.upload.hit");
        logger.info("Received file upload request for: {}", document.getOriginalFilename());

        try {
            String uniqueId = UUID.randomUUID().toString();
            String documentKey = uniqueId + "/" + document.getOriginalFilename();
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String documentUrl = storageBucket + "/" + documentKey;

            AwsS3FileMetadata fileDetails = new AwsS3FileMetadata();
            fileDetails.setId(uniqueId);
            fileDetails.setFileName(document.getOriginalFilename());
            fileDetails.setUrl(documentUrl);
            fileDetails.setUploadDate(timestamp);

            long dbStart = System.currentTimeMillis();
            metadataRepository.save(fileDetails);
            statsDClient.recordExecutionTime("db.file.save", System.currentTimeMillis() - dbStart);
            logger.info("Saved file metadata to DB for ID: {}", uniqueId);

            cloudStorageService.uploadFileToAwsS3(document, documentKey);

            Map<String, String> responsePayload = new HashMap<>();
            responsePayload.put("file_name", document.getOriginalFilename());
            responsePayload.put("id", uniqueId);
            responsePayload.put("url", documentUrl);
            responsePayload.put("upload_date", timestamp);

            logger.info("File uploaded and response returned for ID: {}", uniqueId);
            return ResponseEntity.status(201).body(responsePayload);
        } catch (Exception e) {
            logger.error("File upload failed", e);
            return ResponseEntity.status(400).body(Map.of("error", "File upload failed: " + e.getMessage()));
        } finally {
            statsDClient.recordExecutionTime("api.file.upload.time", System.currentTimeMillis() - start);
        }
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<Map<String, String>> fetchFile(@PathVariable String fileId) {
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("api.file.get.hit");
        logger.info("Fetching file metadata for ID: {}", fileId);

        try {
            long dbStart = System.currentTimeMillis();
            Optional<AwsS3FileMetadata> fileDetailsOpt = metadataRepository.findById(fileId);
            statsDClient.recordExecutionTime("db.file.get", System.currentTimeMillis() - dbStart);

            if (fileDetailsOpt.isEmpty()) {
                logger.warn("File with ID {} not found", fileId);
                return ResponseEntity.status(404).body(Map.of("error", "File not found"));
            }

            AwsS3FileMetadata fileDetails = fileDetailsOpt.get();

            Map<String, String> responsePayload = new HashMap<>();
            responsePayload.put("file_name", fileDetails.getFileName());
            responsePayload.put("id", fileDetails.getId());
            responsePayload.put("url", fileDetails.getUrl());
            responsePayload.put("upload_date", fileDetails.getUploadDate());

            logger.info("Returning metadata for file ID: {}", fileId);
            return ResponseEntity.ok(responsePayload);
        } catch (Exception e) {
            logger.error("Error retrieving file", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error retrieving file: " + e.getMessage()));
        } finally {
            statsDClient.recordExecutionTime("api.file.get.time", System.currentTimeMillis() - start);
        }
    }

    @DeleteMapping("/file/{fileId}")
    public ResponseEntity<Map<String, String>> removeFile(@PathVariable String fileId) {
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("api.file.delete.hit");
        logger.info("Received delete request for file ID: {}", fileId);

        try {
            long dbStart = System.currentTimeMillis();
            Optional<AwsS3FileMetadata> fileDetailsOpt = metadataRepository.findById(fileId);
            statsDClient.recordExecutionTime("db.file.get", System.currentTimeMillis() - dbStart);

            if (fileDetailsOpt.isEmpty()) {
                logger.warn("File with ID {} not found for deletion", fileId);
                return ResponseEntity.status(404).body(Map.of("error", "File not found"));
            }

            AwsS3FileMetadata fileDetails = fileDetailsOpt.get();

            dbStart = System.currentTimeMillis();
            metadataRepository.deleteById(fileId);
            statsDClient.recordExecutionTime("db.file.delete", System.currentTimeMillis() - dbStart);
            logger.info("Deleted file metadata from DB for ID: {}", fileId);

            boolean deletionStatus = cloudStorageService.deleteFile(fileDetails.getId() + "/" + fileDetails.getFileName());
            if (!deletionStatus) {
                logger.error("Failed to delete file from S3 for ID: {}", fileId);
                return ResponseEntity.status(500).body(Map.of("error", "Error deleting file from S3"));
            }

            logger.info("Deleted file from S3 for ID: {}", fileId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting file", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error deleting file: " + e.getMessage()));
        } finally {
            statsDClient.recordExecutionTime("api.file.delete.time", System.currentTimeMillis() - start);
        }
    }

    @RequestMapping(value = "/file", method = {RequestMethod.GET, RequestMethod.DELETE})
    public ResponseEntity<Map<String, String>> invalidRequestForFile() {
        logger.warn("Invalid request method for /v1/file");
        return ResponseEntity.status(400).body(Map.of("error", "Bad request for /v1/file"));
    }

    @RequestMapping(value = "/file", method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.HEAD})
    public ResponseEntity<Map<String, String>> unsupportedOperationsForFile() {
        logger.warn("Unsupported method for /v1/file");
        return ResponseEntity.status(405).body(Map.of("error", "Method not allowed for /v1/file"));
    }

    @RequestMapping(value = "/file/{fileId}", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS})
    public ResponseEntity<Map<String, String>> unsupportedOperationsForFileId(@PathVariable String fileId) {
        logger.warn("Unsupported method for /v1/file/{}",fileId);
        return ResponseEntity.status(405).body(Map.of("error", "Method not allowed for /v1/file/{fileId}"));
    }
}