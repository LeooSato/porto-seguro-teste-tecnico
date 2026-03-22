package com.testetecnico.portoseguros.service;

import com.testetecnico.portoseguros.dto.CourseRequestDto;
import com.testetecnico.portoseguros.dto.CourseResponseDto;
import java.util.List;
import java.util.UUID;

public interface CourseService {

    CourseResponseDto create(CourseRequestDto request);

    CourseResponseDto update(UUID id, CourseRequestDto request);

    void delete(UUID id);

    List<CourseResponseDto> findAll();

    CourseResponseDto findById(UUID id);
}

