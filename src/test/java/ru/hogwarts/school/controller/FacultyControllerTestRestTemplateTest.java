package ru.hogwarts.school.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import ru.hogwarts.school.model.Faculty;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FacultyControllerTestRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/faculty";
    }

    @Test
    @Order(1)
    void shouldCreateFaculty() {
        Faculty faculty = new Faculty();
        faculty.setName("Gryffindor");
        faculty.setColor("Red");

        ResponseEntity<Faculty> response = restTemplate.postForEntity(
                baseUrl,
                faculty,
                Faculty.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Gryffindor");
        assertThat(response.getBody().getColor()).isEqualTo("Red");
    }

    @Test
    @Order(2)
    void shouldGetFacultyById() {

        Faculty faculty = new Faculty();
        faculty.setName("Slytherin");
        faculty.setColor("Green");

        Faculty createdFaculty = restTemplate.postForObject(baseUrl, faculty, Faculty.class);
        Long facultyId = createdFaculty.getId();

        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/" + facultyId,
                Faculty.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(facultyId);
        assertThat(response.getBody().getName()).isEqualTo("Slytherin");
    }

    @Test
    @Order(3)
    void shouldEditFaculty() {

        Faculty faculty = new Faculty();
        faculty.setName("Hufflepuff");
        faculty.setColor("Yellow");
        Faculty createdFaculty = restTemplate.postForObject(baseUrl, faculty, Faculty.class);

        Faculty updatedFaculty = new Faculty();
        updatedFaculty.setId(createdFaculty.getId());
        updatedFaculty.setName("Updated Hufflepuff");
        updatedFaculty.setColor("Gold");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Faculty> request = new HttpEntity<>(updatedFaculty, headers);

        ResponseEntity<Faculty> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                Faculty.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Hufflepuff");
        assertThat(response.getBody().getColor()).isEqualTo("Gold");
    }

    @Test
    @Order(4)
    void shouldDeleteFaculty() {
        Faculty faculty = new Faculty();
        faculty.setName("Ravenclaw");
        faculty.setColor("Blue");
        Faculty createdFaculty = restTemplate.postForObject(baseUrl, faculty, Faculty.class);
        Long facultyId = createdFaculty.getId();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/" + facultyId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Faculty> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + facultyId,
                Faculty.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(5)
    void shouldGetFacultiesByColor() {
        Faculty faculty1 = new Faculty();
        faculty1.setName("Blue House 1");
        faculty1.setColor("Blue");
        restTemplate.postForObject(baseUrl, faculty1, Faculty.class);

        Faculty faculty2 = new Faculty();
        faculty2.setName("Blue House 2");
        faculty2.setColor("Blue");
        restTemplate.postForObject(baseUrl, faculty2, Faculty.class);

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(
                baseUrl + "/color/Blue",
                Faculty[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(6)
    void shouldFindFacultiesByNameOrColor() {
        Faculty faculty = new Faculty();
        faculty.setName("UniqueHouseName");
        faculty.setColor("UniqueColor");
        restTemplate.postForObject(baseUrl, faculty, Faculty.class);

        ResponseEntity<Faculty[]> responseByName = restTemplate.getForEntity(
                baseUrl + "/search?nameOrColor=UniqueHouseName",
                Faculty[].class
        );

        ResponseEntity<Faculty[]> responseByColor = restTemplate.getForEntity(
                baseUrl + "/search?nameOrColor=UniqueColor",
                Faculty[].class
        );

        assertThat(responseByName.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseByName.getBody()).isNotEmpty();
        assertThat(responseByName.getBody()[0].getName()).isEqualTo("UniqueHouseName");

        assertThat(responseByColor.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseByColor.getBody()).isNotEmpty();
        assertThat(responseByColor.getBody()[0].getColor()).isEqualTo("UniqueColor");
    }

    @Test
    @Order(7)
    void shouldGetAllFaculties() {
        Faculty faculty1 = new Faculty();
        faculty1.setName("Test Faculty 1");
        faculty1.setColor("Color 1");
        restTemplate.postForObject(baseUrl, faculty1, Faculty.class);

        Faculty faculty2 = new Faculty();
        faculty2.setName("Test Faculty 2");
        faculty2.setColor("Color 2");
        restTemplate.postForObject(baseUrl, faculty2, Faculty.class);

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(baseUrl, Faculty[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(8)
    void shouldGetFacultyStudents() {
        Faculty faculty = new Faculty();
        faculty.setName("Faculty With Students");
        faculty.setColor("Test Color");
        Faculty createdFaculty = restTemplate.postForObject(baseUrl, faculty, Faculty.class);

        ResponseEntity<Object[]> response = restTemplate.getForEntity(
                baseUrl + "/" + createdFaculty.getId() + "/students",
                Object[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}