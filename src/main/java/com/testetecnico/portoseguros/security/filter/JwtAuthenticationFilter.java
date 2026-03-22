package com.testetecnico.portoseguros.security.filter;

import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.repository.StudentRepository;
import com.testetecnico.portoseguros.security.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final StudentRepository studentRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, StudentRepository studentRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.studentRepository = studentRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request.getHeader("Authorization"));

        try {
            if (StringUtils.hasText(token) && jwtTokenProvider.isTokenValid(token) && isContextNotAuthenticated()) {
                UUID studentId = jwtTokenProvider.extractStudentId(token);
                Optional<Student> studentOpt = studentRepository.findById(studentId);

                if (studentOpt.isPresent()) {
                    var student = studentOpt.get();
                    var role = student.getRole() != null ? student.getRole().name() : "STUDENT";
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + role));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            student, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            // Ignore malformed/invalid tokens and continue without authentication
        }

        filterChain.doFilter(request, response);
    }

    private boolean isContextNotAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private String resolveToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    }
}

