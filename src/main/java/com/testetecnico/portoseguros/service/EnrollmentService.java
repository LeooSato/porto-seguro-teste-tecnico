package com.testetecnico.portoseguros.service;

import com.testetecnico.portoseguros.dto.EnrollmentResponseDto;
import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponseDto enroll(UUID studentId, UUID courseId);

    List<EnrollmentResponseDto> listMyEnrollments(UUID studentId);
}

