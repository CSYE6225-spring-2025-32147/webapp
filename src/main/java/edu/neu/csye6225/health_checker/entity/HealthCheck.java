package edu.neu.csye6225.health_checker.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class HealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkId; // The unique identifier for the health check

    private LocalDateTime dateTime; // Timestamp for the health check record

    // Default constructor
    public HealthCheck() {
        this.dateTime = LocalDateTime.now(); // Set current time when the health check is created
    }

    // Getters and setters for the fields
    public Long getId() {
        return checkId;
    }

    public void setId(Long id) {
        this.checkId = id;
    }

    public LocalDateTime getTimestamp() {
        return dateTime;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.dateTime = timestamp;
    }
}