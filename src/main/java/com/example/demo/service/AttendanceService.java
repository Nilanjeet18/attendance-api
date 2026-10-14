package com.example.demo.service;

import com.example.demo.dto.AttendanceResponse;
import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutRequest;
import com.example.demo.entity.Attendance;
import com.example.demo.entity.Employee;
import com.example.demo.exception.InvalidAttendanceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request) {
        Employee employee = findEmployee(request.employeeId());

        // Rule: आधीच check-in असेल (check-out झालं नसेल) तर पुन्हा check-in नको
        attendanceRepository.findByEmployeeIdAndCheckOutTimeIsNull(employee.getId())
                .ifPresent(open -> {
                    throw new InvalidAttendanceException(
                            "Employee " + employee.getId() + " is already checked in. Please check out first");
                });

        LocalDateTime checkInTime = request.checkInTime() != null
                ? request.checkInTime()
                : LocalDateTime.now();

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setCheckInTime(checkInTime);
        attendance.setDate(checkInTime.toLocalDate());

        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceResponse checkOut(CheckOutRequest request) {
        Employee employee = findEmployee(request.employeeId());

        // Rule: check-in शिवाय check-out करता येत नाही
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndCheckOutTimeIsNull(employee.getId())
                .orElseThrow(() -> new InvalidAttendanceException(
                        "Employee " + employee.getId() + " has no active check-in"));

        LocalDateTime checkOutTime = request.checkOutTime() != null
                ? request.checkOutTime()
                : LocalDateTime.now();

        // Rule: check-out वेळ check-in नंतरची असायला हवी
        if (!checkOutTime.isAfter(attendance.getCheckInTime())) {
            throw new InvalidAttendanceException("Check-out time must be after check-in time");
        }

        attendance.setCheckOutTime(checkOutTime);
        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByEmployee(Long employeeId) {
        Employee employee = findEmployee(employeeId);
        return attendanceRepository
                .findByEmployeeIdOrderByDateDescCheckInTimeDesc(employee.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream()
                .map(this::toResponse)
                .toList();
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + id));
    }

    private AttendanceResponse toResponse(Attendance a) {
        return new AttendanceResponse(
                a.getId(),
                a.getEmployee().getId(),
                a.getEmployee().getName(),
                a.getDate(),
                a.getCheckInTime(),
                a.getCheckOutTime()
        );
    }
}