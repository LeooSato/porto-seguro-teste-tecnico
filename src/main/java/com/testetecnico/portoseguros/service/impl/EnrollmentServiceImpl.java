package com.testetecnico.portoseguros.service.impl;

import com.testetecnico.portoseguros.dto.EnrollmentResponseDto;
import com.testetecnico.portoseguros.entity.Course;
import com.testetecnico.portoseguros.entity.Enrollment;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.exception.ResourceNotFoundException;
import com.testetecnico.portoseguros.repository.CourseRepository;
import com.testetecnico.portoseguros.repository.EnrollmentRepository;
import com.testetecnico.portoseguros.service.EnrollmentService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final int MAX_ENROLLMENTS = 3;
    private static final int MONTHS_TO_COMPLETE = 6;

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public EnrollmentResponseDto enrollStudent(UUID courseId, Student student) {
        long currentEnrollments = enrollmentRepository.countByStudentId(student.getId());
        if (currentEnrollments >= MAX_ENROLLMENTS) {
            throw new BusinessException("Maximum of " + MAX_ENROLLMENTS + " active enrollments reached");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new BusinessException("Student already enrolled in this course");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        LocalDate enrollmentDate = LocalDate.now();
        LocalDate expectedCompletionDate = enrollmentDate.plus(MONTHS_TO_COMPLETE, ChronoUnit.MONTHS);

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrollmentDate(enrollmentDate)
                .expectedCompletionDate(expectedCompletionDate)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> listMyEnrollments(Student student) {
        return enrollmentRepository.findByStudentId(student.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EnrollmentResponseDto toResponse(Enrollment enrollment) {
        return new EnrollmentResponseDto(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getName(),
                enrollment.getEnrollmentDate(),
                enrollment.getExpectedCompletionDate()
        );
    }
}

