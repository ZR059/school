package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
public class StudentControllerWebMvcTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long NON_EXISTENT_STUDENT_ID = 999L;
    private static final int STUDENT_AGE = 20;
    private static final String STUDENT_NAME = "Иван Иванов";
    private static final String STUDENT_EMAIL = "ivan@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private Student createTestStudent() {
        Student student = new Student();
        student.setId(STUDENT_ID);
        student.setName(STUDENT_NAME);
        student.setAge(STUDENT_AGE);
        return student;
    }

    private Avatar createTestAvatar() {
        Avatar avatar = new Avatar();
        avatar.setId(STUDENT_ID);
        avatar.setMediaType("image/jpeg");
        avatar.setData(new byte[]{1, 2, 3});
        avatar.setFileSize(1024L);
        avatar.setFilePath("/path/to/avatar.jpg");
        return avatar;
    }

    @Test
    public void getStudentInfoWhenStudentExistsShouldReturnStudent() throws Exception {
        Student student = createTestStudent();
        when(studentService.findStudent(STUDENT_ID)).thenReturn(student);

        mockMvc.perform(get("/student/{id}", STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(STUDENT_ID))
                .andExpect(jsonPath("$.name").value(STUDENT_NAME))
                .andExpect(jsonPath("$.age").value(STUDENT_AGE));
    }

    @Test
    public void getStudentInfoWhenStudentNotExistsShouldReturnNotFound() throws Exception {
        when(studentService.findStudent(NON_EXISTENT_STUDENT_ID)).thenReturn(null);

        mockMvc.perform(get("/student/{id}", NON_EXISTENT_STUDENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void findStudentsWithValidAgeShouldReturnStudentsList() throws Exception {
        Student student = createTestStudent();
        List<Student> students = List.of(student);
        when(studentService.findByAge(STUDENT_AGE)).thenReturn(students);

        mockMvc.perform(get("/student")
                        .param("age", String.valueOf(STUDENT_AGE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(STUDENT_ID))
                .andExpect(jsonPath("$[0].name").value(STUDENT_NAME))
                .andExpect(jsonPath("$[0].age").value(STUDENT_AGE));
    }

    @Test
    public void findStudentsWithoutAgeShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void createStudentWithValidDataShouldReturnStudent() throws Exception {
        Student student = createTestStudent();
        when(studentService.addStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(STUDENT_ID))
                .andExpect(jsonPath("$.name").value(STUDENT_NAME))
                .andExpect(jsonPath("$.age").value(STUDENT_AGE));
    }

    @Test
    public void editStudentWithValidDataShouldReturnUpdatedStudent() throws Exception {
        Student student = createTestStudent();
        when(studentService.editStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(STUDENT_ID))
                .andExpect(jsonPath("$.name").value(STUDENT_NAME));
    }

    @Test
    public void editStudentWithInvalidDataShouldReturnBadRequest() throws Exception {
        Student invalidStudent = createTestStudent();
        invalidStudent.setId(NON_EXISTENT_STUDENT_ID);
        when(studentService.editStudent(any(Student.class))).thenReturn(null);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStudent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteStudentWithValidIdShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/student/{id}", STUDENT_ID))
                .andExpect(status().isOk());
    }

    @Test
    public void uploadAvatarWithValidFileShouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/student/{id}/avatar", STUDENT_ID)
                        .file(file))
                .andExpect(status().isOk());
    }

    @Test
    public void downloadAvatarPreviewWithValidIdShouldReturnAvatarData() throws Exception {
        Avatar avatar = createTestAvatar();
        when(studentService.findAvatar(STUDENT_ID)).thenReturn(avatar);

        mockMvc.perform(get("/student/{id}/avatar/preview", STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("image/jpeg")))
                .andExpect(content().bytes(avatar.getData()));
    }

    @Test
    void downloadAvatarFullFileWithValidIdShouldReturnAvatarFile() throws Exception {
        // Given
        Long studentId = 1L;

        Path tempFile = Files.createTempFile("avatar", ".jpg");
        byte[] testData = new byte[]{1, 2, 3, 4, 5};
        Files.write(tempFile, testData);

        try {
            Avatar mockAvatar = new Avatar();
            mockAvatar.setId(1L);
            mockAvatar.setMediaType("image/jpeg");
            mockAvatar.setFilePath(tempFile.toAbsolutePath().toString());
            mockAvatar.setFileSize((long) testData.length);


            when(studentService.findAvatar(studentId)).thenReturn(mockAvatar);

            mockMvc.perform(get("/student/{id}/avatar", studentId))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                    .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, testData.length))
                    .andExpect(content().bytes(testData));

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void downloadAvatarPreviewWithValidIdShouldReturnAvatar() throws Exception {
        // Given
        Long studentId = 1L;

        // Создаем mock аватара
        Avatar mockAvatar = new Avatar();
        mockAvatar.setId(1L);
        mockAvatar.setMediaType("image/jpeg");
        mockAvatar.setData(new byte[]{1, 2, 3, 4, 5});
        mockAvatar.setFileSize(5L);

        // Настраиваем моки
        when(studentService.findAvatar(studentId)).thenReturn(mockAvatar);

        // When & Then
        mockMvc.perform(get("/student/{id}/avatar/preview", studentId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 5))
                .andExpect(content().bytes(new byte[]{1, 2, 3, 4, 5}));
    }

    @Test
    public void uploadAvatarWithLargeFileShouldReturnBadRequest() throws Exception {
        byte[] largeFileContent = new byte[1024 * 400]; // 400KB > 300KB limit
        MockMultipartFile largeFile = new MockMultipartFile(
                "avatar",
                "large.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeFileContent
        );

        mockMvc.perform(multipart("/student/{id}/avatar", STUDENT_ID)
                        .file(largeFile))
                .andExpect(status().isBadRequest());
    }
}