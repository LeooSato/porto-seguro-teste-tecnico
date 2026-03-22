package com.testetecnico.portoseguros.service.impl;

import com.testetecnico.portoseguros.dto.CourseRequestDto;
import com.testetecnico.portoseguros.dto.CourseResponseDto;
import com.testetecnico.portoseguros.entity.Course;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.exception.ResourceNotFoundException;
import com.testetecnico.portoseguros.repository.CourseRepository;
import com.testetecnico.portoseguros.service.CourseService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public CourseResponseDto create(CourseRequestDto request) {
        validateUniqueName(request.name(), null);
        Course course = Course.builder()
                .name(request.name().trim())
                .description(request.description().trim())
                .build();
        Course saved = courseRepository.save(course);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CourseResponseDto update(UUID id, CourseRequestDto request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        validateUniqueName(request.name(), id);
        course.setName(request.name().trim());
        course.setDescription(request.description().trim());

        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        courseRepository.delete(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponseDto> findAll() {
        return courseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDto findById(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        return toResponse(course);
    }

    private void validateUniqueName(String name, UUID currentId) {
        boolean exists = courseRepository.findByNameIgnoreCase(name).map(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                return true;
            }
            return false;
        }).orElse(false);

        if (exists) {
            throw new BusinessException("Course name must be unique");
        }
    }

    private CourseResponseDto toResponse(Course course) {
        return new CourseResponseDto(course.getId(), course.getName(), course.getDescription());
    }
}

