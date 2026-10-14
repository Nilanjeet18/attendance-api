package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate date,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime
) {
}
