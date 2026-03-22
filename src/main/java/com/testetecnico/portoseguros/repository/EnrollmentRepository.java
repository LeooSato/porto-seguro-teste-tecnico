package com.testetecnico.portoseguros.repository;

import com.testetecnico.portoseguros.entity.Enrollment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    long countByStudentId(UUID studentId);

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<Enrollment> findByStudentId(UUID studentId);

    Optional<Enrollment> findByIdAndStudentId(UUID id, UUID studentId);
}

