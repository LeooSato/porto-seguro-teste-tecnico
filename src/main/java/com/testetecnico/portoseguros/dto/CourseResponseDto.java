package com.testetecnico.portoseguros.dto;

import java.util.UUID;

public record CourseResponseDto(
        UUID id,
        String name,
        String description
) { }

