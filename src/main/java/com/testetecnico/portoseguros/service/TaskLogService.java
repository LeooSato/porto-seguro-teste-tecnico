package com.testetecnico.portoseguros.service;

import com.testetecnico.portoseguros.dto.TaskLogRequestDto;
import com.testetecnico.portoseguros.dto.TaskLogResponseDto;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskLogService {

    TaskLogResponseDto create(TaskLogRequestDto request, UUID studentId);

    TaskLogResponseDto update(UUID id, TaskLogRequestDto request, UUID studentId);

    void delete(UUID id, UUID studentId);

    List<TaskLogResponseDto> list(UUID studentId, LocalDate startDate, LocalDate endDate);
}

