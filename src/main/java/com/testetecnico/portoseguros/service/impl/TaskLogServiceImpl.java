package com.testetecnico.portoseguros.service.impl;

import com.testetecnico.portoseguros.dto.TaskLogRequestDto;
import com.testetecnico.portoseguros.dto.TaskLogResponseDto;
import com.testetecnico.portoseguros.entity.Enrollment;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.entity.TaskLog;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.exception.ResourceNotFoundException;
import com.testetecnico.portoseguros.repository.EnrollmentRepository;
import com.testetecnico.portoseguros.repository.TaskLogRepository;
import com.testetecnico.portoseguros.service.TaskLogService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskLogServiceImpl implements TaskLogService {

    private static final int TIME_INCREMENT_MINUTES = 30;

    private final TaskLogRepository taskLogRepository;
    private final EnrollmentRepository enrollmentRepository;

    public TaskLogServiceImpl(TaskLogRepository taskLogRepository, EnrollmentRepository enrollmentRepository) {
        this.taskLogRepository = taskLogRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional
    public TaskLogResponseDto create(TaskLogRequestDto request, Student student) {
        validateTime(request.timeSpentMinutes());
        Enrollment enrollment = enrollmentRepository.findByIdAndStudentId(request.enrollmentId(), student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for student"));

        TaskLog log = TaskLog.builder()
                .enrollment(enrollment)
                .date(request.date())
                .category(request.category())
                .description(request.description())
                .timeSpentMinutes(request.timeSpentMinutes())
                .build();

        return toResponse(taskLogRepository.save(log));
    }

    @Override
    @Transactional
    public TaskLogResponseDto update(UUID id, TaskLogRequestDto request, Student student) {
        validateTime(request.timeSpentMinutes());
        TaskLog existing = taskLogRepository.findByIdAndEnrollmentStudentId(id, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task log not found for student"));

        Enrollment enrollment = enrollmentRepository.findByIdAndStudentId(request.enrollmentId(), student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for student"));

        existing.setEnrollment(enrollment);
        existing.setDate(request.date());
        existing.setCategory(request.category());
        existing.setDescription(request.description());
        existing.setTimeSpentMinutes(request.timeSpentMinutes());

        return toResponse(taskLogRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(UUID id, Student student) {
        TaskLog existing = taskLogRepository.findByIdAndEnrollmentStudentId(id, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task log not found for student"));
        taskLogRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskLogResponseDto> list(Student student, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("endDate must be on or after startDate");
        }

        List<TaskLog> logs;
        if (startDate != null && endDate != null) {
            logs = taskLogRepository.findAllByEnrollmentStudentIdAndDateBetween(student.getId(), startDate, endDate);
        } else {
            logs = taskLogRepository.findAllByEnrollmentStudentId(student.getId());
        }

        return logs.stream().map(this::toResponse).toList();
    }

    private void validateTime(Integer minutes) {
        if (minutes == null || minutes <= 0 || minutes % TIME_INCREMENT_MINUTES != 0) {
            throw new BusinessException("Time spent must be positive and in 30-minute increments");
        }
    }

    private TaskLogResponseDto toResponse(TaskLog log) {
        return new TaskLogResponseDto(
                log.getId(),
                log.getEnrollment().getId(),
                log.getEnrollment().getCourse().getId(),
                log.getCategory(),
                log.getDate(),
                log.getDescription(),
                log.getTimeSpentMinutes()
        );
    }
}

