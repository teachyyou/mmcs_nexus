package ru.sfedu.mmcs_nexus.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEvent;
import ru.sfedu.mmcs_nexus.model.entity.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectJuryEventKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.JuryRelationType;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:project_jury_events_repository_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS \"text\" AS CLOB",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions=true"
})
class ProjectJuryEventsRepositoryTest {

    @Autowired
    private ProjectJuryEventRepository projectJuryEventRepository;

    @Autowired
    private ProjectEventRepository projectEventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindMentorsByProjectIdAndEventId() {
        Project project = projectRepository.save(createProject("MMCS Nexus", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));

        User mentor = userRepository.save(createUser("mentor"));
        User obligedJury = userRepository.save(createUser("obliged"));

        projectJuryEventRepository.save(createLink(project, event, mentor, JuryRelationType.MENTOR));
        projectJuryEventRepository.save(createLink(project, event, obligedJury, JuryRelationType.OBLIGED));

        List<User> result = projectJuryEventRepository.findMentorsByProjectIdAndEventId(
                event.getId(),
                project.getId()
        );

        assertEquals(1, result.size());
        assertEquals("mentor", result.getFirst().getLogin());
    }

    @Test
    void shouldFindByProjectIdAndEventId() {
        Project project = projectRepository.save(createProject("MMCS Nexus", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));

        User mentor = userRepository.save(createUser("mentor"));
        User willingJury = userRepository.save(createUser("willing"));

        projectJuryEventRepository.save(createLink(project, event, mentor, JuryRelationType.MENTOR));
        projectJuryEventRepository.save(createLink(project, event, willingJury, JuryRelationType.WILLING));

        List<ProjectJuryEvent> result = projectJuryEventRepository.findByProjectIdAndEventId(
                project.getId(),
                event.getId()
        );

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(link -> link.getProject().getId().equals(project.getId())));
        assertTrue(result.stream().allMatch(link -> link.getEvent().getId().equals(event.getId())));
    }

    @Test
    void shouldFindByProjectIdAndEventIdAndJuryId() {
        Project project = projectRepository.save(createProject("MMCS Nexus", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));
        User jury = userRepository.save(createUser("jury"));

        projectJuryEventRepository.save(createLink(project, event, jury, JuryRelationType.WILLING));

        Optional<ProjectJuryEvent> result = projectJuryEventRepository.findByProjectIdAndEventIdAndJuryId(
                project.getId(),
                event.getId(),
                jury.getId()
        );

        assertTrue(result.isPresent());
        assertEquals(JuryRelationType.WILLING, result.get().getRelationType());
    }

    @Test
    void shouldDeleteByProjectAndEvent() {
        Project project = projectRepository.save(createProject("MMCS Nexus", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));

        User firstJury = userRepository.save(createUser("first"));
        User secondJury = userRepository.save(createUser("second"));

        projectJuryEventRepository.save(createLink(project, event, firstJury, JuryRelationType.MENTOR));
        projectJuryEventRepository.save(createLink(project, event, secondJury, JuryRelationType.WILLING));

        assertEquals(2, projectJuryEventRepository.findByProjectIdAndEventId(project.getId(), event.getId()).size());

        projectJuryEventRepository.deleteByProjectAndEvent(project.getId(), event.getId());
        projectJuryEventRepository.flush();

        assertTrue(projectJuryEventRepository.findByProjectIdAndEventId(project.getId(), event.getId()).isEmpty());
    }

    @Test
    void shouldFindProjectsByEventAssignedToJuryWithoutDayFilter() {
        Project firstProject = projectRepository.save(createProject("Alpha", 2026));
        Project secondProject = projectRepository.save(createProject("Beta", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));
        User jury = userRepository.save(createUser("jury"));

        linkProjectToEvent(firstProject, event, 1);
        linkProjectToEvent(secondProject, event, 2);

        projectJuryEventRepository.save(createLink(firstProject, event, jury, JuryRelationType.OBLIGED));
        projectJuryEventRepository.save(createLink(secondProject, event, jury, JuryRelationType.WILLING));

        List<Project> result = projectJuryEventRepository.findProjectByEventAssignedToJury(
                event.getId(),
                jury.getId(),
                null
        );

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(project -> project.getName().equals("Alpha")));
        assertTrue(result.stream().anyMatch(project -> project.getName().equals("Beta")));
    }

    @Test
    void shouldFindProjectsByEventAssignedToJuryWithDayFilter() {
        Project firstProject = projectRepository.save(createProject("Alpha", 2026));
        Project secondProject = projectRepository.save(createProject("Beta", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));
        User jury = userRepository.save(createUser("jury"));

        linkProjectToEvent(firstProject, event, 1);
        linkProjectToEvent(secondProject, event, 2);

        projectJuryEventRepository.save(createLink(firstProject, event, jury, JuryRelationType.OBLIGED));
        projectJuryEventRepository.save(createLink(secondProject, event, jury, JuryRelationType.WILLING));

        List<Project> result = projectJuryEventRepository.findProjectByEventAssignedToJury(
                event.getId(),
                jury.getId(),
                1
        );

        assertEquals(1, result.size());
        assertEquals("Alpha", result.getFirst().getName());
    }

    @Test
    void shouldFindProjectsByEventMentoredByJuryWithDayFilter() {
        Project mentoredProject = projectRepository.save(createProject("Mentored", 2026));
        Project obligedProject = projectRepository.save(createProject("Obliged", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));
        User jury = userRepository.save(createUser("jury"));

        linkProjectToEvent(mentoredProject, event, 1);
        linkProjectToEvent(obligedProject, event, 1);

        projectJuryEventRepository.save(createLink(mentoredProject, event, jury, JuryRelationType.MENTOR));
        projectJuryEventRepository.save(createLink(obligedProject, event, jury, JuryRelationType.OBLIGED));

        List<Project> result = projectJuryEventRepository.findProjectByEventMentoredByJury(
                event.getId(),
                jury.getId(),
                1
        );

        assertEquals(1, result.size());
        assertEquals("Mentored", result.getFirst().getName());
    }

    @Test
    void shouldFindJuriesForProjectsAssignedToJuryByEvent() {
        Project project = projectRepository.save(createProject("MMCS Nexus", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));

        User baseJury = userRepository.save(createUser("base"));
        User otherJury = userRepository.save(createUser("other"));

        linkProjectToEvent(project, event, 1);

        projectJuryEventRepository.save(createLink(project, event, baseJury, JuryRelationType.WILLING));
        projectJuryEventRepository.save(createLink(project, event, otherJury, JuryRelationType.OBLIGED));

        List<User> result = projectJuryEventRepository.findJuriesForProjectsAssignedToJuryByEvent(
                event.getId(),
                baseJury.getId(),
                1
        );

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getLogin().equals("base")));
        assertTrue(result.stream().anyMatch(user -> user.getLogin().equals("other")));
    }

    @Test
    void shouldFindJuriesForProjectsMentoredByJuryByEvent() {
        Project mentoredProject = projectRepository.save(createProject("Mentored", 2026));
        Project unrelatedProject = projectRepository.save(createProject("Unrelated", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));

        User mentor = userRepository.save(createUser("mentor"));
        User colleague = userRepository.save(createUser("colleague"));

        linkProjectToEvent(mentoredProject, event, 1);
        linkProjectToEvent(unrelatedProject, event, 1);

        projectJuryEventRepository.save(createLink(mentoredProject, event, mentor, JuryRelationType.MENTOR));
        projectJuryEventRepository.save(createLink(mentoredProject, event, colleague, JuryRelationType.OBLIGED));
        projectJuryEventRepository.save(createLink(unrelatedProject, event, colleague, JuryRelationType.OBLIGED));

        List<User> result = projectJuryEventRepository.findJuriesForProjectsMentoredByJuryByEvent(
                event.getId(),
                mentor.getId(),
                1
        );

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getLogin().equals("mentor")));
        assertTrue(result.stream().anyMatch(user -> user.getLogin().equals("colleague")));
    }

    @Test
    void shouldFindJuriesByEventIdAndDay() {
        Project firstProject = projectRepository.save(createProject("First", 2026));
        Project secondProject = projectRepository.save(createProject("Second", 2026));
        Event event = eventRepository.save(createEvent("Идея", 2026));

        User firstJury = userRepository.save(createUser("first"));
        User secondJury = userRepository.save(createUser("second"));

        linkProjectToEvent(firstProject, event, 1);
        linkProjectToEvent(secondProject, event, 2);

        projectJuryEventRepository.save(createLink(firstProject, event, firstJury, JuryRelationType.MENTOR));
        projectJuryEventRepository.save(createLink(secondProject, event, secondJury, JuryRelationType.WILLING));

        List<User> result = projectJuryEventRepository.findJuriesByEventId(event.getId(), 1);

        assertEquals(1, result.size());
        assertEquals("first", result.getFirst().getLogin());
    }

    private void linkProjectToEvent(Project project, Event event, Integer day) {
        projectEventRepository.save(new ProjectEvent(
                new ProjectEventKey(project.getId(), event.getId()),
                event,
                project,
                day
        ));
    }

    private ProjectJuryEvent createLink(Project project, Event event, User jury, JuryRelationType relationType) {
        return new ProjectJuryEvent(
                new ProjectJuryEventKey(project.getId(), jury.getId(), event.getId()),
                jury,
                project,
                event,
                relationType
        );
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

    private Event createEvent(String name, int year) {
        Event event = new Event();

        event.setName(name);
        event.setEventType(EventType.IDEA);
        event.setYear(year);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);

        return event;
    }

    private User createUser(String login) {
        User user = new User();

        user.setLogin(login);
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail(login + "@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_JURY);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }
}