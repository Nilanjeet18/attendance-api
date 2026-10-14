package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

public record CheckInRequest(

        @NotNull(message = "Employee id is required")
        @Positive(message = "Employee id must be a positive number")
        Long employeeId,

        // Optional: दिला नाही तर server ची current वेळ वापरली जाईल
        @PastOrPresent(message = "Check-in time cannot be in the future")
        LocalDateTime checkInTime
) {
}
