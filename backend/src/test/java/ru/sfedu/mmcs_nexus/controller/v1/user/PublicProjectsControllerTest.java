package ru.sfedu.mmcs_nexus.controller.v1.user;

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
import ru.sfedu.mmcs_nexus.controller.publicapi.PublicProjectController;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicProjectsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectEventService projectEventService;

    @Test
    void shouldReturnProjectsByEventIdWithDefaultParams() throws Exception {
        UUID eventId = UUID.randomUUID();
        Project project = createProject("MMCS Nexus", 2026);

        when(projectEventService.findProjectsByEventId(eq(eventId.toString()), eq(null), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(project)));

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId))
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
                .andExpect(jsonPath("$.content[0].description").value("Описание проекта"))
                .andExpect(jsonPath("$.content[0].type").value("WEB_APP"))
                .andExpect(jsonPath("$.content[0].year").value(2026))
                .andExpect(jsonPath("$.content[0].full").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<PaginationPayload> paginationCaptor =
                ArgumentCaptor.forClass(PaginationPayload.class);

        verify(projectEventService).findProjectsByEventId(
                eq(eventId.toString()),
                eq(null),
                paginationCaptor.capture()
        );

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(10, paginationPayload.getLimit());
        assertEquals(0, paginationPayload.getOffset());
        assertEquals("id", paginationPayload.getSort());
        assertEquals("asc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnEmptyProjectsListByEventId() throws Exception {
        UUID eventId = UUID.randomUUID();

        when(projectEventService.findProjectsByEventId(eq(eventId.toString()), eq(null), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(projectEventService).findProjectsByEventId(
                eq(eventId.toString()),
                eq(null),
                any(PaginationPayload.class)
        );
    }

    @Test
    void shouldPassQueryParamsToService() throws Exception {
        UUID eventId = UUID.randomUUID();

        when(projectEventService.findProjectsByEventId(eq(eventId.toString()), eq(2), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("limit", "5")
                        .param("offset", "10")
                        .param("sort", "name")
                        .param("order", "desc")
                        .param("day", "2"))
                .andExpect(status().isOk());

        ArgumentCaptor<PaginationPayload> paginationCaptor =
                ArgumentCaptor.forClass(PaginationPayload.class);

        verify(projectEventService).findProjectsByEventId(
                eq(eventId.toString()),
                eq(2),
                paginationCaptor.capture()
        );

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(5, paginationPayload.getLimit());
        assertEquals(10, paginationPayload.getOffset());
        assertEquals("name", paginationPayload.getSort());
        assertEquals("desc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnSeveralProjectsByEventId() throws Exception {
        UUID eventId = UUID.randomUUID();

        Project firstProject = createProject("Alpha", 2026);
        Project secondProject = createProject("Beta", 2026);

        when(projectEventService.findProjectsByEventId(eq(eventId.toString()), eq(1), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(firstProject, secondProject)));

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("day", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Alpha"))
                .andExpect(jsonPath("$.content[1].name").value("Beta"))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(projectEventService).findProjectsByEventId(
                eq(eventId.toString()),
                eq(1),
                any(PaginationPayload.class)
        );
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        UUID eventId = UUID.randomUUID();

        when(projectEventService.findProjectsByEventId(eq(eventId.toString()), eq(null), any(PaginationPayload.class)))
                .thenThrow(new EntityNotFoundException("Event with id " + eventId + " not found"));

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event with id " + eventId + " not found"));

        verify(projectEventService).findProjectsByEventId(
                eq(eventId.toString()),
                eq(null),
                any(PaginationPayload.class)
        );
    }

    @Test
    void shouldReturnBadRequestWhenEventIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/events/{id}/projects", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsInvalid() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("limit", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOffsetIsInvalid() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("offset", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDayIsInvalidType() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("day", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDayIsLessThanOne() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("day", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDayIsGreaterThanTwo() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("day", "3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSortParamIsInvalid() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("sort", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting param: unknown"));
    }

    @Test
    void shouldReturnBadRequestWhenSortOrderIsInvalid() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("order", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting order: wrong"));
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