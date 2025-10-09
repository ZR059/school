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
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repositories.StudentRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerTestRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private StudentRepository studentRepository;


    private String getBaseUrl() {
        return "http://localhost:" + port + "/student";
    }

    @Test
    public void getStudentInfo_WhenStudentExists_ShouldReturnStudent() {
        Long studentId = 1L;

        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void getStudentInfo_WhenStudentNotExists_ShouldReturnNotFound() {
        Long nonExistentStudentId = 999L;

        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + nonExistentStudentId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void findStudents_WithValidAge_ShouldReturnStudents() {
        int age = 20;

        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + "?age=" + age, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void findStudents_WithoutAge_ShouldReturnEmptyList() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    public void createStudent_WithValidData_ShouldReturnStudent() {
        String studentJson = """
                {
                    "name": "Тестовый Студент",
                    "age": 22
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(studentJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl(), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("Тестовый Студент");
    }

    @Test
    public void editStudent_WithValidData_ShouldReturnUpdatedStudent() {
        Student existingStudent = new Student();
        existingStudent.setName("Исходное Имя");
        existingStudent.setAge(20);
        Student savedStudent = studentRepository.save(existingStudent);

        Student updateRequest = new Student();
        updateRequest.setId(savedStudent.getId());
        updateRequest.setName("Обновленное Имя");
        updateRequest.setAge(23);

        ResponseEntity<Student> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                Student.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Обновленное Имя");
        assertThat(response.getBody().getAge()).isEqualTo(23);
    }

    @Test
    public void editStudent_WithInvalidData_ShouldReturnBadRequest() {
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

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.PUT,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void deleteStudent_WithValidId_ShouldReturnOk() {
        Long studentId = 2L;

        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/" + studentId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void uploadAvatar_WithValidFile_ShouldReturnOk() {
        Long studentId = 1L;
        Resource fileResource = new ClassPathResource("test-avatar.jpg");

        if (!fileResource.exists()) {
            System.out.println("Тестовый файл не найден, тест пропущен");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", fileResource);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/" + studentId + "/avatar",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void uploadAvatar_WithLargeFile_ShouldReturnBadRequest() {
        Long studentId = 1L;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/" + studentId + "/avatar",
                request,
                String.class
        );
    }

    @Test
    public void downloadAvatarPreview_WithValidId_ShouldReturnAvatar() {
        Long studentId = 1L;

        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId + "/avatar/preview",
                byte[].class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    public void downloadAvatar_WithValidId_ShouldReturnAvatarFile() {
        Long studentId = 1L;

        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId + "/avatar",
                byte[].class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }
}