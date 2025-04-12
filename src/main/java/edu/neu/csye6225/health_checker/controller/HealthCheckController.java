package edu.neu.csye6225.health_checker.controller;

import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.neu.csye6225.health_checker.service.HealthCheckService;

import java.util.Map;

@RestController
public class HealthCheckController {

    private final HealthCheckService healthCheckService;
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    private final StatsDClient statsDClient;

    @Autowired
    public HealthCheckController(HealthCheckService healthCheckService, StatsDClient statsDClient) {
        this.healthCheckService = healthCheckService;
        this.statsDClient = statsDClient;
    }

    @GetMapping("/")
    public ResponseEntity<String> home() {
        logger.info("Root context '/' accessed");
        statsDClient.incrementCounter("api.root.hit");

        return ResponseEntity.ok()
                .headers(getDefaultHeaders())
                .body("Welcome to our web application");
    }

    @GetMapping("/healthz")
    public ResponseEntity<Void> checkHealth(
            @RequestBody(required = false) String payload,
            @RequestParam(required = false) Map<String, String> queryParams) {

        long start = System.currentTimeMillis();

        statsDClient.incrementCounter("api.healthz.hit");
        logger.info("Received request at /healthz endpoint");

        if (isRequestInvalid(payload, queryParams)) {
            logger.warn("Request denied: unexpected request body or query parameters");
            return buildResponse(HttpStatus.BAD_REQUEST);
        }

        try {
            if (!healthCheckService.checkDatabaseStatus()) {
                logger.error("Health check failed: unable to reach database");
                throw new RuntimeException("Database connection unavailable");
            }

            healthCheckService.recordHealthEntry();
            logger.info("Health check successfully recorded in the database");
            return buildResponse(HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Health check failed", e);
            return buildResponse(HttpStatus.SERVICE_UNAVAILABLE);
        } finally {
            long duration = System.currentTimeMillis() - start;
            statsDClient.recordExecutionTime("api.healthz.time", duration);
        }
    }

    @RequestMapping(value = "/healthz", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
            RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.TRACE,
            RequestMethod.HEAD})
    public ResponseEntity<Void> handleMethodNotAllowed() {
        logger.warn("Method not allowed on /healthz");
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED);
    }

    private boolean isRequestInvalid(String payload, Map<String, String> queryParams) {
        return (payload != null && !payload.isEmpty()) || (queryParams != null && !queryParams.isEmpty());
    }

    private ResponseEntity<Void> buildResponse(HttpStatus status) {
        return ResponseEntity.status(status)
                .headers(getDefaultHeaders())
                .build();
    }

    private HttpHeaders getDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("Connection", "keep-alive");
        return headers;
    }

    @GetMapping("/cicd")
    public ResponseEntity<Void> checkCiCd(
            @RequestBody(required = false) String payload,
            @RequestParam(required = false) Map<String, String> queryParams) {

        long start = System.currentTimeMillis();

        statsDClient.incrementCounter("api.healthz.hit");
        logger.info("Received request at /healthz endpoint");

        if (isRequestInvalid(payload, queryParams)) {
            logger.warn("Request denied: unexpected request body or query parameters");
            return buildResponse(HttpStatus.BAD_REQUEST);
        }

        try {
            if (!healthCheckService.checkDatabaseStatus()) {
                logger.error("Health check failed: unable to reach database");
                throw new RuntimeException("Database connection unavailable");
            }

            healthCheckService.recordHealthEntry();
            logger.info("Health check successfully recorded in the database");
            return buildResponse(HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Health check failed", e);
            return buildResponse(HttpStatus.SERVICE_UNAVAILABLE);
        } finally {
            long duration = System.currentTimeMillis() - start;
            statsDClient.recordExecutionTime("api.healthz.time", duration);
        }
    }
}