package com.testetecnico.portoseguros.controller;

import com.testetecnico.portoseguros.dto.EnrollmentRequestDto;
import com.testetecnico.portoseguros.dto.EnrollmentResponseDto;
import com.testetecnico.portoseguros.service.EnrollmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrollments")
@PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
public class EnrollmentController {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponseDto> enroll(@Valid @RequestBody EnrollmentRequestDto request,
                                                        Authentication authentication) {
        UUID studentId = UUID.fromString(authentication.getName());
        log.info("User {} enrolling in course {}", studentId, request.courseId());
        EnrollmentResponseDto response = enrollmentService.enroll(studentId, request.courseId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentResponseDto>> listMyEnrollments(Authentication authentication) {
        UUID studentId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(enrollmentService.listMyEnrollments(studentId));
    }
}

