package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repositories.FacultyRepository;

import java.util.Collection;


@Service
public class FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);
    @Autowired
    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty addFaculty(Faculty faculty) {
        logger.info("Was invoked method for add faculty");
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(long id) {

        logger.info("Was invoked method for find faculty by id={}", id);

        return facultyRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("No faculty with id={}", id);
                    return new RuntimeException("Faculty not found");
                });
    }

    public Collection<Faculty> findFacultyAll() {
        logger.info("Was invoked method for find all faculties");
        return facultyRepository.findAll();
    }

    public Faculty editFaculty(Faculty faculty) {
        logger.info("Was invoked method for edit faculty id={}", faculty.getId());
        return facultyRepository.save(faculty);
    }

    public void deleteFaculty(long id) {
        logger.warn("Was invoked method for delete faculty id={}", id);
        facultyRepository.deleteById(id);
    }

    public Collection<Faculty> findByNameOrColorIgnoreCase(String searchTerm) {
        logger.info("Was invoked method for find all faculties by Name Or Color Ignore Case");
        return facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(searchTerm, searchTerm);
    }

    public Collection<Student> getStudentsByFacultyId(Long facultyId) {
        logger.info("Was invoked method for get students by facultyId={}", facultyId);

        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> {
                    logger.error("Faculty not found with id={}", facultyId);
                    return new RuntimeException("Faculty not found");
                });
        return faculty.getStudents();
    }
}
