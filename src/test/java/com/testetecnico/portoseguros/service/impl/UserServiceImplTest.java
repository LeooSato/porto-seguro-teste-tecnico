package com.testetecnico.portoseguros.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.CreateUserDto;
import com.testetecnico.portoseguros.dto.LoginRequestDto;
import com.testetecnico.portoseguros.dto.LoginResponseDto;
import com.testetecnico.portoseguros.dto.StudentResponseDto;
import com.testetecnico.portoseguros.entity.Role;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.exception.UnauthorizedException;
import com.testetecnico.portoseguros.repository.StudentRepository;
import com.testetecnico.portoseguros.security.service.JwtTokenProvider;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserShouldPersistStudentRoleAndReturnResponse() {
        CreateUserDto request = userRequest("student@example.com", LocalDate.now().minusYears(20));
        UUID userId = UUID.randomUUID();

        when(studentRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            student.setId(userId);
            return student;
        });

        StudentResponseDto response = userService.createUser(request);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.STUDENT);
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("student@example.com");
    }

    @Test
    void createAdminShouldPersistAdminRole() {
        CreateUserDto request = userRequest("admin@example.com", LocalDate.now().minusYears(20));

        when(studentRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createAdmin(request);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void createUserShouldThrowWhenUnderage() {
        CreateUserDto request = userRequest("young@example.com", LocalDate.now().minusYears(15));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User must be at least 16 years old");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void createUserShouldThrowWhenEmailAlreadyExists() {
        CreateUserDto request = userRequest("student@example.com", LocalDate.now().minusYears(20));
        when(studentRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email already in use");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void authenticateShouldThrowWhenEmailNotFound() {
        LoginRequestDto request = new LoginRequestDto("missing@example.com", "secret");
        when(studentRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void authenticateShouldThrowWhenPasswordIsInvalid() {
        Student student = Student.builder()
                .id(UUID.randomUUID())
                .email("student@example.com")
                .password("encoded")
                .build();
        LoginRequestDto request = new LoginRequestDto("student@example.com", "wrong");

        when(studentRepository.findByEmail(request.email())).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void authenticateShouldReturnTokenWhenCredentialsAreValid() {
        Student student = Student.builder()
                .id(UUID.randomUUID())
                .firstName("Ana")
                .lastName("Silva")
                .email("student@example.com")
                .password("encoded")
                .role(Role.STUDENT)
                .build();
        LoginRequestDto request = new LoginRequestDto("student@example.com", "secret");

        when(studentRepository.findByEmail(request.email())).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        when(jwtTokenProvider.generateToken(student)).thenReturn("jwt-token");

        LoginResponseDto response = userService.authenticate(request);

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    private CreateUserDto userRequest(String email, LocalDate birthDate) {
        return new CreateUserDto(
                "Ana",
                "Silva",
                birthDate,
                email,
                "11999999999",
                "secret"
        );
    }
}
