package com.testetecnico.portoseguros.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.entity.Role;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.repository.StudentRepository;
import com.testetecnico.portoseguros.security.service.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterShouldAuthenticateWhenTokenIsValidAndStudentExists() throws Exception {
        UUID studentId = UUID.randomUUID();
        Student student = new Student();
        student.setId(studentId);
        student.setRole(Role.ADMIN);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var chain = new org.springframework.mock.web.MockFilterChain();

        when(jwtTokenProvider.isTokenValid("valid-token")).thenReturn(true);
        when(jwtTokenProvider.extractStudentId("valid-token")).thenReturn(studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        jwtAuthenticationFilter.doFilter(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(student);
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(auth.getDetails()).isInstanceOf(WebAuthenticationDetails.class);
    }

    @Test
    void doFilterShouldNotAuthenticateWhenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        var chain = new org.springframework.mock.web.MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider, never()).isTokenValid(org.mockito.ArgumentMatchers.anyString());
        verify(studentRepository, never()).findById(org.mockito.ArgumentMatchers.any(UUID.class));
    }

    @Test
    void doFilterShouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var chain = new org.springframework.mock.web.MockFilterChain();

        when(jwtTokenProvider.isTokenValid("invalid-token")).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(studentRepository, never()).findById(org.mockito.ArgumentMatchers.any(UUID.class));
    }

    @Test
    void doFilterShouldIgnoreJwtExceptionAndContinueWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer broken-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var chain = new org.springframework.mock.web.MockFilterChain();

        when(jwtTokenProvider.isTokenValid("broken-token")).thenReturn(true);
        when(jwtTokenProvider.extractStudentId("broken-token")).thenThrow(new JwtException("Broken token"));

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterShouldNotOverrideExistingAuthentication() throws Exception {
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken("existing", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var chain = new org.springframework.mock.web.MockFilterChain();

        when(jwtTokenProvider.isTokenValid("valid-token")).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
        verify(studentRepository, never()).findById(org.mockito.ArgumentMatchers.any(UUID.class));
    }

    @Test
    void resolveTokenShouldReturnNullForInvalidHeader() {
        Object result = ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "resolveToken", "Invalid token");
        assertThat(result).isNull();
    }

    @Test
    void resolveTokenShouldTrimBearerPrefix() {
        Object result = ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "resolveToken", "Bearer  abc ");
        assertThat(result).isEqualTo("abc");
    }
}
