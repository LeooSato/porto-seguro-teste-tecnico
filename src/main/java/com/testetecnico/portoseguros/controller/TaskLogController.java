package com.testetecnico.portoseguros.controller;

import com.testetecnico.portoseguros.dto.TaskLogRequestDto;
import com.testetecnico.portoseguros.dto.TaskLogResponseDto;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.service.TaskLogService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    private final TaskLogService taskLogService;

    public TaskLogController(TaskLogService taskLogService) {
        this.taskLogService = taskLogService;
    }

    @PostMapping
    public ResponseEntity<TaskLogResponseDto> create(@Valid @RequestBody TaskLogRequestDto request,
                                                     @AuthenticationPrincipal Student student) {
        TaskLogResponseDto response = taskLogService.create(request, student);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskLogResponseDto> update(@PathVariable UUID id,
                                                     @Valid @RequestBody TaskLogRequestDto request,
                                                     @AuthenticationPrincipal Student student) {
        TaskLogResponseDto response = taskLogService.update(id, request, student);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Student student) {
        taskLogService.delete(id, student);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskLogResponseDto>> list(@AuthenticationPrincipal Student student,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(taskLogService.list(student, startDate, endDate));
    }
}

