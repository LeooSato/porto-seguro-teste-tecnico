package com.testetecnico.portoseguros.service.impl;

import com.testetecnico.portoseguros.dto.CreateUserDto;
import com.testetecnico.portoseguros.dto.LoginRequestDto;
import com.testetecnico.portoseguros.dto.LoginResponseDto;
import com.testetecnico.portoseguros.entity.Role;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.exception.UnauthorizedException;
import com.testetecnico.portoseguros.repository.StudentRepository;
import com.testetecnico.portoseguros.security.service.JwtTokenProvider;
import com.testetecnico.portoseguros.service.UserService;
import com.testetecnico.portoseguros.dto.StudentResponseDto;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final int MINIMUM_AGE = 16;

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceImpl(StudentRepository studentRepository, PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public StudentResponseDto createUser(CreateUserDto request) {
        return createUserWithRole(request, Role.STUDENT);
    }

    @Override
    @Transactional
    public StudentResponseDto createAdmin(CreateUserDto request) {
        return createUserWithRole(request, Role.ADMIN);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto authenticate(LoginRequestDto request) {
        Student student = studentRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), student.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(student);
        return new LoginResponseDto(token);
    }

    private StudentResponseDto createUserWithRole(CreateUserDto request, Role role) {
        validate(request);
        Student student = Student.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .email(request.email())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .build();
        Student saved = studentRepository.save(student);
        return new StudentResponseDto(saved.getId(), saved.getFirstName(), saved.getLastName(), saved.getEmail());
    }

    private void validate(CreateUserDto request) {
        long age = ChronoUnit.YEARS.between(request.birthDate(), LocalDate.now());
        if (age < MINIMUM_AGE) {
            throw new BusinessException("User must be at least " + MINIMUM_AGE + " years old");
        }
        if (studentRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use");
        }
    }
}

