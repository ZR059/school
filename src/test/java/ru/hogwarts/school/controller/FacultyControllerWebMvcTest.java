package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
public class FacultyControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacultyService facultyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetFacultyById() throws Exception {
        // Тест для GET /faculty/{id}
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Red");

        when(facultyService.findFaculty(1L)).thenReturn(faculty);

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gryffindor"));
    }

    @Test
    void shouldCreateFaculty() throws Exception {
        // Тест для POST /faculty
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Slytherin");
        faculty.setColor("Green");

        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(faculty);

        mockMvc.perform(MockMvcRequestBuilders.post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldEditFaculty() throws Exception {
        // Тест для PUT /faculty
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Hufflepuff");
        faculty.setColor("Yellow");

        when(facultyService.editFaculty(any(Faculty.class))).thenReturn(faculty);

        mockMvc.perform(MockMvcRequestBuilders.put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hufflepuff"));
    }

    @Test
    void shouldDeleteFaculty() throws Exception {
        // Тест для DELETE /faculty/{id}
        mockMvc.perform(MockMvcRequestBuilders.delete("/faculty/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetFacultiesByColor() throws Exception {
        // Тест для GET /faculty/color/{color}
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Test Faculty");
        faculty.setColor("Blue");

        when(facultyService.getFacultiesByColor("Blue")).thenReturn(List.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/color/Blue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].color").value("Blue"));
    }

    @Test
    void shouldFindFacultiesByNameOrColor() throws Exception {
        // Тест для GET /faculty/search с использованием param
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Red");

        when(facultyService.findFacultiesByNameOrColor("Gryffindor")).thenReturn(List.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/search")
                        .param("nameOrColor", "Gryffindor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gryffindor"));
    }

    @Test
    void shouldGetAllFaculties() throws Exception {
        // Тест для GET /faculty
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Test Faculty");
        faculty.setColor("Test Color");

        when(facultyService.getAllFaculties()).thenReturn(List.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldGetFacultyStudents() throws Exception {
        // Тест для GET /faculty/{id}/students
        Student student = new Student();
        student.setId(1L);
        student.setName("Harry Potter");
        student.setAge(17);

        when(facultyService.getFacultyStudents(1L)).thenReturn(List.of(student));

        mockMvc.perform(MockMvcRequestBuilders.get("/faculty/{id}/students", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Harry Potter"));
    }
}
