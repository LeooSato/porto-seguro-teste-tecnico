package com.testetecnico.portoseguros.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.TaskLogRequestDto;
import com.testetecnico.portoseguros.dto.TaskLogResponseDto;
import com.testetecnico.portoseguros.entity.Course;
import com.testetecnico.portoseguros.entity.Enrollment;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.entity.TaskCategory;
import com.testetecnico.portoseguros.entity.TaskLog;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.exception.ResourceNotFoundException;
import com.testetecnico.portoseguros.repository.EnrollmentRepository;
import com.testetecnico.portoseguros.repository.TaskLogRepository;
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
class TaskLogServiceImplTest {

    @Mock
    private TaskLogRepository taskLogRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private TaskLogServiceImpl taskLogService;

    @Test
    void createShouldThrowWhenTimeIsNotInThirtyMinuteIncrements() {
        Student student = studentWithId(UUID.randomUUID());
        TaskLogRequestDto request = request(UUID.randomUUID(), 25);

        assertThatThrownBy(() -> taskLogService.create(request, student))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Time spent must be positive and in 30-minute increments");

        verify(taskLogRepository, never()).save(any(TaskLog.class));
    }

    @Test
    void createShouldThrowWhenEnrollmentIsNotFoundForStudent() {
        UUID studentId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        TaskLogRequestDto request = request(enrollmentId, 60);

        when(enrollmentRepository.findByIdAndStudentId(enrollmentId, studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskLogService.create(request, student))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Enrollment not found for student");
    }

    @Test
    void createShouldSaveAndReturnMappedResponse() {
        UUID studentId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();

        Student student = studentWithId(studentId);
        Enrollment enrollment = enrollment(enrollmentId, courseId, student);
        TaskLogRequestDto request = request(enrollmentId, 60);

        when(enrollmentRepository.findByIdAndStudentId(enrollmentId, studentId)).thenReturn(Optional.of(enrollment));
        when(taskLogRepository.save(any(TaskLog.class))).thenAnswer(invocation -> {
            TaskLog log = invocation.getArgument(0);
            log.setId(logId);
            return log;
        });

        TaskLogResponseDto response = taskLogService.create(request, student);

        assertThat(response.id()).isEqualTo(logId);
        assertThat(response.enrollmentId()).isEqualTo(enrollmentId);
        assertThat(response.courseId()).isEqualTo(courseId);
        assertThat(response.timeSpentMinutes()).isEqualTo(60);
        assertThat(response.category()).isEqualTo(TaskCategory.PRATICA);
    }

    @Test
    void updateShouldThrowWhenTaskLogNotFoundForStudent() {
        UUID studentId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        TaskLogRequestDto request = request(enrollmentId, 60);

        when(taskLogRepository.findByIdAndEnrollmentStudentId(logId, studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskLogService.update(logId, request, student))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task log not found for student");
    }

    @Test
    void updateShouldThrowWhenTargetEnrollmentNotFoundForStudent() {
        UUID studentId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        TaskLog existing = TaskLog.builder().id(logId).build();
        TaskLogRequestDto request = request(enrollmentId, 60);

        when(taskLogRepository.findByIdAndEnrollmentStudentId(logId, studentId)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.findByIdAndStudentId(enrollmentId, studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskLogService.update(logId, request, student))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Enrollment not found for student");
    }

    @Test
    void updateShouldPersistAndReturnUpdatedResponse() {
        UUID studentId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        Student student = studentWithId(studentId);
        Enrollment enrollment = enrollment(enrollmentId, courseId, student);
        TaskLog existing = TaskLog.builder().id(logId).enrollment(enrollment).build();
        TaskLogRequestDto request = request(enrollmentId, 90);

        when(taskLogRepository.findByIdAndEnrollmentStudentId(logId, studentId)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.findByIdAndStudentId(enrollmentId, studentId)).thenReturn(Optional.of(enrollment));
        when(taskLogRepository.save(existing)).thenReturn(existing);

        TaskLogResponseDto response = taskLogService.update(logId, request, student);

        assertThat(response.id()).isEqualTo(logId);
        assertThat(response.enrollmentId()).isEqualTo(enrollmentId);
        assertThat(response.timeSpentMinutes()).isEqualTo(90);
        assertThat(response.description()).isEqualTo("Resolved exercises");
    }

    @Test
    void deleteShouldRemoveTaskLogWhenFound() {
        UUID studentId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        TaskLog existing = TaskLog.builder().id(logId).build();

        when(taskLogRepository.findByIdAndEnrollmentStudentId(logId, studentId)).thenReturn(Optional.of(existing));

        taskLogService.delete(logId, student);

        verify(taskLogRepository).delete(existing);
    }

    @Test
    void deleteShouldThrowWhenTaskLogNotFoundForStudent() {
        UUID studentId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        Student student = studentWithId(studentId);

        when(taskLogRepository.findByIdAndEnrollmentStudentId(logId, studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskLogService.delete(logId, student))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task log not found for student");
    }

    @Test
    void listShouldThrowWhenEndDateIsBeforeStartDate() {
        Student student = studentWithId(UUID.randomUUID());
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 1, 1);

        assertThatThrownBy(() -> taskLogService.list(student, start, end))
                .isInstanceOf(BusinessException.class)
                .hasMessage("endDate must be on or after startDate");
    }

    @Test
    void listShouldUseRangeQueryWhenStartAndEndProvided() {
        UUID studentId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        TaskLog log = taskLog(logId, enrollment(enrollmentId, courseId, student), 60);
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        when(taskLogRepository.findAllByEnrollmentStudentIdAndDateBetween(studentId, start, end))
                .thenReturn(List.of(log));

        List<TaskLogResponseDto> result = taskLogService.list(student, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(logId);
        verify(taskLogRepository).findAllByEnrollmentStudentIdAndDateBetween(studentId, start, end);
        verify(taskLogRepository, never()).findAllByEnrollmentStudentId(studentId);
    }

    @Test
    void listShouldUseDefaultQueryWhenDatesAreNotBothProvided() {
        UUID studentId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        Student student = studentWithId(studentId);
        TaskLog log = taskLog(logId, enrollment(enrollmentId, courseId, student), 60);

        when(taskLogRepository.findAllByEnrollmentStudentId(studentId)).thenReturn(List.of(log));

        List<TaskLogResponseDto> result = taskLogService.list(student, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(logId);
        verify(taskLogRepository).findAllByEnrollmentStudentId(studentId);
    }

    private TaskLogRequestDto request(UUID enrollmentId, int minutes) {
        return new TaskLogRequestDto(
                enrollmentId,
                LocalDate.of(2026, 1, 10),
                TaskCategory.PRATICA,
                "Resolved exercises",
                minutes
        );
    }

    private Student studentWithId(UUID id) {
        Student student = new Student();
        student.setId(id);
        return student;
    }

    private Enrollment enrollment(UUID enrollmentId, UUID courseId, Student student) {
        return Enrollment.builder()
                .id(enrollmentId)
                .student(student)
                .course(Course.builder().id(courseId).name("Java").description("Basics").build())
                .enrollmentDate(LocalDate.of(2026, 1, 1))
                .expectedCompletionDate(LocalDate.of(2026, 7, 1))
                .build();
    }

    private TaskLog taskLog(UUID logId, Enrollment enrollment, int minutes) {
        return TaskLog.builder()
                .id(logId)
                .enrollment(enrollment)
                .date(LocalDate.of(2026, 1, 10))
                .category(TaskCategory.PRATICA)
                .description("Resolved exercises")
                .timeSpentMinutes(minutes)
                .build();
    }
}
