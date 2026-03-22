package com.testetecnico.portoseguros.repository;

import com.testetecnico.portoseguros.entity.TaskLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskLogRepository extends JpaRepository<TaskLog, UUID> {

    Optional<TaskLog> findByIdAndEnrollmentStudentId(UUID id, UUID studentId);

    List<TaskLog> findAllByEnrollmentStudentId(UUID studentId);

    List<TaskLog> findAllByEnrollmentStudentIdAndDateBetween(UUID studentId, LocalDate start, LocalDate end);
}

