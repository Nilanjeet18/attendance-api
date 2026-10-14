-- Employee Attendance Tracking REST API
-- MySQL 8 schema
--
-- NOTE: The application creates these tables automatically on first run
-- (spring.jpa.hibernate.ddl-auto=update). This file is provided for reference
-- or for creating the schema manually.

CREATE DATABASE IF NOT EXISTS attendance_db;
USE attendance_db;

CREATE TABLE IF NOT EXISTS employees (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    department  VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_employees_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attendance (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    employee_id     BIGINT      NOT NULL,
    date            DATE        NOT NULL,
    check_in_time   DATETIME(6) NOT NULL,
    check_out_time  DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_attendance_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id)
) ENGINE=InnoDB;
