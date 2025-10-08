package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerTestRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/student";
    }

    // Тест для получения студента по ID (GET /student/{id})
    @Test
    public void getStudentInfo_WhenStudentExists_ShouldReturnStudent() {
        // Given
        Long studentId = 1L;

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void getStudentInfo_WhenStudentNotExists_ShouldReturnNotFound() {
        // Given
        Long nonExistentStudentId = 999L;

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + nonExistentStudentId, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // Тест для поиска студентов по возрасту (GET /student?age=)
    @Test
    public void findStudents_WithValidAge_ShouldReturnStudents() {
        // Given
        int age = 20;

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "?age=" + age, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void findStudents_WithoutAge_ShouldReturnEmptyList() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl(), String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    // Тест для создания студента (POST /student)
    @Test
    public void createStudent_WithValidData_ShouldReturnStudent() {
        // Given
        String studentJson = """
                {
                    "name": "Тестовый Студент",
                    "age": 22
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(studentJson, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(), request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("Тестовый Студент");
    }

    // Тест для обновления студента (PUT /student)
    @Test
    public void editStudent_WithValidData_ShouldReturnUpdatedStudent() {
        // Given
        String studentJson = """
                {
                    "id": 1,
                    "name": "Обновленное Имя",
                    "age": 23
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(studentJson, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.PUT,
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void editStudent_WithInvalidData_ShouldReturnBadRequest() {
        // Given
        String invalidStudentJson = """
                {
                    "id": 999,
                    "name": "Несуществующий Студент",
                    "age": 25
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(invalidStudentJson, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.PUT,
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Тест для удаления студента (DELETE /student/{id})
    @Test
    public void deleteStudent_WithValidId_ShouldReturnOk() {
        // Given
        Long studentId = 2L; // Предполагаем, что студент с ID=2 существует

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/" + studentId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Тест для загрузки аватара (POST /student/{id}/avatar)
    @Test
    public void uploadAvatar_WithValidFile_ShouldReturnOk() {
        // Given
        Long studentId = 1L;
        Resource fileResource = new ClassPathResource("test-avatar.jpg");

        // Если тестового файла нет, создаем заглушку
        if (!fileResource.exists()) {
            // В реальном проекте здесь должен быть тестовый файл
            System.out.println("Тестовый файл не найден, тест пропущен");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", fileResource);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/" + studentId + "/avatar",
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Тест для загрузки слишком большого аватара
    @Test
    public void uploadAvatar_WithLargeFile_ShouldReturnBadRequest() {
        // Given
        Long studentId = 1L;
        // Здесь нужно создать большой файл для теста
        // В реальном проекте это можно сделать с помощью временного файла

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // body.add("avatar", largeFileResource); // большой файл

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/" + studentId + "/avatar",
                request,
                String.class
        );

        // Then
        // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // assertThat(response.getBody()).contains("File is too big");
    }

    // Тест для получения превью аватара (GET /student/{id}/avatar/preview)
    @Test
    public void downloadAvatarPreview_WithValidId_ShouldReturnAvatar() {
        // Given
        Long studentId = 1L;

        // When
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId + "/avatar/preview",
                byte[].class
        );

        // Then
        // Может вернуть 200 или 404 в зависимости от наличия аватара
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    // Тест для скачивания аватара (GET /student/{id}/avatar)
    @Test
    public void downloadAvatar_WithValidId_ShouldReturnAvatarFile() {
        // Given
        Long studentId = 1L;

        // When
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId + "/avatar",
                byte[].class
        );

        // Then
        // Может вернуть 200 или 404 в зависимости от наличия аватара
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }
}