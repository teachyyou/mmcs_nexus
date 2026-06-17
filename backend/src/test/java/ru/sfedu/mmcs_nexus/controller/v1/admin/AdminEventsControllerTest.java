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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateEventRequestPayload;
import ru.sfedu.mmcs_nexus.service.EventService;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;

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

@WebMvcTest(AdminEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminEventsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private ProjectEventService projectEventService;

    @Test
    void shouldReturnEventsList() throws Exception {
        Event event = createEvent();

        when(eventService.findAll(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        mockMvc.perform(get("/api/v1/admin/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(event.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value("Идея"))
                .andExpect(jsonPath("$.content[0].eventType").value("IDEA"))
                .andExpect(jsonPath("$.content[0].year").value(2026))
                .andExpect(jsonPath("$.content[0].maxPresPoints").value(20))
                .andExpect(jsonPath("$.content[0].maxBuildPoints").value(30))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(eventService).findAll(isNull(), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(10, paginationPayload.getLimit());
        assertEquals(0, paginationPayload.getOffset());
        assertEquals("id", paginationPayload.getSort());
        assertEquals("asc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnEmptyEventsList() throws Exception {
        when(eventService.findAll(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(eventService).findAll(isNull(), any(PaginationPayload.class));
    }

    @Test
    void shouldPassQueryParamsToService() throws Exception {
        when(eventService.findAll(any(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/events")
                        .param("limit", "5")
                        .param("offset", "10")
                        .param("sort", "name")
                        .param("order", "desc")
                        .param("year", "2026"))
                .andExpect(status().isOk());

        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(eventService).findAll(yearCaptor.capture(), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(2026, yearCaptor.getValue());
        assertEquals(5, paginationPayload.getLimit());
        assertEquals(10, paginationPayload.getOffset());
        assertEquals("name", paginationPayload.getSort());
        assertEquals("desc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnEventById() throws Exception {
        Event event = createEvent();

        when(eventService.find(event.getId().toString())).thenReturn(event);

        mockMvc.perform(get("/api/v1/admin/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.name").value("Идея"))
                .andExpect(jsonPath("$.eventType").value("IDEA"))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.maxPresPoints").value(20))
                .andExpect(jsonPath("$.maxBuildPoints").value(30));

        verify(eventService).find(event.getId().toString());
    }

    @Test
    void shouldCreateEvent() throws Exception {
        Map<String, Object> requestBody = createRequestBody("Идея", "IDEA", 2026, 20, 30);

        mockMvc.perform(post("/api/v1/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("saved successfully"));

        ArgumentCaptor<CreateEventRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(CreateEventRequestPayload.class);

        verify(eventService).create(payloadCaptor.capture());

        CreateEventRequestPayload payload = payloadCaptor.getValue();

        assertEquals("Идея", payload.getName());
        assertEquals(EventType.IDEA, payload.getEventType());
        assertEquals(2026, payload.getYear());
        assertEquals(20, payload.getMaxPresPoints());
        assertEquals(30, payload.getMaxBuildPoints());
    }

    @Test
    void shouldEditEvent() throws Exception {
        Event event = createEvent();
        Map<String, Object> requestBody = createRequestBody("Релиз", "RELEASE", 2027, 25, 35);

        when(eventService.edit(any(String.class), any(CreateEventRequestPayload.class)))
                .thenReturn(event);

        mockMvc.perform(put("/api/v1/admin/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.name").value("Идея"))
                .andExpect(jsonPath("$.eventType").value("IDEA"));

        ArgumentCaptor<String> eventIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CreateEventRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(CreateEventRequestPayload.class);

        verify(eventService).edit(eventIdCaptor.capture(), payloadCaptor.capture());

        CreateEventRequestPayload payload = payloadCaptor.getValue();

        assertEquals(event.getId().toString(), eventIdCaptor.getValue());
        assertEquals("Релиз", payload.getName());
        assertEquals(EventType.RELEASE, payload.getEventType());
        assertEquals(2027, payload.getYear());
        assertEquals(25, payload.getMaxPresPoints());
        assertEquals(35, payload.getMaxBuildPoints());
    }

    @Test
    void shouldDeleteEvent() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/events/{id}", eventId))
                .andExpect(status().isNoContent());

        verify(eventService).deleteEventById(eventId.toString());
    }

    @Test
    void shouldReturnBadRequestWhenEventIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/events/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        String eventId = UUID.randomUUID().toString();

        when(eventService.find(eventId))
                .thenThrow(new EntityNotFoundException("Event with id " + eventId + " not found"));

        mockMvc.perform(get("/api/v1/admin/events/{id}", eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event with id " + eventId + " not found"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateBodyIsInvalid() throws Exception {
        Map<String, Object> requestBody = createRequestBody("", "IDEA", 2026, 20, 30);

        mockMvc.perform(post("/api/v1/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenEventNameContainsLatinLetters() throws Exception {
        Map<String, Object> requestBody = createRequestBody("Release", "RELEASE", 2026, 20, 30);

        mockMvc.perform(post("/api/v1/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void shouldReturnBadRequestWhenYearIsOutOfRange() throws Exception {
        Map<String, Object> requestBody = createRequestBody("Идея", "IDEA", 2031, 20, 30);

        mockMvc.perform(post("/api/v1/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.year").exists());
    }

    @Test
    void shouldReturnBadRequestWhenMaxPointsIsOutOfRange() throws Exception {
        Map<String, Object> requestBody = createRequestBody("Идея", "IDEA", 2026, 36, 30);

        mockMvc.perform(post("/api/v1/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.maxPresPoints").exists());
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/events")
                        .param("limit", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOffsetIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/events")
                        .param("offset", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenYearParamIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/events")
                        .param("year", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSortParamIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/events")
                        .param("sort", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting param: unknown"));
    }

    @Test
    void shouldReturnBadRequestWhenSortOrderIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/events")
                        .param("order", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting order: wrong"));
    }

    private Event createEvent() {
        Event event = new Event();

        event.setId(UUID.randomUUID());
        event.setName("Идея");
        event.setEventType(EventType.IDEA);
        event.setYear(2026);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);

        return event;
    }

    private Map<String, Object> createRequestBody(
            String name,
            String eventType,
            int year,
            int maxPresPoints,
            int maxBuildPoints
    ) {
        return Map.of(
                "name", name,
                "eventType", eventType,
                "year", year,
                "maxPresPoints", maxPresPoints,
                "maxBuildPoints", maxBuildPoints
        );
    }
}