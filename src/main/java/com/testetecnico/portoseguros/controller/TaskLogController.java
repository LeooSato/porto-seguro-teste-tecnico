package com.testetecnico.portoseguros.controller;

import com.testetecnico.portoseguros.dto.TaskLogRequestDto;
import com.testetecnico.portoseguros.dto.TaskLogResponseDto;
import com.testetecnico.portoseguros.service.TaskLogService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@PreAuthorize("hasRole('STUDENT')")
public class TaskLogController {

    private static final Logger log = LoggerFactory.getLogger(TaskLogController.class);

    private final TaskLogService taskLogService;

    public TaskLogController(TaskLogService taskLogService) {
        this.taskLogService = taskLogService;
    }

    @PostMapping
    public ResponseEntity<TaskLogResponseDto> create(@Valid @RequestBody TaskLogRequestDto request,
                                                     Authentication authentication) {
        UUID studentId = UUID.fromString(authentication.getName());
        log.info("User {} creating task for enrollment {}", studentId, request.enrollmentId());
        TaskLogResponseDto response = taskLogService.create(request, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskLogResponseDto> update(@PathVariable UUID id,
                                                     @Valid @RequestBody TaskLogRequestDto request,
                                                     Authentication authentication) {
        UUID studentId = UUID.fromString(authentication.getName());
        log.info("User {} updating task {}", studentId, id);
        TaskLogResponseDto response = taskLogService.update(id, request, studentId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        UUID studentId = UUID.fromString(authentication.getName());
        log.info("User {} deleting task {}", studentId, id);
        taskLogService.delete(id, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskLogResponseDto>> list(Authentication authentication,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID studentId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(taskLogService.list(studentId, startDate, endDate));
    }
}

