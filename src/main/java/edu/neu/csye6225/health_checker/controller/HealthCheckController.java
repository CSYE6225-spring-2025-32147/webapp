package edu.neu.csye6225.health_checker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.neu.csye6225.health_checker.service.HealthCheckService;
import java.util.Map;

@RestController
@RequestMapping("/healthz")
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @Autowired
    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    public ResponseEntity<Void> checkHealth(
            @RequestBody(required = false) String payload,
            @RequestParam Map<String, String> queryParams) {

        if (isRequestInvalid(payload, queryParams)) {
            return buildResponse(HttpStatus.BAD_REQUEST);
        }

        try {
            if (!healthCheckService.isDatabaseConnected()) {
                throw new RuntimeException("Database connection unavailable");
            }
            healthCheckService.insertHealthCheck();
            return buildResponse(HttpStatus.OK);
        } catch (Exception e) {
            return buildResponse(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
            RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.TRACE,
            RequestMethod.HEAD})
    public ResponseEntity<Void> handleMethodNotAllowed() {
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
}