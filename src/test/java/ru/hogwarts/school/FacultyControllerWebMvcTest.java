package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.FacultyController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacultyService facultyService;

    @Test
    void getFacultyInfo() throws Exception {
        Faculty faculty = new Faculty("ТС", "Красный");
        faculty.setId(1L);

        when(facultyService.findFaculty(1L)).thenReturn(faculty);

        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ТС"));
    }

    @Test
    void createFaculty() throws Exception {
        Faculty faculty = new Faculty("ТФ", "Серый");
        faculty.setId(1L);

        when(facultyService.addFaculty(any(Faculty.class)))
                .thenReturn(faculty);

        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" 
                                {"name": "ТФ","color": "Серый"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("Серый"));
    }

    @Test
    void findFacultyByNameOrColor() throws Exception {
        when(facultyService.findByNameOrColorIgnoreCase("Красный"))
                .thenReturn(List.of(new Faculty("ТС", "Красный")));

        mockMvc.perform(get("/faculty/search-color-name")
                        .param("searchTerm", "Красный"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].color").value("Красный"));
    }
}
