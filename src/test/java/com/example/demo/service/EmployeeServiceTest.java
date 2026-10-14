package com.example.demo.service;

import com.example.demo.dto.EmployeeRequest;
import com.example.demo.dto.EmployeeResponse;
import com.example.demo.entity.Employee;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createEmployee_success() {
        EmployeeRequest request = new EmployeeRequest("Nilanjeet", "nilanjeet@example.com", "Engineering");

        when(employeeRepository.existsByEmail("nilanjeet@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EmployeeResponse response = employeeService.createEmployee(request);

        assertEquals(1L, response.id());
        assertEquals("Nilanjeet", response.name());
        assertEquals("nilanjeet@example.com", response.email());
    }

    @Test
    void createEmployee_duplicateEmail_throwsException() {
        EmployeeRequest request = new EmployeeRequest("Nilanjeet", "nilanjeet@example.com", "Engineering");
        when(employeeRepository.existsByEmail("nilanjeet@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getEmployeeById_found() {
        Employee e = new Employee("Nilanjeet", "nilanjeet@example.com", "Engineering");
        e.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(e));

        EmployeeResponse response = employeeService.getEmployeeById(1L);

        assertEquals(1L, response.id());
    }

    @Test
    void getEmployeeById_notFound_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    void getAllEmployees_returnsList() {
        Employee e = new Employee("Nilanjeet", "nilanjeet@example.com", "Engineering");
        e.setId(1L);
        when(employeeRepository.findAll()).thenReturn(List.of(e));

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
    }
}