package edu.neu.csye6225.health_checker.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.neu.csye6225.health_checker.service.HealthCheckService;
import java.util.Map;

@RestController
public class HealthCheckController {
    private final HealthCheckService healthCheckerService;

    @Autowired
    public HealthCheckController(HealthCheckService healthCheckerService) {
        this.healthCheckerService = healthCheckerService;
    }

    @GetMapping("/healthz")
    public ResponseEntity<Void> checkHealth(@RequestBody(required = false) String payload, @RequestParam Map<String, String> queryParams) {
        if ((payload != null && !payload.isEmpty()) || (queryParams!=null && !queryParams.isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .headers(setHeaders())
                    .build();
        }

        try {
            if (!healthCheckerService.isDatabaseConnected()) {
                throw new RuntimeException("Database connection unavailable");
            }
            healthCheckerService.insertHealthCheck();
            return ResponseEntity.status(HttpStatus.OK)
                    .headers(setHeaders())
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .headers(setHeaders())
                    .build();
        }
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
            RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.TRACE,
            RequestMethod.HEAD})
    public ResponseEntity<Void> handleMethodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(setHeaders())
                .build();
    }

    private HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("Connection", "keep-alive");
        return headers;
    }
}
