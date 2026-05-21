package ru.sfedu.mmcs_nexus.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEvent;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:project_events_repository_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS \"text\" AS CLOB",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions=true"
})
class ProjectEventsRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProjectEventRepository projectEventRepository;

    @Test
    void shouldFindProjectsByEventId() {
        Event event = eventRepository.save(createEvent("Идея", EventType.IDEA, 2026));
        Project alphaProject = projectRepository.save(createProject("Alpha", 2026));
        Project betaProject = projectRepository.save(createProject("Beta", 2026));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(betaProject.getId(), event.getId()),
                event,
                betaProject
        ));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(alphaProject.getId(), event.getId()),
                event,
                alphaProject
        ));

        Page<Project> result = projectEventRepository.findProjectsByEventId(
                event.getId(),
                null,
                PageRequest.of(0, 10)
        );

        assertEquals(2, result.getTotalElements());
        assertEquals("Alpha", result.getContent().get(0).getName());
        assertEquals("Beta", result.getContent().get(1).getName());
    }

    @Test
    void shouldFindProjectsByEventIdAndDay() {
        Event event = eventRepository.save(createEvent("Идея", EventType.IDEA, 2026));
        Project firstDayProject = projectRepository.save(createProject("First day", 2026));
        Project secondDayProject = projectRepository.save(createProject("Second day", 2026));
        Project noDayProject = projectRepository.save(createProject("No day", 2026));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(firstDayProject.getId(), event.getId()),
                event,
                firstDayProject,
                1
        ));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(secondDayProject.getId(), event.getId()),
                event,
                secondDayProject,
                2
        ));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(noDayProject.getId(), event.getId()),
                event,
                noDayProject,
                null
        ));

        List<Project> result = projectEventRepository.findProjectsByEventId(event.getId(), 1);

        assertEquals(1, result.size());
        assertEquals("First day", result.getFirst().getName());
    }

    @Test
    void shouldFindEventsByProjectId() {
        Project project = projectRepository.save(createProject("MMCS Nexus", 2026));
        Event alphaEvent = eventRepository.save(createEvent("Альфа", EventType.IDEA, 2026));
        Event betaEvent = eventRepository.save(createEvent("Бета", EventType.RELEASE, 2026));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(project.getId(), betaEvent.getId()),
                betaEvent,
                project
        ));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(project.getId(), alphaEvent.getId()),
                alphaEvent,
                project
        ));

        Page<Event> result = projectEventRepository.findEventsByProjectId(
                project.getId(),
                null,
                PageRequest.of(0, 10)
        );

        assertEquals(2, result.getTotalElements());
        assertEquals("Альфа", result.getContent().get(0).getName());
        assertEquals("Бета", result.getContent().get(1).getName());
    }

    @Test
    void shouldFindEventsByProjectIdAndDay() {
        Project project = projectRepository.save(createProject("MMCS Nexus", 2026));
        Event firstEvent = eventRepository.save(createEvent("Первый день", EventType.IDEA, 2026));
        Event secondEvent = eventRepository.save(createEvent("Второй день", EventType.RELEASE, 2026));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(project.getId(), firstEvent.getId()),
                firstEvent,
                project,
                1
        ));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(project.getId(), secondEvent.getId()),
                secondEvent,
                project,
                2
        ));

        List<Event> result = projectEventRepository.findEventsByProjectId(project.getId(), 2);

        assertEquals(1, result.size());
        assertEquals("Второй день", result.getFirst().getName());
    }

    @Test
    void shouldFindLinksByEventId() {
        Event event = eventRepository.save(createEvent("Идея", EventType.IDEA, 2026));
        Project firstProject = projectRepository.save(createProject("First", 2026));
        Project secondProject = projectRepository.save(createProject("Second", 2026));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(firstProject.getId(), event.getId()),
                event,
                firstProject,
                1
        ));

        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(secondProject.getId(), event.getId()),
                event,
                secondProject,
                2
        ));

        List<ProjectEvent> result = projectEventRepository.findByEventId(event.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(link -> link.getEvent().getId().equals(event.getId())));
    }

    @Test
    void shouldApplyPaginationForProjectsByEventId() {
        Event event = eventRepository.save(createEvent("Идея", EventType.IDEA, 2026));
        Project alphaProject = projectRepository.save(createProject("Alpha", 2026));
        Project betaProject = projectRepository.save(createProject("Beta", 2026));
        Project gammaProject = projectRepository.save(createProject("Gamma", 2026));

        projectEventRepository.saveAll(List.of(
                new ProjectEvent(new ProjectEventKey(alphaProject.getId(), event.getId()), event, alphaProject),
                new ProjectEvent(new ProjectEventKey(betaProject.getId(), event.getId()), event, betaProject),
                new ProjectEvent(new ProjectEventKey(gammaProject.getId(), event.getId()), event, gammaProject)
        ));

        Page<Project> result = projectEventRepository.findProjectsByEventId(
                event.getId(),
                null,
                PageRequest.of(1, 1)
        );

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Beta", result.getContent().getFirst().getName());
    }

    @Test
    void shouldReturnEmptyPageWhenEventHasNoProjects() {
        Event event = eventRepository.save(createEvent("Идея", EventType.IDEA, 2026));

        Page<Project> result = projectEventRepository.findProjectsByEventId(
                event.getId(),
                null,
                PageRequest.of(0, 10)
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    private Project createProject(String name, int year) {
        Project project = new Project();

        project.setExternalId(year);
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

        event.setName(name);
        event.setEventType(eventType);
        event.setYear(year);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);

        return event;
    }
}