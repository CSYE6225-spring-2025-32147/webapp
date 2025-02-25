package edu.neu.csye6225.health_checker;



import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.sql.Connection;


import static io.restassured.RestAssured.*;


@ExtendWith({MockitoExtension.class, SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthCheckApplicationTest {

    @LocalServerPort
    private int serverPort;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = serverPort;
    }
    @Test
    public void testHealthCheckSuccess() {
        given()
                .when()
                .get("/healthz")  // Path for the health check endpoint
                .then()
                .statusCode(200)  // Assert that the response code is 200
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }



    @Test
    public void testHealthCheckBadRequestWithPayload() {
        given()
                .body("{ \"invalid\": \"data\" }")  // Sending invalid payload in body
                .when()
                .get("/healthz")
                .then()
                .statusCode(400)  // Should return 400 because of bad request
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    public void testHealthCheckBadRequestWithQueryParams() {
        given()
                .queryParam("invalid", "param")  // Adding query parameters
                .when()
                .get("/healthz")
                .then()
                .statusCode(400)  // Should return 400 due to invalid query parameters
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    public void testHealthCheckMethodNotAllowed() {
        // List of all methods to test except GET
        String[] methods = {"POST", "PUT", "DELETE", "PATCH"};

        for (String method : methods) {
            given()
                    .when()
                    .request(method , "/healthz")  // Using dynamic method (POST, PUT, DELETE, PATCH, HEAD)
                    .then()
                    .statusCode(405)  // Should return 405 Method Not Allowed
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "nosniff");
        }
    }
}