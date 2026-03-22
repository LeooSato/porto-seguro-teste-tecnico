package com.testetecnico.portoseguros.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.CreateUserDto;
import com.testetecnico.portoseguros.dto.StudentRequestDto;
import com.testetecnico.portoseguros.dto.StudentResponseDto;
import com.testetecnico.portoseguros.service.UserService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class StudentControllerUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private StudentController studentController;

    @Test
    void createShouldMapRequestToCreateUserAndReturnCreated() {
        StudentRequestDto request = new StudentRequestDto(
                "Ana",
                "Silva",
                LocalDate.of(2000, 1, 1),
                "ana@example.com",
                "11999999999",
                "secret"
        );
        StudentResponseDto serviceResponse =
                new StudentResponseDto(UUID.randomUUID(), "Ana", "Silva", "ana@example.com");
        when(userService.createUser(any(CreateUserDto.class))).thenReturn(serviceResponse);

        ResponseEntity<StudentResponseDto> response = studentController.create(request);

        ArgumentCaptor<CreateUserDto> captor = ArgumentCaptor.forClass(CreateUserDto.class);
        verify(userService).createUser(captor.capture());
        CreateUserDto mapped = captor.getValue();
        assertThat(mapped.firstName()).isEqualTo("Ana");
        assertThat(mapped.lastName()).isEqualTo("Silva");
        assertThat(mapped.email()).isEqualTo("ana@example.com");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }
}
