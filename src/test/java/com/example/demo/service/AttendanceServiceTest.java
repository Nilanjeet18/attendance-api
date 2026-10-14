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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("Nilanjeet Gugale", "nilanjeet@example.com", "Engineering");
        employee.setId(1L);
    }

    // ---------- check-in ----------

    @Test
    void checkIn_success_savesRecordWithoutCheckOut() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 14, 9, 30);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndCheckOutTimeIsNull(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceResponse response = attendanceService.checkIn(new CheckInRequest(1L, time));

        assertEquals(1L, response.employeeId());
        assertEquals(time, response.checkInTime());
        assertEquals(LocalDate.of(2026, 10, 14), response.date());
        assertNull(response.checkOutTime());
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void checkIn_withoutTime_usesCurrentTime() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndCheckOutTimeIsNull(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceResponse response = attendanceService.checkIn(new CheckInRequest(1L, null));

        assertNotNull(response.checkInTime());
        assertNotNull(response.date());
    }

    @Test
    void checkIn_employeeNotFound_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.checkIn(new CheckInRequest(99L, null)));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void checkIn_alreadyCheckedIn_throwsException() {
        Attendance open = new Attendance();
        open.setEmployee(employee);
        open.setCheckInTime(LocalDateTime.of(2026, 10, 14, 9, 0));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndCheckOutTimeIsNull(1L)).thenReturn(Optional.of(open));

        assertThrows(InvalidAttendanceException.class,
                () -> attendanceService.checkIn(new CheckInRequest(1L, null)));

        verify(attendanceRepository, never()).save(any());
    }

    // ---------- check-out ----------

    @Test
    void checkOut_success_setsCheckOutTime() {
        LocalDateTime in = LocalDateTime.of(2026, 10, 14, 9, 0);
        LocalDateTime out = LocalDateTime.of(2026, 10, 14, 18, 0);

        Attendance open = new Attendance();
        open.setEmployee(employee);
        open.setDate(in.toLocalDate());
        open.setCheckInTime(in);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndCheckOutTimeIsNull(1L)).thenReturn(Optional.of(open));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceResponse response = attendanceService.checkOut(new CheckOutRequest(1L, out));

        assertEquals(out, response.checkOutTime());
        assertEquals(in, response.checkInTime());
    }

    @Test
    void checkOut_withoutActiveCheckIn_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndCheckOutTimeIsNull(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidAttendanceException.class,
                () -> attendanceService.checkOut(new CheckOutRequest(1L, null)));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void checkOut_beforeCheckInTime_throwsException() {
        LocalDateTime in = LocalDateTime.of(2026, 10, 14, 9, 0);
        LocalDateTime earlier = LocalDateTime.of(2026, 10, 14, 8, 0);

        Attendance open = new Attendance();
        open.setEmployee(employee);
        open.setCheckInTime(in);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndCheckOutTimeIsNull(1L)).thenReturn(Optional.of(open));

        assertThrows(InvalidAttendanceException.class,
                () -> attendanceService.checkOut(new CheckOutRequest(1L, earlier)));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void checkOut_employeeNotFound_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.checkOut(new CheckOutRequest(99L, null)));
    }

    // ---------- records ----------

    @Test
    void getAttendanceByEmployee_returnsRecords() {
        Attendance a = new Attendance();
        a.setId(10L);
        a.setEmployee(employee);
        a.setDate(LocalDate.of(2026, 10, 14));
        a.setCheckInTime(LocalDateTime.of(2026, 10, 14, 9, 0));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdOrderByDateDescCheckInTimeDesc(1L)).thenReturn(List.of(a));

        List<AttendanceResponse> result = attendanceService.getAttendanceByEmployee(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
    }

    @Test
    void getAttendanceByEmployee_employeeNotFound_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.getAttendanceByEmployee(99L));
    }

    @Test
    void getAttendanceByDate_returnsRecords() {
        LocalDate date = LocalDate.of(2026, 10, 14);
        Attendance a = new Attendance();
        a.setId(10L);
        a.setEmployee(employee);
        a.setDate(date);
        a.setCheckInTime(LocalDateTime.of(2026, 10, 14, 9, 0));

        when(attendanceRepository.findByDate(date)).thenReturn(List.of(a));

        List<AttendanceResponse> result = attendanceService.getAttendanceByDate(date);

        assertEquals(1, result.size());
        assertEquals(date, result.get(0).date());
    }

    @Test
    void getAttendanceByDate_noRecords_returnsEmptyList() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        when(attendanceRepository.findByDate(date)).thenReturn(List.of());

        assertTrue(attendanceService.getAttendanceByDate(date).isEmpty());
    }
}