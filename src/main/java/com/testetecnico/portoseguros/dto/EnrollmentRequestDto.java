package com.testetecnico.portoseguros.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EnrollmentRequestDto(
        @NotNull UUID courseId
) { }

