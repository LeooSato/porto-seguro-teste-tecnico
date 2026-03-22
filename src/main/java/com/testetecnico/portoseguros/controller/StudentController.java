package com.testetecnico.portoseguros.controller;

import com.testetecnico.portoseguros.dto.CreateUserDto;
import com.testetecnico.portoseguros.dto.StudentRequestDto;
import com.testetecnico.portoseguros.dto.StudentResponseDto;
import com.testetecnico.portoseguros.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final UserService userService;

    public StudentController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<StudentResponseDto> create(@Valid @RequestBody StudentRequestDto request) {
        CreateUserDto userDto = new CreateUserDto(
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.email(),
                request.phone(),
                request.password());
        StudentResponseDto response = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
