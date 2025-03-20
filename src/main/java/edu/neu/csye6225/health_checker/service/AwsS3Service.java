package edu.neu.csye6225.health_checker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
public class AwsS3Service {

    private static final Logger LOG = LoggerFactory.getLogger(AwsS3Service.class);
    private final S3Client cloudClient;

    @Value("${aws.s3.bucket-name}")
    private String cloudBucket;

    public AwsS3Service(@Value("${aws.region}") String cloudRegion) {
        LOG.info("Setting up AWS S3 client with DefaultCredentialsProvider");

        this.cloudClient = S3Client.builder()
                .region(Region.of(cloudRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void uploadFileToAwsS3(MultipartFile file, String filePath) throws Exception {
        try {
            cloudClient.putObject(PutObjectRequest.builder()
                            .bucket(cloudBucket)
                            .key(filePath)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));
            LOG.info("Successfully uploaded file: {}", filePath);
        } catch (S3Exception e) {
            LOG.error("AWS S3 Upload Failed: {}", e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    public boolean deleteFile(String filePath) {
        try {
            DeleteObjectRequest removalRequest = DeleteObjectRequest.builder()
                    .bucket(cloudBucket)
                    .key(filePath)
                    .build();

            cloudClient.deleteObject(removalRequest);
            LOG.info("File removed successfully: {}", filePath);
            return true;
        } catch (S3Exception e) {
            LOG.error("AWS S3 Deletion Error: {}", e.awsErrorDetails().errorMessage());
            return false;
        }
    }
}