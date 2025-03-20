package edu.neu.csye6225.health_checker.repository;

import edu.neu.csye6225.health_checker.entity.AwsS3FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AwsS3FileMetadataRepository extends JpaRepository<AwsS3FileMetadata, String> {
}