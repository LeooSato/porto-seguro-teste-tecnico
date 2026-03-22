package com.testetecnico.portoseguros.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.LoginRequestDto;
import com.testetecnico.portoseguros.dto.LoginResponseDto;
import com.testetecnico.portoseguros.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginShouldReturnToken() {
        LoginRequestDto request = new LoginRequestDto("student@example.com", "secret");
        LoginResponseDto responseDto = new LoginResponseDto("jwt-token");
        when(userService.authenticate(request)).thenReturn(responseDto);

        ResponseEntity<LoginResponseDto> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseDto);
    }
}
