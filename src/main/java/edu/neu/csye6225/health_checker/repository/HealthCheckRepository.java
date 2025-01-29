package edu.neu.csye6225.health_checker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.neu.csye6225.health_checker.entity.HealthCheck;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {

}