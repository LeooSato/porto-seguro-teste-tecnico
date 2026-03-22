package com.testetecnico.portoseguros.service.impl;

import com.testetecnico.portoseguros.dto.StudentRequestDto;
import com.testetecnico.portoseguros.dto.StudentResponseDto;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.entity.Role;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.repository.StudentRepository;
import com.testetecnico.portoseguros.service.StudentService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class StudentServiceImpl implements StudentService {

    private static final int MINIMUM_AGE = 16;

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public StudentResponseDto create(StudentRequestDto request) {
        validateMinimumAge(request.birthDate());
        ensureEmailIsUnique(request.email());

        Student student = Student.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .email(request.email())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.STUDENT)
                .build();

        Student saved = studentRepository.save(student);
        return new StudentResponseDto(saved.getId(), saved.getFirstName(), saved.getLastName(), saved.getEmail());
    }

    private void validateMinimumAge(LocalDate birthDate) {
        long age = ChronoUnit.YEARS.between(birthDate, LocalDate.now());
        if (age < MINIMUM_AGE) {
            throw new BusinessException("Student must be at least " + MINIMUM_AGE + " years old");
        }
    }

    private void ensureEmailIsUnique(String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new BusinessException("Email already in use");
        }
    }
}

