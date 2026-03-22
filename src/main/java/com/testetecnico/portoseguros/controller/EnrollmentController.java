package com.testetecnico.portoseguros.controller;

import com.testetecnico.portoseguros.dto.EnrollmentRequestDto;
import com.testetecnico.portoseguros.dto.EnrollmentResponseDto;
import com.testetecnico.portoseguros.entity.Student;
import com.testetecnico.portoseguros.service.EnrollmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrollments")
@PreAuthorize("hasRole('STUDENT')")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponseDto> enroll(@Valid @RequestBody EnrollmentRequestDto request,
                                                        @AuthenticationPrincipal Student student) {
        EnrollmentResponseDto response = enrollmentService.enrollStudent(request.courseId(), student);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentResponseDto>> listMyEnrollments(@AuthenticationPrincipal Student student) {
        return ResponseEntity.ok(enrollmentService.listMyEnrollments(student));
    }
}

