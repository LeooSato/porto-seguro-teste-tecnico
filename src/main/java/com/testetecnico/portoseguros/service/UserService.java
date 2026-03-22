package com.testetecnico.portoseguros.service;

import com.testetecnico.portoseguros.dto.CreateUserDto;
import com.testetecnico.portoseguros.dto.LoginRequestDto;
import com.testetecnico.portoseguros.dto.LoginResponseDto;
import com.testetecnico.portoseguros.dto.StudentResponseDto;

public interface UserService {

    StudentResponseDto createUser(CreateUserDto request);

    StudentResponseDto createAdmin(CreateUserDto request);

    LoginResponseDto authenticate(LoginRequestDto request);
}

