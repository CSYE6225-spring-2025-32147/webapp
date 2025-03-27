package edu.neu.csye6225.health_checker.service;

import com.timgroup.statsd.StatsDClient;
import edu.neu.csye6225.health_checker.entity.HealthCheck;
import edu.neu.csye6225.health_checker.repository.HealthCheckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final HealthCheckRepository healthCheckRepository;
    private final DataSource dbSource;
    private final StatsDClient statsDClient;

    public HealthCheckService(HealthCheckRepository healthCheckRepository, DataSource dbSource, StatsDClient statsDClient) {
        this.healthCheckRepository= healthCheckRepository;
        this.dbSource = dbSource;
        this.statsDClient = statsDClient;
    }

    /**
     * Verifies database connectivity by validating a short-lived connection.
     */
    public boolean checkDatabaseStatus() {
        try (Connection conn = dbSource.getConnection()) {
            boolean result = conn.isValid(2);
            log.info("DB connection check returned: {}", result);
            return result;
        } catch (SQLException ex) {
            log.error("Unable to validate database connection", ex);
            return false;
        }
    }

    /**
     * Persists a dummy health-check entry to the database and tracks the operation time.
     */
    public void  recordHealthEntry() {
        long startTime = System.currentTimeMillis();
        try {
            healthCheckRepository.save(new HealthCheck());
            log.info("Health record successfully saved");
        } catch (Exception ex) {
            log.error("Error during health record save", ex);
            throw new RuntimeException("Could not save health check record", ex);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            statsDClient.recordExecutionTime("db.healthcheck.insert", elapsed);
        }
    }
}