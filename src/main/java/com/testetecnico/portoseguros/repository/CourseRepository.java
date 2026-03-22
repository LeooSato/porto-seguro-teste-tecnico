package com.testetecnico.portoseguros.repository;

import com.testetecnico.portoseguros.entity.Course;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Course> findByNameIgnoreCase(String name);
}

