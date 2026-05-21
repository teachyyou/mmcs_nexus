package ru.sfedu.mmcs_nexus.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:events_repository_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS \"text\" AS CLOB",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions=true"
})
class EventsRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldFindEventsByYear() {
        Event event2025 = createEvent("Идея", EventType.IDEA, 2025);
        Event event2026 = createEvent("Релиз", EventType.RELEASE, 2026);

        eventRepository.saveAll(List.of(event2025, event2026));

        Page<Event> result = eventRepository.findByYear(
                2026,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Релиз", result.getContent().getFirst().getName());
        assertEquals(2026, result.getContent().getFirst().getYear());
    }

    @Test
    void shouldReturnEmptyPageWhenYearDoesNotMatch() {
        Event event = createEvent("Идея", EventType.IDEA, 2026);

        eventRepository.save(event);

        Page<Event> result = eventRepository.findByYear(
                2024,
                PageRequest.of(0, 10)
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void shouldApplySortingForFindByYear() {
        Event betaEvent = createEvent("Бета", EventType.IDEA, 2026);
        Event alphaEvent = createEvent("Альфа", EventType.ZERO_VERSION, 2026);
        Event gammaEvent = createEvent("Гамма", EventType.RELEASE, 2026);

        eventRepository.saveAll(List.of(betaEvent, alphaEvent, gammaEvent));

        Page<Event> result = eventRepository.findByYear(
                2026,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        );

        assertEquals(3, result.getTotalElements());
        assertEquals("Альфа", result.getContent().get(0).getName());
        assertEquals("Бета", result.getContent().get(1).getName());
        assertEquals("Гамма", result.getContent().get(2).getName());
    }

    @Test
    void shouldApplyPaginationForFindByYear() {
        Event firstEvent = createEvent("Альфа", EventType.IDEA, 2026);
        Event secondEvent = createEvent("Бета", EventType.ZERO_VERSION, 2026);
        Event thirdEvent = createEvent("Гамма", EventType.RELEASE, 2026);

        eventRepository.saveAll(List.of(firstEvent, secondEvent, thirdEvent));

        Page<Event> result = eventRepository.findByYear(
                2026,
                PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "name"))
        );

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Бета", result.getContent().getFirst().getName());
    }

    @Test
    void shouldFindDistinctEventYearsOrderedAsc() {
        Event firstEvent2026 = createEvent("Идея", EventType.IDEA, 2026);
        Event secondEvent2026 = createEvent("Релиз", EventType.RELEASE, 2026);
        Event event2024 = createEvent("Ноль", EventType.ZERO_VERSION, 2024);
        Event event2025 = createEvent("Предрелиз", EventType.PRE_RELEASE, 2025);

        eventRepository.saveAll(List.of(firstEvent2026, secondEvent2026, event2024, event2025));

        List<Integer> result = eventRepository.findAllEventsYears();

        assertEquals(List.of(2024, 2025, 2026), result);
    }

    private Event createEvent(String name, EventType eventType, int year) {
        Event event = new Event();

        event.setName(name);
        event.setEventType(eventType);
        event.setYear(year);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);

        return event;
    }
}