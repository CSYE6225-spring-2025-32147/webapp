package edu.neu.csye6225.health_checker.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class HealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkId;

    private LocalDateTime dateTime;


    public HealthCheck() {

        this.dateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getCheckId() {

        return checkId;
    }

    public void setCheckId(Long checkId) {

        this.checkId = checkId;
    }

    public LocalDateTime getDatetime() {

        return dateTime;
    }

    public void setDatetime(LocalDateTime datetime) {

        this.dateTime = datetime;
    }
}
