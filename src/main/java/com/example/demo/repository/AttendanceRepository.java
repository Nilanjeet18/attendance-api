package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Check-out न केलेला (open) record शोधतो
    Optional<Attendance> findByEmployeeIdAndCheckOutTimeIsNull(Long employeeId);

    // एका employee चे सगळे records, नवीन आधी
    List<Attendance> findByEmployeeIdOrderByDateDescCheckInTimeDesc(Long employeeId);

    // एका तारखेचे सगळे records
    List<Attendance> findByDate(LocalDate date);
}