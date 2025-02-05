package edu.neu.csye6225.health_checker.service;


import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.neu.csye6225.health_checker.entity.HealthCheck;
import edu.neu.csye6225.health_checker.repository.HealthCheckRepository;

@Service
public class HealthCheckService {
    private final HealthCheckRepository healthCheckRepository;
    @Autowired
    private DataSource dataSource;
    @Autowired
    public HealthCheckService(HealthCheckRepository healthCheckRepository) {
        this.healthCheckRepository = healthCheckRepository;
    }

    public void insertHealthCheck() {
        try {
            HealthCheck healthCheck = new HealthCheck();
            healthCheckRepository.save(healthCheck);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert health check record", e);
        }
    }
    public boolean isDatabaseConnected() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
