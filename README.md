
# Health Checker - Cloud Native Web Application

## Overview
This is a **Cloud Native Web** application that performs a health check using a **MySQL database**. The application automatically creates the required database and table using **Hibernate** and provides a RESTful API for health checks.

---

## Prerequisites

### 1. Install Java 17 (or later)

Ensure you have Java installed: 
```sh
java -version
```
### 2. Install MySQL

Check if MySQL is installed:
```sh
mysql --version
```
If not installed, install MySQL from the MySQL Community Server.

Start the MySQL server:


### 3. Install Maven
Check if Maven is installed:
```
mvn -version
```

## Project Setup

### 1. Clone the Repository
Clone the repository to your local machine:
```sh
git clone <repository-url>
cd <repository-folder>
```
### 2. Configure application.properties
Configure the `application.properties` file with your database settings.

## Running the Application

### 1. Build the Project Using Maven
```sh
mvn clean install
```
### 2. Run the Spring Boot Application
```sh
mvn spring-boot:run
```
### 3. Verify MySQL Tables 
After running the application, check the database:

```sh
mysql -u root -p
```
```sh
USE DATABASE_NAME;
SHOW TABLES;
SELECT * FROM TABLE_NAME;
```

## API endpoints

1. Health Check Endpoint
* URL: GET /healthz
    * Description: Inserts a health check record into the database and returns an appropriate HTTP status.
* Response Codes:
    * 200 OK: Health check inserted successfully.
    * 503 Service Unavailable: Database insert failed.
    * 400 Bad Request: Request contains a payload (should be empty).
    * 405 Method Not Allowed: Any method other than GET.

### Example usage:
```sh
curl -X GET http://localhost:8080/healthz -v
```

