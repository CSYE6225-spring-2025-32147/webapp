package edu.neu.csye6225.health_checker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import edu.neu.csye6225.health_checker.service.HealthCheckService;

@RestController
public class HealthCheckController {
    private final HealthCheckService healthCheckService;

    @Autowired
    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }
    @GetMapping("/healthz")
    public ResponseEntity<Void> performHealthCheck(@RequestBody(required = false) String payload) {
        if (payload != null && !payload.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)  
                    .header("Cache-Control", "no-cache")
                    .build();
        }

        try {
            healthCheckService.insertHealthCheck();  
            return ResponseEntity.status(HttpStatus.OK) 
                    .header("Cache-Control", "no-cache")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)  
                    .header("Cache-Control", "no-cache")
                    .build();
        }
    }

    
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Void> handleUnsupportedHttpRequests() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)  // 405
                .header("Cache-Control", "no-cache")
                .build();
    }

}
