package com.testetecnico.portoseguros.service;

import com.testetecnico.portoseguros.dto.TaskLogRequestDto;
import com.testetecnico.portoseguros.dto.TaskLogResponseDto;
import com.testetecnico.portoseguros.entity.Student;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskLogService {

    TaskLogResponseDto create(TaskLogRequestDto request, Student student);

    TaskLogResponseDto update(UUID id, TaskLogRequestDto request, Student student);

    void delete(UUID id, Student student);

    List<TaskLogResponseDto> list(Student student, LocalDate startDate, LocalDate endDate);
}

