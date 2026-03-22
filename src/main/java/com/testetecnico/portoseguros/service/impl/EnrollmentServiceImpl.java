package com.testetecnico.portoseguros.service.impl;

import com.testetecnico.portoseguros.dto.EnrollmentResponseDto;
import com.testetecnico.portoseguros.entity.Course;
import com.testetecnico.portoseguros.entity.Enrollment;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.exception.ResourceNotFoundException;
import com.testetecnico.portoseguros.repository.CourseRepository;
import com.testetecnico.portoseguros.repository.EnrollmentRepository;
import com.testetecnico.portoseguros.repository.StudentRepository;
import com.testetecnico.portoseguros.service.EnrollmentService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

    private static final int MAX_ENROLLMENTS = 3;
    private static final int MONTHS_TO_COMPLETE = 6;

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository,
                                 StudentRepository studentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public EnrollmentResponseDto enroll(UUID studentId, UUID courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        long currentEnrollments = enrollmentRepository.countByStudentId(student.getId());
        if (currentEnrollments >= MAX_ENROLLMENTS) {
            log.info("Enrollment denied for student {}: max enrollments reached ({})", studentId, currentEnrollments);
            throw new BusinessException("Maximum of " + MAX_ENROLLMENTS + " active enrollments reached");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            log.info("Enrollment denied for student {}: already enrolled in course {}", studentId, courseId);
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
        log.info("Enrollment created: student {} -> course {} (enrollment {})", studentId, courseId, saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> listMyEnrollments(UUID studentId) {
        return enrollmentRepository.findByStudentId(studentId)
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

