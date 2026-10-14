package com.example.demo.exception;

public class InvalidAttendanceException extends RuntimeException {

    public InvalidAttendanceException(String message) {
        super(message);
    }
}
