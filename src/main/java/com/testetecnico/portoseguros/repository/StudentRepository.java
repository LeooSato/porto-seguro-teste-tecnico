package com.testetecnico.portoseguros.repository;

import com.testetecnico.portoseguros.entity.Student;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);
}


