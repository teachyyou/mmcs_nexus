package ru.sfedu.mmcs_nexus.controller.v1.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.controller.publicapi.PublicProjectController;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.LinkProjectsToEventRequestPayload;
import ru.sfedu.mmcs_nexus.service.EventService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
        AdminEventController.class,
        AdminProjectController.class,
        PublicProjectController.class
})
@AutoConfigureMockMvc(addFilters = false)
class ProjectEventsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectEventService projectEventService;

    @MockBean
    private ProjectJuryEventService projectJuryEventService;

    @MockBean
    private ImportService importService;

    @Test
    void shouldReturnProjectsByEventIdForAdmin() throws Exception {
        UUID eventId = UUID.randomUUID();
        Project project = createProject("MMCS Nexus", 2026);

        when(projectEventService.findProjectsByEventId(eq(eventId.toString()), eq(null), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(project)));

        mockMvc.perform(get("/api/v1/admin/events/{id}/projects", eventId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(project.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value("MMCS Nexus"))
                .andExpect(jsonPath("$.content[0].year").value(2026))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(projectEventService).findProjectsByEventId(eq(eventId.toString()), eq(null), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(10, paginationPayload.getLimit());
        assertEquals(0, paginationPayload.getOffset());
    }

    @Test
    void shouldReturnProjectsByEventIdAndDayForAdmin() throws Exception {
        UUID eventId = UUID.randomUUID();
        Project project = createProject("First day project", 2026);

        when(projectEventService.findProjectsByEventId(eq(eventId.toString()), eq(1), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(project)));

        mockMvc.perform(get("/api/v1/admin/events/{id}/projects", eventId)
                        .param("limit", "5")
                        .param("offset", "10")
                        .param("day", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("First day project"));

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(projectEventService).findProjectsByEventId(eq(eventId.toString()), eq(1), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(5, paginationPayload.getLimit());
        assertEquals(10, paginationPayload.getOffset());
    }

    @Test
    void shouldReturnEventsByProjectIdForAdmin() throws Exception {
        UUID projectId = UUID.randomUUID();
        Event event = createEvent("Идея", EventType.IDEA, 2026);

        when(projectEventService.findEventsByProjectId(eq(projectId.toString()), eq(null), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        mockMvc.perform(get("/api/v1/admin/projects/{id}/events", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(event.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value("Идея"))
                .andExpect(jsonPath("$.content[0].eventType").value("IDEA"))
                .andExpect(jsonPath("$.content[0].year").value(2026))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(projectEventService).findEventsByProjectId(eq(projectId.toString()), eq(null), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(10, paginationPayload.getLimit());
        assertEquals(0, paginationPayload.getOffset());
    }

    @Test
    void shouldReturnProjectsByEventIdForPublicApi() throws Exception {
        UUID eventId = UUID.randomUUID();
        Project project = createProject("Public project", 2026);

        when(projectEventService.findProjectsByEventId(eq(eventId.toString()), eq(2), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(project)));

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("sort", "name")
                        .param("order", "asc")
                        .param("limit", "7")
                        .param("offset", "14")
                        .param("day", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Public project"));

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(projectEventService).findProjectsByEventId(eq(eventId.toString()), eq(2), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(7, paginationPayload.getLimit());
        assertEquals(14, paginationPayload.getOffset());
        assertEquals("name", paginationPayload.getSort());
        assertEquals("asc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnBadRequestWhenPublicDayIsOutOfRange() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("day", "3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnProjectsGroupedByDefenceDays() throws Exception {
        UUID eventId = UUID.randomUUID();

        Project firstDayProject = createProject("First day project", 2026);
        Project secondDayProject = createProject("Second day project", 2026);

        when(projectEventService.findProjectsByEventId(eventId.toString(), 1))
                .thenReturn(List.of(firstDayProject));

        when(projectEventService.findProjectsByEventId(eventId.toString(), 2))
                .thenReturn(List.of(secondDayProject));

        mockMvc.perform(get("/api/v1/admin/events/{id}/projects/days", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.firstDayProjects", hasSize(1)))
                .andExpect(jsonPath("$.content.firstDayProjects[0].name").value("First day project"))
                .andExpect(jsonPath("$.content.secondDayProjects", hasSize(1)))
                .andExpect(jsonPath("$.content.secondDayProjects[0].name").value("Second day project"))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(projectEventService).findProjectsByEventId(eventId.toString(), 1);
        verify(projectEventService).findProjectsByEventId(eventId.toString(), 2);
    }

    @Test
    void shouldSetProjectsForEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID firstProjectId = UUID.randomUUID();
        UUID secondProjectId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "projectIds", List.of(firstProjectId.toString(), secondProjectId.toString()),
                "linkAllProjects", false
        );

        mockMvc.perform(post("/api/v1/admin/events/{id}/projects", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        ArgumentCaptor<LinkProjectsToEventRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(LinkProjectsToEventRequestPayload.class);

        verify(projectEventService).setProjectsForEvent(eq(eventId.toString()), payloadCaptor.capture());

        LinkProjectsToEventRequestPayload payload = payloadCaptor.getValue();

        assertEquals(List.of(firstProjectId, secondProjectId), payload.getProjectIds());
        assertEquals(false, payload.isLinkAllProjects());
    }

    @Test
    void shouldSetAllProjectsForEvent() throws Exception {
        UUID eventId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "projectIds", List.of(),
                "linkAllProjects", true
        );

        mockMvc.perform(post("/api/v1/admin/events/{id}/projects", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        ArgumentCaptor<LinkProjectsToEventRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(LinkProjectsToEventRequestPayload.class);

        verify(projectEventService).setProjectsForEvent(eq(eventId.toString()), payloadCaptor.capture());

        assertEquals(true, payloadCaptor.getValue().isLinkAllProjects());
        assertEquals(List.of(), payloadCaptor.getValue().getProjectIds());
    }

    @Test
    void shouldSetProjectDefenceDays() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID firstDayProjectId = UUID.randomUUID();
        UUID secondDayProjectId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "firstDayProjects", List.of(firstDayProjectId.toString()),
                "secondDayProjects", List.of(secondDayProjectId.toString())
        );

        mockMvc.perform(post("/api/v1/admin/events/{id}/days", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("saved successfully"));

        verify(projectEventService).setDaysForProjectAndEvent(
                eventId.toString(),
                List.of(firstDayProjectId),
                List.of(secondDayProjectId)
        );
    }

    @Test
    void shouldReturnBadRequestWhenDefenceDayListsOverlap() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "firstDayProjects", List.of(projectId.toString()),
                "secondDayProjects", List.of(projectId.toString())
        );

        org.mockito.Mockito.doThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Project lists for day 1 and day 2 must not overlap"
                ))
                .when(projectEventService)
                .setDaysForProjectAndEvent(eq(eventId.toString()), any(), any());

        mockMvc.perform(post("/api/v1/admin/events/{id}/days", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        UUID eventId = UUID.randomUUID();

        when(projectEventService.findProjectsByEventId(eq(eventId.toString()), eq(null), any(PaginationPayload.class)))
                .thenThrow(new EntityNotFoundException("Event with id " + eventId + " not found"));

        mockMvc.perform(get("/api/v1/admin/events/{id}/projects", eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event with id " + eventId + " not found"));
    }

    @Test
    void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
        UUID projectId = UUID.randomUUID();

        when(projectEventService.findEventsByProjectId(eq(projectId.toString()), eq(null), any(PaginationPayload.class)))
                .thenThrow(new EntityNotFoundException("Project with id " + projectId + " not found"));

        mockMvc.perform(get("/api/v1/admin/projects/{id}/events", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Project with id " + projectId + " not found"));
    }

    @Test
    void shouldReturnBadRequestWhenEventIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/events/{id}/projects", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenProjectIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/projects/{id}/events", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenProjectSortParamIsInvalid() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/public/events/{id}/projects", eventId)
                        .param("sort", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting param: unknown"));
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

    private Event createEvent(String name, EventType eventType, int year) {
        Event event = new Event();

        event.setId(UUID.randomUUID());
        event.setName(name);
        event.setEventType(eventType);
        event.setYear(year);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);

        return event;
    }
}