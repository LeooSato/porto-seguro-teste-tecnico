package com.testetecnico.portoseguros.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record StudentRequestDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate birthDate,
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotBlank String password
) { }

