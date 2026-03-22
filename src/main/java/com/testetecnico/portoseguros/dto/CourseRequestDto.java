package com.testetecnico.portoseguros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequestDto(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 500) String description
) { }

