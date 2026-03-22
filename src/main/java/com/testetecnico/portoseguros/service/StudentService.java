package com.testetecnico.portoseguros.service;

import com.testetecnico.portoseguros.dto.StudentRequestDto;
import com.testetecnico.portoseguros.dto.StudentResponseDto;

public interface StudentService {

    StudentResponseDto create(StudentRequestDto request);
}

