package com.example.demo.dto;

public record EmployeeResponse(
        Long id,
        String name,
        String email,
        String department
) {
}
