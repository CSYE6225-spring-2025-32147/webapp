package edu.neu.csye6225.health_checker.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.neu.csye6225.health_checker.entity.HealthCheck;
import edu.neu.csye6225.health_checker.repository.HealthCheckRepository;


@Service
public class HealthCheckService {
    private final HealthCheckRepository healthCheckRepository;

    @Autowired
    public HealthCheckService(HealthCheckRepository healthCheckRepository) {
        this.healthCheckRepository = healthCheckRepository;
    }

    public void insertHealthCheck() {
        try {
            HealthCheck healthCheck = new HealthCheck();  // Creates a new health check entity
            healthCheckRepository.save(healthCheck);  // Saves it to the database
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert health check record", e);
        }
    }
}
