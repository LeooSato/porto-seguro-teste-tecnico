package com.testetecnico.portoseguros.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.EnrollmentRequestDto;
import com.testetecnico.portoseguros.dto.EnrollmentResponseDto;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.service.EnrollmentService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EnrollmentControllerUnitTest {

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private EnrollmentController enrollmentController;

    @Test
    void enrollShouldReturnCreated() {
        Student student = new Student();
        student.setId(UUID.randomUUID());
        UUID courseId = UUID.randomUUID();

        EnrollmentRequestDto request = new EnrollmentRequestDto(courseId);
        EnrollmentResponseDto serviceResponse = new EnrollmentResponseDto(
                UUID.randomUUID(),
                courseId,
                "Java",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 1)
        );
        when(enrollmentService.enrollStudent(courseId, student)).thenReturn(serviceResponse);

        ResponseEntity<EnrollmentResponseDto> response = enrollmentController.enroll(request, student);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void listMyEnrollmentsShouldReturnOk() {
        Student student = new Student();
        student.setId(UUID.randomUUID());
        List<EnrollmentResponseDto> serviceResponse = List.of(new EnrollmentResponseDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Java",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 1)
        ));
        when(enrollmentService.listMyEnrollments(student)).thenReturn(serviceResponse);

        ResponseEntity<List<EnrollmentResponseDto>> response = enrollmentController.listMyEnrollments(student);

        verify(enrollmentService).listMyEnrollments(student);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }
}
