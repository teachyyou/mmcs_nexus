package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateEventRequestPayload;
import ru.sfedu.mmcs_nexus.repository.EventRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventsServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldFindAllEventsWithoutYear() {
        Event event = createEvent();
        PaginationPayload paginationPayload = new PaginationPayload(10, 0, "id", "asc", EntitySort.EVENT_SORT);

        when(eventRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        Page<Event> result = eventService.findAll(null, paginationPayload);

        assertEquals(1, result.getTotalElements());
        assertEquals(event.getId(), result.getContent().getFirst().getId());

        verify(eventRepository).findAll(any(org.springframework.data.domain.Pageable.class));
        verify(eventRepository, never()).findByYear(anyInt(), any());
    }

    @Test
    void shouldFindAllEventsByYear() {
        Event event = createEvent();
        PaginationPayload paginationPayload = new PaginationPayload(10, 0, "id", "asc", EntitySort.EVENT_SORT);

        when(eventRepository.findByYear(eq(2026), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        Page<Event> result = eventService.findAll(2026, paginationPayload);

        assertEquals(1, result.getTotalElements());
        assertEquals(event.getId(), result.getContent().getFirst().getId());

        verify(eventRepository).findByYear(eq(2026), any(org.springframework.data.domain.Pageable.class));
        verify(eventRepository, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void shouldFindEventById() {
        Event event = createEvent();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        Event result = eventService.find(event.getId().toString());

        assertSame(event, result);

        verify(eventRepository).findById(event.getId());
    }

    @Test
    void shouldThrowWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> eventService.find(eventId.toString())
        );

        assertEquals("Event with id " + eventId + " not found", exception.getMessage());

        verify(eventRepository).findById(eventId);
    }

    @Test
    void shouldReturnEventsYears() {
        when(eventRepository.findAllEventsYears()).thenReturn(List.of(2024, 2025, 2026));

        List<Integer> result = eventService.getEventsYears();

        assertEquals(List.of(2024, 2025, 2026), result);

        verify(eventRepository).findAllEventsYears();
    }

    @Test
    void shouldCreateEvent() {
        CreateEventRequestPayload payload = createPayload("  Идея  ", EventType.IDEA, 2026, 20, 30);

        eventService.create(payload);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();

        assertEquals("Идея", savedEvent.getName());
        assertEquals(EventType.IDEA, savedEvent.getEventType());
        assertEquals(2026, savedEvent.getYear());
        assertEquals(20, savedEvent.getMaxPresPoints());
        assertEquals(30, savedEvent.getMaxBuildPoints());
    }

    @Test
    void shouldEditEvent() {
        Event event = createEvent();
        CreateEventRequestPayload payload = createPayload("Релиз", EventType.RELEASE, 2027, 25, 35);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        Event result = eventService.edit(event.getId().toString(), payload);

        assertSame(event, result);
        assertEquals("Релиз", event.getName());
        assertEquals(EventType.RELEASE, event.getEventType());
        assertEquals(2027, event.getYear());
        assertEquals(25, event.getMaxPresPoints());
        assertEquals(35, event.getMaxBuildPoints());

        verify(eventRepository).findById(event.getId());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldThrowWhenEditingUnknownEvent() {
        UUID eventId = UUID.randomUUID();
        CreateEventRequestPayload payload = createPayload("Релиз", EventType.RELEASE, 2027, 25, 35);

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> eventService.edit(eventId.toString(), payload)
        );

        assertEquals("Event with id " + eventId + " not found", exception.getMessage());

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldDeleteEvent() {
        Event event = createEvent();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        eventService.deleteEventById(event.getId().toString());

        verify(eventRepository).delete(event);
    }

    @Test
    void shouldThrowWhenDeletingUnknownEvent() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> eventService.deleteEventById(eventId.toString())
        );

        assertEquals("Event with id " + eventId + " not found", exception.getMessage());

        verify(eventRepository, never()).delete(any(Event.class));
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

    private CreateEventRequestPayload createPayload(
            String name,
            EventType eventType,
            int year,
            int maxPresPoints,
            int maxBuildPoints
    ) {
        CreateEventRequestPayload payload = new CreateEventRequestPayload();

        payload.setName(name);
        payload.setEventType(eventType);
        payload.setYear(year);
        payload.setMaxPresPoints(maxPresPoints);
        payload.setMaxBuildPoints(maxBuildPoints);

        return payload;
    }
}