package ru.hogwarts.school.service;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repositories.StudentRepository;


import java.util.Collection;
import java.util.List;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    private final StudentRepository studentRepository;


    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student addStudent(Student student) {
        logger.info("Was invoked method for add student");
        if (student == null) {
            logger.error("Attempt to add null student");
            throw new IllegalArgumentException("Student must not be null");
        }
        return studentRepository.save(student);
    }

    public Student findStudent(long id) {
        logger.info("Was invoked method for find student by id={}", id);
        return studentRepository.findById(id).orElse(null);
    }

    public Student editStudent(Student student) {
        logger.info("Was invoked method for edit student id={}", student.getId());

        if (student.getId() == null || !studentRepository.existsById(student.getId())) {
            logger.error("Student not found for edit id={}", student.getId());
            throw new EntityNotFoundException("Student not found");
        }
        return studentRepository.save(student);
    }

    public Collection<Student> findStudentAll() {
        logger.info("Was invoked method for find all students");
        return studentRepository.findAll();
    }

    public void deleteStudent(long id) {
        logger.warn("Was invoked method for delete student id={}", id);
        studentRepository.deleteById(id);
    }

    public Collection<Student> findByAge(int age) {
        logger.warn("Was invoked method for find student by age={}", age);
        return studentRepository.findByAge(age);
    }

    public Collection<Student> findByAgeBetween(int ageMin, int ageMax) {
        logger.warn("Was invoked method for find student by age between ageMin={}, ageMax={}", ageMin,ageMax );
        return studentRepository.findByAgeBetween(ageMin, ageMax);
    }

    public Faculty getFacultyByStudentId(Long studentId) {
        logger.info("Was invoked method for get faculty by studentId={}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    logger.error("Student not found with id={}", studentId);
                    return new RuntimeException("Student not found");
                });
        return student.getFaculty();
    }
    public List<String> getStudentsNamesStartsWithA() {
        logger.info("Was invoked method for get students names starting with A");

        return studentRepository.findAll().stream()
                .map(Student::getName)
                .map(String::toUpperCase)
                .filter(name -> name.startsWith("А"))
                .sorted()
                .toList();
    }
    public double getAverageAge() {
        logger.info("Was invoked method for get average students age");

        return studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0);
    }

}
