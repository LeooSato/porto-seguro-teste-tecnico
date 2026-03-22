package com.testetecnico.portoseguros.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EnrollmentResponseDto(
        UUID id,
        UUID courseId,
        String courseName,
        LocalDate enrollmentDate,
        LocalDate expectedCompletionDate
) { }

