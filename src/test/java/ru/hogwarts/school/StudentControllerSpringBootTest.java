
package ru.hogwarts.school;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repositories.AvatarRepository;
import ru.hogwarts.school.repositories.FacultyRepository;
import ru.hogwarts.school.repositories.StudentRepository;
import ru.hogwarts.school.service.StudentService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StudentControllerSpringBootTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;


    @Autowired
    private AvatarRepository avatarRepository;
    @Value("${student.avatar.dir.path}")
    private String avatarDir;

    @Autowired
    private TestRestTemplate restTemplate;

    private Student student;

    @BeforeEach
    public void setUp() {
        avatarRepository.deleteAll();
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
        student = studentRepository.save(new Student("Колян", 17));

    }

    @AfterEach
    void cleanFiles() throws IOException {
        Files.list(Path.of(avatarDir))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
    }

    @Test
    public void testGetStudentInfo_Success() {

        ResponseEntity<Student> response = restTemplate
                .getForEntity("http://localhost:" + port + "/student/" + student.getId(), Student.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getId(), student.getId());
        assertEquals(response.getBody().getName(), student.getName());
        assertEquals(response.getBody().getName(), student.getName());
        assertEquals(response.getBody().getAge(), student.getAge());
    }

    @Test
    public void testGetStudentInfo_NotFound() {
        ResponseEntity<Student> response = restTemplate.getForEntity("http://localhost:" + port + "/student/999", Student.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testCreateStudent() {
        ResponseEntity<Student> response = restTemplate.postForEntity("http://localhost:" + port + "/student", student, Student.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getId(), student.getId());
        assertThat(response.getBody().getName()).isEqualTo(student.getName());
    }


    @Test
    public void testEditStudent_Success() {

        ResponseEntity<Student> response = restTemplate.exchange("http://localhost:" + port + "/student", HttpMethod.PUT, new HttpEntity<>(student), Student.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getId(), student.getId());
        assertThat(response.getBody().getName()).isEqualTo(student.getName());
    }

    @Test
    public void testEditStudent_Failure() {
        Student student = new Student("Unknown", 20);
        student.setId(999L);

        ResponseEntity<Student> response = restTemplate.exchange("http://localhost:" + port + "/student", HttpMethod.PUT, new HttpEntity<>(student), Student.class);
        System.out.println("response.getStatusCode() = " + response.getStatusCode());
        System.out.println("student = " + student);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    public void testDeleteStudent() {

        ResponseEntity<Void> response = restTemplate
                .exchange("http://localhost:" + port + "/student/" + student.getId(), HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }


    @Test
    public void testFindStudentsByAge() {
        Student s1 = new Student("Harry", student.getAge());
        Student s2 = new Student("Hermione", student.getAge());
        studentRepository.save(s1);
        studentRepository.save(s2);

        ResponseEntity<Student[]> response = restTemplate.getForEntity("http://localhost:" + port + "/student/by-age?age=" + student.getAge(), Student[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
    }

    @Test
    public void testFindStudentsByAge_Negative() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity("http://localhost:" + port + "/student/by-age?age=-5", Student[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEmpty();
    }


    @Test
    public void testFindStudentsBetweenAge() {
        Student s1 = new Student("Luna", 20);
        studentRepository.save(s1);

        ResponseEntity<Student[]> response = restTemplate.getForEntity("http://localhost:" + port + "/student/by-age-range?ageMin=18&ageMax=25", Student[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    public void testFindStudentsBetweenAge_Invalid() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity("/student/by-age-range?ageMin=30&ageMax=10", Student[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEmpty();
    }


    @Test
    public void testGetFacultyByStudent() {
        Faculty faculty = facultyRepository.save(new Faculty("МИИТ", "Зеленый"));

        student.setFaculty(faculty);
        student = studentRepository.save(student);

        ResponseEntity<Faculty> response = restTemplate.getForEntity("http://localhost:" + port
                + "/student/" + student.getId() + "/faculty", Faculty.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("МИИТ");
    }


    @Test
    public void testUploadAvatar_Success() throws IOException {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);

        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray()) {
            @Override
            public String getFilename() {
                return "avatar.png";
            }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port
                + "/student/" + student.getId() + "/avatar", requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUploadAvatar_TooBigFile() throws Exception {
        byte[] largeData = new byte[1024 * 400];
        ByteArrayResource resource = new ByteArrayResource(largeData) {
            @Override
            public String getFilename() {
                return "avatar.png";
            }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:"
                + port + "/student/1/avatar", requestEntity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Files is too big"));
    }

    @Test
    public void testDownloadAvatarPreview() throws IOException {
        byte[] preview = new byte[]{1, 2, 3, 4, 5};

        Avatar avatar = new Avatar();
        avatar.setStudent(student);
        avatar.setMediaType("image/png");
        avatar.setPreview(preview);
        avatar.setFileSize((long) preview.length);
        avatarRepository.save(avatar);

        ResponseEntity<byte[]> response = restTemplate.getForEntity("http://localhost:" + port
                + "/student/" + student.getId() + "/avatar/preview", byte[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
        assertThat(response.getHeaders().getContentLength())
                .isEqualTo(preview.length);
        assertThat(avatar.getPreview()).isNotNull();
        assertThat(response.getBody()).isEqualTo(preview);
    }


    @Test
    public void testDownloadFullAvatar() throws Exception {
        byte[] data = new byte[]{10, 20, 30, 40};
        byte[] previewData = new byte[]{1, 2, 3, 4, 5};

        Path avatarFile = createTempAvatarFile(data);
        Path avatarPreview = createTempAvatarPreviewFile(previewData);


        Avatar avatar = new Avatar();
        avatar.setStudent(student);
        avatar.setMediaType("image/png");
        avatar.setFilePath(avatarFile.toString());
        avatar.setFileSize((long) data.length);
        avatar.setPreview(previewData);
        System.out.println("avatar.getPreview() = 1" + avatar.getPreview());
        avatarRepository.save(avatar);
        System.out.println("avatar.getPreview() =2 " + avatar.getPreview());

        ResponseEntity<byte[]> response =
                restTemplate.getForEntity(
                        "http://localhost:" + port + "/student/" + student.getId() + "/avatar",
                        byte[].class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.IMAGE_PNG);
        assertThat(response.getBody()).isEqualTo(data);
        assertThat(response.getHeaders().getContentLength())
                .isEqualTo(data.length);


    }

    private Path createTempAvatarFile(byte[] data) throws IOException {
        Path file = Files.createTempFile("avatar-", ".png");
        Files.write(file, data);
        return file;
    }

    private Path createTempAvatarPreviewFile(byte[] data) throws IOException {
        Path file = Files.createTempFile("avatarPreview-", ".png");
        Files.write(file, data);
        return file;
    }
}