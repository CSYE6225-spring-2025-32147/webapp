package edu.neu.csye6225.health_checker.service;

import com.timgroup.statsd.StatsDClient;
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

    private static final Logger log = LoggerFactory.getLogger(AwsS3Service.class);

    private final S3Client s3Client;
    private final StatsDClient metricsClient;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public AwsS3Service(@Value("${aws.region}") String region, StatsDClient metricsClient) {
        this.metricsClient = metricsClient;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        log.info("S3 client configured for region: {}", region);
    }

    public void uploadFileToAwsS3(MultipartFile multipartFile, String objectKey) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            log.info("uploading file to S3 with key: {}", objectKey);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(multipartFile.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(multipartFile.getBytes()));

            log.info("File successfully uploaded to S3 with key: {}", objectKey);
        } catch (S3Exception ex) {
            log.error("Error while uploading file to S3 at key: {}", objectKey, ex);
            throw ex;
        } finally {
            metricsClient.recordExecutionTime("s3.upload.time", System.currentTimeMillis() - startTime);
        }
    }

    public boolean deleteFile(String objectKey) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("Initiating deletion of file from S3 with key: {}", objectKey);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);

            log.info("Successfully deleted object with key: {}", objectKey);
            return true;
        } catch (S3Exception ex) {
            log.error("Failed to delete the file from S3 at key: {}", objectKey, ex);
            return false;
        } finally {
            metricsClient.recordExecutionTime("s3.delete.time", System.currentTimeMillis() - startTime);
        }
    }
}