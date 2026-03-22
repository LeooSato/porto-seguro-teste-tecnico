package com.testetecnico.portoseguros.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.testetecnico.portoseguros.entity.Role;
import com.testetecnico.portoseguros.entity.Student;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String VALID_SECRET = "0123456789ABCDEF0123456789ABCDEF";

    @Test
    void constructorShouldRejectEmptySecret() {
        assertThatThrownBy(() -> new JwtTokenProvider("", 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT secret must not be empty");
    }

    @Test
    void constructorShouldRejectTooShortSecret() {
        assertThatThrownBy(() -> new JwtTokenProvider("short-secret", 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT secret must be at least 256 bits");
    }

    @Test
    void shouldGenerateAndParseTokenClaims() {
        JwtTokenProvider provider = new JwtTokenProvider(VALID_SECRET, 60);
        Student student = student();

        String token = provider.generateToken(student);

        assertThat(provider.isTokenValid(token)).isTrue();
        assertThat(provider.extractStudentId(token)).isEqualTo(student.getId());
        assertThat(provider.extractEmail(token)).isEqualTo(student.getEmail());
    }

    @Test
    void shouldReturnFalseForMalformedToken() {
        JwtTokenProvider provider = new JwtTokenProvider(VALID_SECRET, 60);

        assertThat(provider.isTokenValid("not-a-token")).isFalse();
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        JwtTokenProvider provider = new JwtTokenProvider(VALID_SECRET, -1);
        Student student = student();
        String token = provider.generateToken(student);

        assertThat(provider.isTokenValid(token)).isFalse();
    }

    private Student student() {
        Student student = new Student();
        student.setId(UUID.randomUUID());
        student.setFirstName("Ana");
        student.setLastName("Silva");
        student.setEmail("ana@example.com");
        student.setRole(Role.STUDENT);
        return student;
    }
}
