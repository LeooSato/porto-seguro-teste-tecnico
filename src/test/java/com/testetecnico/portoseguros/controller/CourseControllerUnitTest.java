package com.testetecnico.portoseguros.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.testetecnico.portoseguros.dto.CourseRequestDto;
import com.testetecnico.portoseguros.dto.CourseResponseDto;
import com.testetecnico.portoseguros.service.CourseService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CourseControllerUnitTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    @Test
    void createShouldReturnCreated() {
        CourseRequestDto request = new CourseRequestDto("Java", "Basics");
        CourseResponseDto serviceResponse = new CourseResponseDto(UUID.randomUUID(), "Java", "Basics");
        when(courseService.create(request)).thenReturn(serviceResponse);

        ResponseEntity<CourseResponseDto> response = courseController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void updateShouldReturnOk() {
        UUID id = UUID.randomUUID();
        CourseRequestDto request = new CourseRequestDto("Spring", "Boot");
        CourseResponseDto serviceResponse = new CourseResponseDto(id, "Spring", "Boot");
        when(courseService.update(id, request)).thenReturn(serviceResponse);

        ResponseEntity<CourseResponseDto> response = courseController.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void deleteShouldReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = courseController.delete(id);

        verify(courseService).delete(id);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void listShouldReturnCourses() {
        List<CourseResponseDto> serviceResponse = List.of(
                new CourseResponseDto(UUID.randomUUID(), "Java", "Basics"),
                new CourseResponseDto(UUID.randomUUID(), "Spring", "Boot")
        );
        when(courseService.findAll()).thenReturn(serviceResponse);

        ResponseEntity<List<CourseResponseDto>> response = courseController.list();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void getByIdShouldReturnCourse() {
        UUID id = UUID.randomUUID();
        CourseResponseDto serviceResponse = new CourseResponseDto(id, "Java", "Basics");
        when(courseService.findById(id)).thenReturn(serviceResponse);

        ResponseEntity<CourseResponseDto> response = courseController.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }
}
