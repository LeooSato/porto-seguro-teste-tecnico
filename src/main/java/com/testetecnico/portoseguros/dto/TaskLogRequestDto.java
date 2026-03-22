package com.testetecnico.portoseguros.dto;

import com.testetecnico.portoseguros.entity.TaskCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record TaskLogRequestDto(
        @NotNull UUID enrollmentId,
        @NotNull LocalDate date,
        @NotNull TaskCategory category,
        @NotBlank @Size(max = 500) String description,
        @NotNull Integer timeSpentMinutes
) { }

