package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.AvatarService;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private AvatarService avatarService;

    @Test
    void getStudentInfo_success() throws Exception {
        Student student = new Student("Семен", 18);
        student.setId(1L);

        when(studentService.findStudent(1L)).thenReturn(student);

        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Семен"))
                .andExpect(jsonPath("$.age").value(18));
    }

    @Test
    void getStudentInfo_notFound() throws Exception {
        when(studentService.findStudent(99L)).thenReturn(null);

        mockMvc.perform(get("/student/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createStudent() throws Exception {
        Student student = new Student("Роман", 17);
        student.setId(1L);

        when(studentService.addStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Роман",
                          "age": 17
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Роман"));
    }

    @Test
    void editStudent_success() throws Exception {
        Student student = new Student("Антон", 19);
        student.setId(1L);

        when(studentService.editStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "id": 1,
                          "name": "Антон",
                          "age": 19
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Антон"));
    }

    @Test
    void deleteStudent() throws Exception {
        doNothing().when(studentService).deleteStudent(1L);

        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isOk());
    }

    @Test
    void findStudentsByAge() throws Exception {
        when(studentService.findByAge(18)).thenReturn(List.of(new Student("Григорий", 18)));

        mockMvc.perform(get("/student/by-age")
                        .param("age", "18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].age").value(18));
    }

    @Test
    void uploadAvatar_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "image".getBytes()
        );

        doNothing().when(avatarService).uploadAvatar(eq(1L), any());

        mockMvc.perform(multipart("/student/1/avatar")
                        .file(file))
                .andExpect(status().isOk());
    }

    @Test
    void downloadAvatarPreview() throws Exception {
        Avatar avatar = new Avatar();
        avatar.setMediaType("image/png");
        avatar.setPreview(new byte[]{1, 2, 3});

        when(avatarService.findAvatar(1L)).thenReturn(avatar);

        mockMvc.perform(get("/student/1/avatar/preview"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().string("Content-Type", "image/png"));
    }
}