package com.testetecnico.portoseguros.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.TaskLogRequestDto;
import com.testetecnico.portoseguros.dto.TaskLogResponseDto;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.entity.TaskCategory;
import com.testetecnico.portoseguros.service.TaskLogService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TaskLogControllerUnitTest {

    @Mock
    private TaskLogService taskLogService;

    @InjectMocks
    private TaskLogController taskLogController;

    @Test
    void createShouldReturnCreated() {
        Student student = student();
        TaskLogRequestDto request = taskRequest();
        TaskLogResponseDto serviceResponse = taskResponse(request.enrollmentId());
        when(taskLogService.create(request, student)).thenReturn(serviceResponse);

        ResponseEntity<TaskLogResponseDto> response = taskLogController.create(request, student);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void updateShouldReturnOk() {
        Student student = student();
        UUID id = UUID.randomUUID();
        TaskLogRequestDto request = taskRequest();
        TaskLogResponseDto serviceResponse = taskResponse(request.enrollmentId());
        when(taskLogService.update(id, request, student)).thenReturn(serviceResponse);

        ResponseEntity<TaskLogResponseDto> response = taskLogController.update(id, request, student);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void deleteShouldReturnNoContent() {
        Student student = student();
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = taskLogController.delete(id, student);

        verify(taskLogService).delete(id, student);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void listShouldReturnOkWithServiceResult() {
        Student student = student();
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        UUID enrollmentId = UUID.randomUUID();
        List<TaskLogResponseDto> serviceResponse = List.of(taskResponse(enrollmentId));
        when(taskLogService.list(student, start, end)).thenReturn(serviceResponse);

        ResponseEntity<List<TaskLogResponseDto>> response = taskLogController.list(student, start, end);

        verify(taskLogService).list(student, start, end);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    private Student student() {
        Student student = new Student();
        student.setId(UUID.randomUUID());
        return student;
    }

    private TaskLogRequestDto taskRequest() {
        return new TaskLogRequestDto(
                UUID.randomUUID(),
                LocalDate.of(2026, 1, 10),
                TaskCategory.PRATICA,
                "Resolved exercises",
                60
        );
    }

    private TaskLogResponseDto taskResponse(UUID enrollmentId) {
        return new TaskLogResponseDto(
                UUID.randomUUID(),
                enrollmentId,
                UUID.randomUUID(),
                TaskCategory.PRATICA,
                LocalDate.of(2026, 1, 10),
                "Resolved exercises",
                60
        );
    }
}
