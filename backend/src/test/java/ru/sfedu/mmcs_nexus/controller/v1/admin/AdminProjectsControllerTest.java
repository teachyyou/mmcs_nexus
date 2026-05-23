package ru.sfedu.mmcs_nexus.controller.v1.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateProjectRequestPayload;
import ru.sfedu.mmcs_nexus.service.ImportService;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;
import ru.sfedu.mmcs_nexus.service.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.service.ProjectService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminProjectsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectEventService projectEventService;

    @MockBean
    private ProjectJuryEventService projectJuryEventService;

    @MockBean
    private ImportService importService;

    @Test
    void shouldReturnProjectsList() throws Exception {
        Project project = createProject();

        when(projectService.findAll(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(project)));

        mockMvc.perform(get("/api/v1/admin/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(project.getId().toString()))
                .andExpect(jsonPath("$.content[0].externalId").value(1001))
                .andExpect(jsonPath("$.content[0].quantityOfStudents").value(4))
                .andExpect(jsonPath("$.content[0].captainName").value("Иван Иванов"))
                .andExpect(jsonPath("$.content[0].track").value("Backend"))
                .andExpect(jsonPath("$.content[0].technologies").value("Java, Spring"))
                .andExpect(jsonPath("$.content[0].name").value("MMCS Nexus"))
                .andExpect(jsonPath("$.content[0].description").value("Платформа для проектной деятельности"))
                .andExpect(jsonPath("$.content[0].type").value("WEB_APP"))
                .andExpect(jsonPath("$.content[0].year").value(2026))
                .andExpect(jsonPath("$.content[0].full").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(projectService).findAll(isNull(), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(10, paginationPayload.getLimit());
        assertEquals(0, paginationPayload.getOffset());
        assertEquals("id", paginationPayload.getSort());
        assertEquals("asc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnEmptyProjectsList() throws Exception {
        when(projectService.findAll(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(projectService).findAll(isNull(), any(PaginationPayload.class));
    }

    @Test
    void shouldPassQueryParamsToService() throws Exception {
        when(projectService.findAll(any(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/projects")
                        .param("limit", "5")
                        .param("offset", "10")
                        .param("sort", "name")
                        .param("order", "desc")
                        .param("year", "2026"))
                .andExpect(status().isOk());

        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(projectService).findAll(yearCaptor.capture(), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(2026, yearCaptor.getValue());
        assertEquals(5, paginationPayload.getLimit());
        assertEquals(10, paginationPayload.getOffset());
        assertEquals("name", paginationPayload.getSort());
        assertEquals("desc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnProjectById() throws Exception {
        Project project = createProject();

        when(projectService.find(project.getId().toString())).thenReturn(project);

        mockMvc.perform(get("/api/v1/admin/projects/{id}", project.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(project.getId().toString()))
                .andExpect(jsonPath("$.externalId").value(1001))
                .andExpect(jsonPath("$.quantityOfStudents").value(4))
                .andExpect(jsonPath("$.captainName").value("Иван Иванов"))
                .andExpect(jsonPath("$.track").value("Backend"))
                .andExpect(jsonPath("$.technologies").value("Java, Spring"))
                .andExpect(jsonPath("$.name").value("MMCS Nexus"))
                .andExpect(jsonPath("$.description").value("Платформа для проектной деятельности"))
                .andExpect(jsonPath("$.type").value("WEB_APP"))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.full").value(true));

        verify(projectService).find(project.getId().toString());
    }

    @Test
    void shouldCreateProject() throws Exception {
        Map<String, Object> requestBody = createRequestBody(
                "Новый проект",
                "Описание нового проекта",
                2026
        );

        mockMvc.perform(post("/api/v1/admin/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("saved successfully"));

        ArgumentCaptor<CreateProjectRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(CreateProjectRequestPayload.class);

        verify(projectService).create(payloadCaptor.capture());

        CreateProjectRequestPayload payload = payloadCaptor.getValue();

        assertEquals(1001, payload.getExternalId());
        assertEquals(4, payload.getQuantityOfStudents());
        assertEquals("Иван Иванов", payload.getCaptainName());
        assertEquals(true, payload.isFull());
        assertEquals("Backend", payload.getTrack());
        assertEquals("Java, Spring", payload.getTechnologies());
        assertEquals("Новый проект", payload.getName());
        assertEquals("Описание нового проекта", payload.getDescription());
        assertEquals("WEB_APP", payload.getType());
        assertEquals(2026, payload.getYear());
    }

    @Test
    void shouldReturnConflictWhenCreatingProjectWithExistingName() throws Exception {
        Map<String, Object> requestBody = createRequestBody(
                "MMCS Nexus",
                "Описание",
                2026
        );

        org.mockito.Mockito.doThrow(new EntityExistsException("Project with name MMCS Nexus already exists"))
                .when(projectService)
                .create(any(CreateProjectRequestPayload.class));

        mockMvc.perform(post("/api/v1/admin/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Project with name MMCS Nexus already exists"));
    }

    @Test
    void shouldEditProject() throws Exception {
        Project project = createProject();
        Map<String, Object> requestBody = createRequestBody(
                "Обновлённый проект",
                "Обновлённое описание",
                2027
        );

        when(projectService.edit(any(String.class), any(CreateProjectRequestPayload.class)))
                .thenReturn(project);

        mockMvc.perform(put("/api/v1/admin/projects/{id}", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(project.getId().toString()))
                .andExpect(jsonPath("$.name").value("MMCS Nexus"))
                .andExpect(jsonPath("$.year").value(2026));

        ArgumentCaptor<String> projectIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CreateProjectRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(CreateProjectRequestPayload.class);

        verify(projectService).edit(projectIdCaptor.capture(), payloadCaptor.capture());

        CreateProjectRequestPayload payload = payloadCaptor.getValue();

        assertEquals(project.getId().toString(), projectIdCaptor.getValue());
        assertEquals("Обновлённый проект", payload.getName());
        assertEquals("Обновлённое описание", payload.getDescription());
        assertEquals(2027, payload.getYear());
    }

    @Test
    void shouldReturnConflictWhenEditingProjectWithExistingName() throws Exception {
        UUID projectId = UUID.randomUUID();
        Map<String, Object> requestBody = createRequestBody(
                "Duplicate",
                "Описание",
                2026
        );

        when(projectService.edit(any(String.class), any(CreateProjectRequestPayload.class)))
                .thenThrow(new EntityExistsException("Project with name Duplicate already exists"));

        mockMvc.perform(put("/api/v1/admin/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Project with name Duplicate already exists"));
    }

    @Test
    void shouldDeleteProject() throws Exception {
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/projects/{id}", projectId))
                .andExpect(status().isNoContent());

        verify(projectService).delete(projectId.toString());
    }

    @Test
    void shouldReturnBadRequestWhenProjectIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/projects/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
        String projectId = UUID.randomUUID().toString();

        when(projectService.find(projectId))
                .thenThrow(new EntityNotFoundException("Project with id " + projectId + " not found"));

        mockMvc.perform(get("/api/v1/admin/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Project with id " + projectId + " not found"));
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/projects")
                        .param("limit", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOffsetIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/projects")
                        .param("offset", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenYearIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/projects")
                        .param("year", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSortParamIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/projects")
                        .param("sort", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting param: unknown"));
    }

    @Test
    void shouldReturnBadRequestWhenSortOrderIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/projects")
                        .param("order", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting order: wrong"));
    }

    private Project createProject() {
        Project project = new Project();

        project.setId(UUID.randomUUID());
        project.setExternalId(1001);
        project.setQuantityOfStudents(4);
        project.setCaptainName("Иван Иванов");
        project.setFull(true);
        project.setTrack("Backend");
        project.setTechnologies("Java, Spring");
        project.setName("MMCS Nexus");
        project.setDescription("Платформа для проектной деятельности");
        project.setType("WEB_APP");
        project.setYear(2026);

        return project;
    }

    private Map<String, Object> createRequestBody(String name, String description, int year) {
        return Map.of(
                "externalId", 1001,
                "quantityOfStudents", 4,
                "captainName", "Иван Иванов",
                "full", true,
                "track", "Backend",
                "technologies", "Java, Spring",
                "name", name,
                "description", description,
                "type", "WEB_APP",
                "year", year
        );
    }
}