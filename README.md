# Employee Attendance Tracking REST API

A secure-by-design RESTful backend service built with **Java** and **Spring Boot**. Employees can clock in and out, and managers can retrieve attendance records by employee or by date.

> This submission is a \*\*backend-only\*\* REST API (no frontend). It can be tested with Postman or curl.

## Tech Stack

* Java 17
* Spring Boot 4.1.1 (Spring Web, Spring Data JPA / Hibernate, Bean Validation)
* MySQL 8
* JUnit 5 and Mockito (service layer unit tests)
* Maven

## Prerequisites

* JDK 17 or higher
* Maven 3.8+ (or the Maven Wrapper `mvnw`, if present in the project)
* MySQL 8 running on `localhost:3306`

## Database Setup

1. Start MySQL and create the database:

```sql
CREATE DATABASE IF NOT EXISTS attendance\_db;
```

2. The database password is read from the `DB\_PASSWORD` environment variable (see `src/main/resources/application.properties`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/attendance\_db?serverTimezone=Asia/Kolkata
spring.datasource.username=root
spring.datasource.password=${DB\_PASSWORD:YOUR\_PASSWORD}
```

Set it before running the application.

Windows (Command Prompt):

```
set DB\_PASSWORD=your\_mysql\_password
```

Windows (PowerShell):

```
$env:DB\_PASSWORD="your\_mysql\_password"
```

Linux / macOS:

```
export DB\_PASSWORD=your\_mysql\_password
```

If your MySQL username is not `root`, change `spring.datasource.username` in `application.properties`.

3. Tables (`employees`, `attendance`) are created automatically by Hibernate on first run (`spring.jpa.hibernate.ddl-auto=update`). A reference schema is also provided in `schema.sql` if you prefer to create the tables manually:

```
mysql -u root -p < schema.sql
```

## Build

```
mvn clean install
```

## Run

```
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

## Test

```
mvn test
```

Unit tests cover the service layer (`AttendanceService`, `EmployeeService`) using JUnit 5 and Mockito. The repositories are mocked, so **no database is needed to run the tests**.

## API Endpoints

|Method|URL|Description|Success|
|-|-|-|-|
|POST|`/api/employees`|Create an employee|201|
|GET|`/api/employees/{id}`|Get an employee by id|200|
|GET|`/api/employees`|List all employees|200|
|POST|`/api/attendance/check-in`|Record check-in|201|
|POST|`/api/attendance/check-out`|Record check-out|200|
|GET|`/api/attendance/employee/{employeeId}`|Attendance records of an employee (latest first)|200|
|GET|`/api/attendance?date=YYYY-MM-DD`|Attendance records of a date|200|

### Sample Requests

Create an employee:

```
POST /api/employees
Content-Type: application/json

{
  "name": "Nilanjeet Gugale",
  "email": "nilanjeet@example.com",
  "department": "Engineering"
}
```

Check-in (time is optional; the server time is used if omitted):

```
POST /api/attendance/check-in
Content-Type: application/json

{
  "employeeId": 1
}
```

Check-in with an explicit time (ISO format, must not be in the future):

```
{
  "employeeId": 1,
  "checkInTime": "2026-10-14T09:30:00"
}
```

Check-out:

```
POST /api/attendance/check-out
Content-Type: application/json

{
  "employeeId": 1
}
```

Get records:

```
GET /api/attendance/employee/1
GET /api/attendance?date=2026-10-14
```

### Sample Error Response

```json
{
  "timestamp": "2026-10-14T19:30:00.123",
  "status": 400,
  "error": "Bad Request",
  "messages": \["Employee 1 is already checked in. Please check out first"]
}
```

## Validation and Business Rules

* `employeeId` is required and must be a positive number.
* Check-in and check-out times, when provided, must be valid ISO date-times and must not be in the future.
* Employee `name` and `email` are required; `email` must be a valid format and unique.
* An employee must check in before checking out.
* An employee cannot check in again while a check-in is still open (no check-out yet).
* Check-out time must be after the check-in time.

|Situation|HTTP Status|
|-|-|
|Employee not found|404 Not Found|
|Validation failure / malformed JSON / invalid date|400 Bad Request|
|Business rule violation (duplicate check-in, no active check-in, bad check-out time)|400 Bad Request|
|Duplicate employee email|409 Conflict|

## Project Structure

```
attendance-api/
├── pom.xml
├── README.md
├── schema.sql
└── src/
    ├── main/
    │   ├── java/com/example/demo/
    │   │   ├── AttendanceApiApplication.java
    │   │   ├── controller/   REST controllers
    │   │   ├── service/      Business logic
    │   │   ├── repository/   Spring Data JPA repositories
    │   │   ├── entity/       JPA entities (Employee, Attendance)
    │   │   ├── dto/          Request and response records
    │   │   └── exception/    Custom exceptions and global handler
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/example/demo/service/
            ├── AttendanceServiceTest.java
            └── EmployeeServiceTest.java
```

## Author

Nilanjeet Gugale

