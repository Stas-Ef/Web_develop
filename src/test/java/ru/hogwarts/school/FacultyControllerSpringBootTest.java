package ru.hogwarts.school;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repositories.FacultyRepository;
import ru.hogwarts.school.repositories.StudentRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class FacultyControllerSpringBootTest {


    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void cleanDb() {
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    public void testGetFacultyInfo_Success() {
        Faculty faculty = facultyRepository.save(new Faculty("Slytherin", "Green"));

        ResponseEntity<Faculty> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/faculty/" + faculty.getId(),
                        Faculty.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testCreateFaculty() {
        Faculty faculty = new Faculty("Ravenclaw", "Blue");

        ResponseEntity<Faculty> response =
                restTemplate.postForEntity(
                        "http://localhost:" + port + "/faculty",
                        faculty,
                        Faculty.class
                );

        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    public void findFacultyByColorOrName() {
        facultyRepository.save(new Faculty("Hufflepuff", "Yellow"));

        ResponseEntity<Faculty[]> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/faculty/search-color-name?searchTerm=yellow",
                        Faculty[].class
                );

        assertEquals(1, response.getBody().length);
    }

    @Test
    public void getStudentsByFaculty() {
        Faculty faculty = facultyRepository.save(new Faculty("Griffindor", "Red"));

        Student student = new Student("Harry", 15);
        student.setFaculty(faculty);
        studentRepository.save(student);

        ResponseEntity<Student[]> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/faculty/" + faculty.getId() + "/students",
                        Student[].class
                );

        assertEquals(1, response.getBody().length);
    }
}

