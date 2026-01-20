package ru.hogwarts.school.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.AvatarService;
import ru.hogwarts.school.service.StudentService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final AvatarService avatarService;


    public StudentController(StudentService studentService, AvatarService avatarService) {
        this.studentService = studentService;
        this.avatarService = avatarService;
    }

    @GetMapping("{id}")
    public ResponseEntity<Student> getStudentInfo(@PathVariable Long id) {
        Student student = studentService.findStudent(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentService.addStudent(student);
    }

    @PutMapping
    public ResponseEntity<Student> editStudent(@RequestBody Student student) {
        try {
            Student foundStudent = studentService.editStudent(student);
            return ResponseEntity.ok(foundStudent);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }

    @DeleteMapping("{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);

    }

    @GetMapping("/by-age")
    public ResponseEntity<Collection<Student>> findStudents(@RequestParam(required = false) Integer age) {
        if (age != null && age > 0) {
            return ResponseEntity.ok(studentService.findByAge(age));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/by-age-range")
    public ResponseEntity<Collection<Student>> findStudentsBetweenAge(@RequestParam(required = false) Integer ageMin, @RequestParam(required = false) Integer ageMax) {
        if (ageMin != null && ageMax != null && ageMin > 0 && ageMax < 50 && ageMin <= ageMax) {
            return ResponseEntity.ok(studentService.findByAgeBetween(ageMin, ageMax));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/{studentId}/faculty")
    public Faculty getFacultyByStudent(@PathVariable Long studentId) {
        return studentService.getFacultyByStudentId(studentId);
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@PathVariable Long id, @RequestParam MultipartFile avatar) throws IOException {

        if (avatar.getSize() >= 1024 * 300) {

            return ResponseEntity.badRequest().body("Files is too big");
        }
        if (avatar == null || avatar.isEmpty()) {
            return ResponseEntity.badRequest().body("File is missing");
        }
        avatarService.uploadAvatar(id, avatar);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{id}/avatar/preview")
    public ResponseEntity<byte[]> downloadAvatarPreview(@PathVariable Long id) {
        Avatar avatar = avatarService.findAvatar(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
        headers.setContentLength(avatar.getPreview().length);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(avatar.getPreview());

    }

    @GetMapping(value = "/{id}/avatar")
    public void downloadAvatar(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Avatar avatar = avatarService.findAvatar(id);

        Path path = Path.of(avatar.getFilePath());

        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream();) {
            response.setContentType(avatar.getMediaType());
            response.setContentLengthLong(avatar.getFileSize());
            is.transferTo(os);
        }
    }

    @GetMapping("/names-starts-with-a")
    public List<String> getStudentsNamesStartsWithA() {
        return studentService.getStudentsNamesStartsWithA();
    }

    @GetMapping("/average-age")
    public double getAverageAge() {
        return studentService.getAverageAge();
    }

    @GetMapping("/sum")
    public int getSum() {
        return Stream.iterate(1, a -> a + 1)
                .limit(1_000_000)
                .parallel()
                .reduce(0, Integer::sum);
    }
    @GetMapping("/print-parallel")
    public void printStudentsParallel() {

        List<Student> students = studentService.findStudentAll()
                .stream()
                .limit(6)
                .toList();


        System.out.println(Thread.currentThread().getName() + ": " + students.get(0).getName());
        System.out.println(Thread.currentThread().getName() + ": " + students.get(1).getName());


        Thread thread1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ": " + students.get(2).getName());
            System.out.println(Thread.currentThread().getName() + ": " + students.get(3).getName());
        });


        Thread thread2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ": " + students.get(4).getName());
            System.out.println(Thread.currentThread().getName() + ": " + students.get(5).getName());
        });

        thread1.start();
        thread2.start();
    }
    private synchronized void printName(String name) {
        System.out.println(Thread.currentThread().getName() + ": " + name);
    }
    @GetMapping("/print-synchronized")
    public void printStudentsSynchronized() {

        List<Student> students = studentService.findStudentAll()
                .stream()
                .limit(6)
                .toList();


        printName(students.get(0).getName());
        printName(students.get(1).getName());


        Thread thread1 = new Thread(() -> {
            printName(students.get(2).getName());
            printName(students.get(3).getName());
        });


        Thread thread2 = new Thread(() -> {
            printName(students.get(4).getName());
            printName(students.get(5).getName());
        });

        thread1.start();
        thread2.start();
    }
}
