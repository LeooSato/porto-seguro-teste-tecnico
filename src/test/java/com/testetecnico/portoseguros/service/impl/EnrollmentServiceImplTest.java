package com.testetecnico.portoseguros.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.EnrollmentResponseDto;
import com.testetecnico.portoseguros.entity.Course;
import com.testetecnico.portoseguros.entity.Enrollment;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.exception.ResourceNotFoundException;
import com.testetecnico.portoseguros.repository.CourseRepository;
import com.testetecnico.portoseguros.repository.EnrollmentRepository;
import com.testetecnico.portoseguros.repository.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Test
    void enrollStudentShouldThrowWhenStudentReachedMaxEnrollments() {
        UUID studentId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        UUID courseId = UUID.randomUUID();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.countByStudentId(studentId)).thenReturn(3L);

        assertThatThrownBy(() -> enrollmentService.enroll(studentId, courseId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Maximum of 3 active enrollments reached");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollStudentShouldThrowWhenAlreadyEnrolledInCourse() {
        UUID studentId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        UUID courseId = UUID.randomUUID();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.countByStudentId(studentId)).thenReturn(1L);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enroll(studentId, courseId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Student already enrolled in this course");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollStudentShouldThrowWhenCourseDoesNotExist() {
        UUID studentId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        UUID courseId = UUID.randomUUID();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.countByStudentId(studentId)).thenReturn(1L);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enroll(studentId, courseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course not found");
    }

    @Test
    void enrollStudentShouldCreateEnrollmentAndReturnResponse() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        Course course = Course.builder().id(courseId).name("Spring Boot").description("API").build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.countByStudentId(studentId)).thenReturn(0L);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            enrollment.setId(enrollmentId);
            return enrollment;
        });

        EnrollmentResponseDto response = enrollmentService.enroll(studentId, courseId);

        assertThat(response.id()).isEqualTo(enrollmentId);
        assertThat(response.courseId()).isEqualTo(courseId);
        assertThat(response.courseName()).isEqualTo("Spring Boot");
        assertThat(response.expectedCompletionDate()).isEqualTo(response.enrollmentDate().plusMonths(6));
    }

    @Test
    void listMyEnrollmentsShouldMapEnrollments() {
        UUID studentId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        UUID enrollmentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Enrollment enrollment = Enrollment.builder()
                .id(enrollmentId)
                .student(student)
                .course(Course.builder().id(courseId).name("Java").description("Basics").build())
                .enrollmentDate(LocalDate.of(2026, 1, 1))
                .expectedCompletionDate(LocalDate.of(2026, 7, 1))
                .build();
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(List.of(enrollment));

        List<EnrollmentResponseDto> result = enrollmentService.listMyEnrollments(studentId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(enrollmentId);
        assertThat(result.getFirst().courseId()).isEqualTo(courseId);
        assertThat(result.getFirst().courseName()).isEqualTo("Java");
    }

    private Student studentWithId(UUID id) {
        Student student = new Student();
        student.setId(id);
        return student;
    }
}
