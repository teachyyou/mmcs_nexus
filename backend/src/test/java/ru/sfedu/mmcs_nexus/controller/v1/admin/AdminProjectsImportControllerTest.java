package ru.sfedu.mmcs_nexus.controller.v1.admin;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.internal.ImportRowIssue;
import ru.sfedu.mmcs_nexus.model.payload.admin.ImportResponsePayload;
import ru.sfedu.mmcs_nexus.service.ImportService;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;
import ru.sfedu.mmcs_nexus.service.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.service.ProjectService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminProjectsImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectEventService projectEventService;

    @MockBean
    private ProjectJuryEventService projectJuryEventService;

    @MockBean
    private ImportService importService;

    @Test
    void shouldImportProjectsFromCsvWithDefaultLimit() throws Exception {
        Project project = createProject("MMCS Nexus", 2026);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projects.csv",
                "text/csv",
                "name,projectType,captainFio,track\nMMCS Nexus,web,Иван Иванов,Backend\n".getBytes()
        );

        when(importService.ImportProjectsFromCsv(any(MultipartFile.class), eq(2)))
                .thenReturn(new ImportResponsePayload<>(List.of(project), List.of()));

        mockMvc.perform(multipart("/api/v1/admin/projects/from_csv")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.created", hasSize(1)))
                .andExpect(jsonPath("$.created[0].id").value(project.getId().toString()))
                .andExpect(jsonPath("$.created[0].name").value("MMCS Nexus"))
                .andExpect(jsonPath("$.created[0].type").value("WEB_APP"))
                .andExpect(jsonPath("$.created[0].year").value(2026))
                .andExpect(jsonPath("$.skipped", hasSize(0)));

        ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(importService).ImportProjectsFromCsv(fileCaptor.capture(), limitCaptor.capture());

        assertEquals("projects.csv", fileCaptor.getValue().getOriginalFilename());
        assertEquals(2, limitCaptor.getValue());
    }

    @Test
    void shouldImportProjectsFromCsvWithCustomLimit() throws Exception {
        Project firstProject = createProject("First", 2026);
        Project secondProject = createProject("Second", 2026);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projects.csv",
                "text/csv",
                "name,projectType,captainFio,track\nFirst,web,Иван Иванов,Backend\nSecond,bot,Пётр Петров,Bots\n".getBytes()
        );

        when(importService.ImportProjectsFromCsv(any(MultipartFile.class), eq(10)))
                .thenReturn(new ImportResponsePayload<>(List.of(firstProject, secondProject), List.of()));

        mockMvc.perform(multipart("/api/v1/admin/projects/from_csv")
                        .file(file)
                        .param("limit", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created", hasSize(2)))
                .andExpect(jsonPath("$.created[0].name").value("First"))
                .andExpect(jsonPath("$.created[1].name").value("Second"))
                .andExpect(jsonPath("$.skipped", hasSize(0)));

        verify(importService).ImportProjectsFromCsv(any(MultipartFile.class), eq(10));
    }

    @Test
    void shouldReturnSkippedRowsFromImportService() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projects.csv",
                "text/csv",
                "name,projectType,captainFio,track\nDuplicate,web,Иван Иванов,Backend\n".getBytes()
        );

        ImportRowIssue issue = ImportRowIssue.of(
                2,
                "duplicate_name",
                "Project with name 'Duplicate' already exists"
        );

        when(importService.ImportProjectsFromCsv(any(MultipartFile.class), eq(2)))
                .thenReturn(new ImportResponsePayload<>(List.of(), List.of(issue)));

        mockMvc.perform(multipart("/api/v1/admin/projects/from_csv")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created", hasSize(0)))
                .andExpect(jsonPath("$.skipped", hasSize(1)))
                .andExpect(jsonPath("$.skipped[0].rowNumber").value(2))
                .andExpect(jsonPath("$.skipped[0].code").value("duplicate_name"))
                .andExpect(jsonPath("$.skipped[0].message").value("Project with name 'Duplicate' already exists"));

        verify(importService).ImportProjectsFromCsv(any(MultipartFile.class), eq(2));
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsInvalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projects.csv",
                "text/csv",
                "name,projectType,captainFio,track\nProject,web,Иван Иванов,Backend\n".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/admin/projects/from_csv")
                        .file(file)
                        .param("limit", "invalid")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    private Project createProject(String name, int year) {
        Project project = new Project();

        project.setId(UUID.randomUUID());
        project.setExternalId(1001);
        project.setQuantityOfStudents(4);
        project.setCaptainName("Иван Иванов");
        project.setFull(true);
        project.setTrack("Backend");
        project.setTechnologies("Java, Spring");
        project.setName(name);
        project.setDescription("Описание проекта");
        project.setType("WEB_APP");
        project.setYear(year);

        return project;
    }
}