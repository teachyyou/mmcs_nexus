package ru.sfedu.mmcs_nexus.controller.v1.user;

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
import ru.sfedu.mmcs_nexus.service.EventService;

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

@WebMvcTest(PublicEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicEventsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    void shouldReturnEventsListWithDefaultParams() throws Exception {
        Event event = createEvent("Идея", EventType.IDEA, 2026);

        when(eventService.findAll(eq(2026), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        mockMvc.perform(get("/api/v1/public/events"))
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

        ArgumentCaptor<PaginationPayload> paginationCaptor =
                ArgumentCaptor.forClass(PaginationPayload.class);

        verify(eventService).findAll(eq(2026), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(10, paginationPayload.getLimit());
        assertEquals(0, paginationPayload.getOffset());
        assertEquals("id", paginationPayload.getSort());
        assertEquals("asc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnEmptyEventsList() throws Exception {
        when(eventService.findAll(eq(2026), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/public/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(eventService).findAll(eq(2026), any(PaginationPayload.class));
    }

    @Test
    void shouldPassQueryParamsToService() throws Exception {
        when(eventService.findAll(eq(2026), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/public/events")
                        .param("limit", "5")
                        .param("offset", "10")
                        .param("sort", "name")
                        .param("order", "desc")
                        .param("year", "2026"))
                .andExpect(status().isOk());

        ArgumentCaptor<PaginationPayload> paginationCaptor =
                ArgumentCaptor.forClass(PaginationPayload.class);

        verify(eventService).findAll(eq(2026), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(5, paginationPayload.getLimit());
        assertEquals(10, paginationPayload.getOffset());
        assertEquals("name", paginationPayload.getSort());
        assertEquals("desc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnSeveralEvents() throws Exception {
        Event ideaEvent = createEvent("Идея", EventType.IDEA, 2026);
        Event releaseEvent = createEvent("Релиз", EventType.RELEASE, 2026);

        when(eventService.findAll(eq(2026), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(ideaEvent, releaseEvent)));

        mockMvc.perform(get("/api/v1/public/events")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Идея"))
                .andExpect(jsonPath("$.content[0].eventType").value("IDEA"))
                .andExpect(jsonPath("$.content[1].name").value("Релиз"))
                .andExpect(jsonPath("$.content[1].eventType").value("RELEASE"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldReturnEventsYears() throws Exception {
        when(eventService.getEventsYears())
                .thenReturn(List.of(2024, 2025, 2026));

        mockMvc.perform(get("/api/v1/public/events/years"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0]").value(2024))
                .andExpect(jsonPath("$.content[1]").value(2025))
                .andExpect(jsonPath("$.content[2]").value(2026))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(eventService).getEventsYears();
    }

    @Test
    void shouldReturnEmptyEventsYears() throws Exception {
        when(eventService.getEventsYears())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/public/events/years"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(eventService).getEventsYears();
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/events")
                        .param("limit", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOffsetIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/events")
                        .param("offset", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenYearIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/events")
                        .param("year", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSortParamIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/events")
                        .param("sort", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting param: unknown"));
    }

    @Test
    void shouldReturnBadRequestWhenSortOrderIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/events")
                        .param("order", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting order: wrong"));
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