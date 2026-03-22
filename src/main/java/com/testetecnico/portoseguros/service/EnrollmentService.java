package com.testetecnico.portoseguros.service;

import com.testetecnico.portoseguros.dto.EnrollmentResponseDto;
import com.testetecnico.portoseguros.entity.Student;
import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponseDto enrollStudent(UUID courseId, Student student);

    List<EnrollmentResponseDto> listMyEnrollments(Student student);
}

