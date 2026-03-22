package com.testetecnico.portoseguros.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.StudentRequestDto;
import com.testetecnico.portoseguros.dto.StudentResponseDto;
import com.testetecnico.portoseguros.entity.Role;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.repository.StudentRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void createShouldPersistStudentWithEncodedPasswordAndDefaultRole() {
        StudentRequestDto request = new StudentRequestDto(
                "Ana",
                "Silva",
                LocalDate.now().minusYears(20),
                "ana@example.com",
                "11999999999",
                "secret"
        );

        when(studentRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            student.setId(UUID.randomUUID());
            return student;
        });

        StudentResponseDto response = studentService.create(request);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        Student saved = captor.getValue();

        assertThat(saved.getPassword()).isEqualTo("encoded-secret");
        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
        assertThat(saved.getEmail()).isEqualTo("ana@example.com");
        assertThat(response.firstName()).isEqualTo("Ana");
        assertThat(response.lastName()).isEqualTo("Silva");
        assertThat(response.email()).isEqualTo("ana@example.com");
    }

    @Test
    void createShouldThrowWhenStudentIsUnderage() {
        StudentRequestDto request = new StudentRequestDto(
                "Young",
                "Student",
                LocalDate.now().minusYears(15),
                "young@example.com",
                "11999999999",
                "secret"
        );

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Student must be at least 16 years old");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void createShouldAllowStudentWithMinimumAgeBoundary() {
        StudentRequestDto request = new StudentRequestDto(
                "Boundary",
                "User",
                LocalDate.now().minusYears(16),
                "boundary@example.com",
                "11999999999",
                "secret"
        );

        when(studentRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            student.setId(UUID.randomUUID());
            return student;
        });

        StudentResponseDto response = studentService.create(request);

        assertThat(response.email()).isEqualTo("boundary@example.com");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createShouldThrowWhenEmailAlreadyExists() {
        StudentRequestDto request = new StudentRequestDto(
                "Ana",
                "Silva",
                LocalDate.now().minusYears(20),
                "ana@example.com",
                "11999999999",
                "secret"
        );

        when(studentRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email already in use");

        verify(studentRepository, never()).save(any(Student.class));
    }
}
