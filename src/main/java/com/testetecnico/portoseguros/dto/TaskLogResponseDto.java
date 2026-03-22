package com.testetecnico.portoseguros.dto;

import com.testetecnico.portoseguros.entity.TaskCategory;
import java.time.LocalDate;
import java.util.UUID;

public record TaskLogResponseDto(
        UUID id,
        UUID enrollmentId,
        UUID courseId,
        TaskCategory category,
        LocalDate date,
        String description,
        Integer timeSpentMinutes
) { }

