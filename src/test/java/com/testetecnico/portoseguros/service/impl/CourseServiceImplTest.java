package com.testetecnico.portoseguros.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.CourseRequestDto;
import com.testetecnico.portoseguros.dto.CourseResponseDto;
import com.testetecnico.portoseguros.entity.Course;
import com.testetecnico.portoseguros.exception.BusinessException;
import com.testetecnico.portoseguros.exception.ResourceNotFoundException;
import com.testetecnico.portoseguros.repository.CourseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void createShouldTrimFieldsAndReturnResponse() {
        CourseRequestDto request = new CourseRequestDto("  Java  ", "  Spring basics  ");
        UUID id = UUID.randomUUID();

        when(courseRepository.findByNameIgnoreCase(request.name())).thenReturn(Optional.empty());
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(id);
            return course;
        });

        CourseResponseDto response = courseService.create(request);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Java");
        assertThat(captor.getValue().getDescription()).isEqualTo("Spring basics");
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Java");
        assertThat(response.description()).isEqualTo("Spring basics");
    }

    @Test
    void createShouldThrowWhenNameAlreadyExists() {
        CourseRequestDto request = new CourseRequestDto("Java", "Spring");
        Course existing = Course.builder().id(UUID.randomUUID()).name("Java").description("X").build();
        when(courseRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> courseService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Course name must be unique");

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateShouldThrowWhenCourseDoesNotExist() {
        UUID courseId = UUID.randomUUID();
        CourseRequestDto request = new CourseRequestDto("Java", "Spring");
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.update(courseId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course not found");
    }

    @Test
    void updateShouldAllowSameNameForCurrentCourse() {
        UUID courseId = UUID.randomUUID();
        CourseRequestDto request = new CourseRequestDto("  Java  ", "  Updated  ");
        Course current = Course.builder().id(courseId).name("Java").description("Old").build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(current));
        when(courseRepository.findByNameIgnoreCase(request.name())).thenReturn(Optional.of(current));
        when(courseRepository.save(current)).thenReturn(current);

        CourseResponseDto response = courseService.update(courseId, request);

        assertThat(response.id()).isEqualTo(courseId);
        assertThat(response.name()).isEqualTo("Java");
        assertThat(response.description()).isEqualTo("Updated");
    }

    @Test
    void updateShouldThrowWhenNameBelongsToAnotherCourse() {
        UUID courseId = UUID.randomUUID();
        CourseRequestDto request = new CourseRequestDto("Java", "Updated");

        Course current = Course.builder().id(courseId).name("Current").description("Old").build();
        Course existing = Course.builder().id(UUID.randomUUID()).name("Java").description("Existing").build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(current));
        when(courseRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> courseService.update(courseId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Course name must be unique");

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteShouldRemoveExistingCourse() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().id(courseId).name("Java").description("Spring").build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseService.delete(courseId);

        verify(courseRepository).delete(course);
    }

    @Test
    void deleteShouldThrowWhenCourseDoesNotExist() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.delete(courseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course not found");
    }

    @Test
    void findAllShouldMapCoursesToResponse() {
        Course first = Course.builder()
                .id(UUID.randomUUID())
                .name("Java")
                .description("Basics")
                .build();
        Course second = Course.builder()
                .id(UUID.randomUUID())
                .name("Spring")
                .description("Boot")
                .build();
        when(courseRepository.findAll()).thenReturn(List.of(first, second));

        List<CourseResponseDto> result = courseService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Java");
        assertThat(result.get(1).name()).isEqualTo("Spring");
    }

    @Test
    void findByIdShouldReturnMappedResponse() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().id(courseId).name("Java").description("Basics").build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        CourseResponseDto response = courseService.findById(courseId);

        assertThat(response.id()).isEqualTo(courseId);
        assertThat(response.name()).isEqualTo("Java");
        assertThat(response.description()).isEqualTo("Basics");
    }

    @Test
    void findByIdShouldThrowWhenCourseMissing() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(courseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course not found");
    }
}
