package ru.sfedu.mmcs_nexus.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEvent;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEventSubmission;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventSubmissionKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:project_event_submissions_repository_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS \"text\" AS CLOB",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions=true"
})
class ProjectEventSubmissionsRepositoryTest {

    @Autowired
    private ProjectEventSubmissionRepository projectEventSubmissionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectEventRepository projectEventRepository;

    @Test
    void shouldSaveAndFindSubmissionByProjectIdAndEventId() {
        User captain = userRepository.save(createUser("captain"));
        Project project = projectRepository.save(createProject(captain));
        Event event = eventRepository.save(createEvent("Промежуточная защита"));

        projectEventRepository.save(createProjectEvent(project, event));

        ProjectEventSubmission submission = createSubmission(project, event, captain);

        projectEventSubmissionRepository.save(submission);

        Optional<ProjectEventSubmission> result =
                projectEventSubmissionRepository.findByProjectIdAndEventId(project.getId(), event.getId());

        assertTrue(result.isPresent());
        assertEquals(project.getId(), result.get().getProject().getId());
        assertEquals(event.getId(), result.get().getEvent().getId());
        assertEquals(captain.getId(), result.get().getSubmittedBy().getId());
        assertEquals("https://docs.google.com/presentation/d/example", result.get().getPresentationUrl());
        assertEquals("https://github.com/teachyyou/mmcs_nexus", result.get().getRepositoryUrl());
        assertEquals("https://github.com/teachyyou/mmcs_nexus/releases/tag/v1", result.get().getReleaseUrl());
        assertEquals("Комментарий", result.get().getComment());
    }

    @Test
    void shouldFindAllSubmissionsByProjectId() {
        User captain = userRepository.save(createUser("captain"));
        Project project = projectRepository.save(createProject(captain));
        Project otherProject = projectRepository.save(createProject(captain));
        Event firstEvent = eventRepository.save(createEvent("Идея"));
        Event secondEvent = eventRepository.save(createEvent("Промежуточная защита"));
        Event thirdEvent = eventRepository.save(createEvent("Итоговая защита"));

        projectEventRepository.save(createProjectEvent(project, firstEvent));
        projectEventRepository.save(createProjectEvent(project, secondEvent));
        projectEventRepository.save(createProjectEvent(otherProject, thirdEvent));

        projectEventSubmissionRepository.save(createSubmission(project, firstEvent, captain));
        projectEventSubmissionRepository.save(createSubmission(project, secondEvent, captain));
        projectEventSubmissionRepository.save(createSubmission(otherProject, thirdEvent, captain));

        List<ProjectEventSubmission> result = projectEventSubmissionRepository.findAllByProjectId(project.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(submission -> submission.getProject().getId().equals(project.getId())));
        assertTrue(result.stream().anyMatch(submission -> submission.getEvent().getId().equals(firstEvent.getId())));
        assertTrue(result.stream().anyMatch(submission -> submission.getEvent().getId().equals(secondEvent.getId())));
    }

    @Test
    void shouldReturnEmptyListWhenProjectHasNoSubmissions() {
        User captain = userRepository.save(createUser("captain"));
        Project project = projectRepository.save(createProject(captain));

        List<ProjectEventSubmission> result = projectEventSubmissionRepository.findAllByProjectId(project.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUpdateExistingSubmission() {
        User captain = userRepository.save(createUser("captain"));
        Project project = projectRepository.save(createProject(captain));
        Event event = eventRepository.save(createEvent("Промежуточная защита"));

        projectEventRepository.save(createProjectEvent(project, event));

        ProjectEventSubmission submission = projectEventSubmissionRepository.save(createSubmission(project, event, captain));

        submission.setPresentationUrl("https://example.com/new-presentation");
        submission.setRepositoryUrl("https://github.com/example/new-repository");
        submission.setReleaseUrl("https://github.com/example/new-repository/releases/tag/v2");
        submission.setComment("Новый комментарий");

        projectEventSubmissionRepository.save(submission);

        ProjectEventSubmission result = projectEventSubmissionRepository.findById(
                new ProjectEventSubmissionKey(project.getId(), event.getId())
        ).orElseThrow();

        assertEquals("https://example.com/new-presentation", result.getPresentationUrl());
        assertEquals("https://github.com/example/new-repository", result.getRepositoryUrl());
        assertEquals("https://github.com/example/new-repository/releases/tag/v2", result.getReleaseUrl());
        assertEquals("Новый комментарий", result.getComment());
    }

    private ProjectEventSubmission createSubmission(Project project, Event event, User user) {
        ProjectEventSubmission submission = new ProjectEventSubmission();

        submission.setId(new ProjectEventSubmissionKey(project.getId(), event.getId()));
        submission.setProject(project);
        submission.setEvent(event);
        submission.setSubmittedBy(user);
        submission.setPresentationUrl("https://docs.google.com/presentation/d/example");
        submission.setRepositoryUrl("https://github.com/teachyyou/mmcs_nexus");
        submission.setReleaseUrl("https://github.com/teachyyou/mmcs_nexus/releases/tag/v1");
        submission.setComment("Комментарий");

        return submission;
    }

    private ProjectEvent createProjectEvent(Project project, Event event) {
        ProjectEvent projectEvent = new ProjectEvent();

        projectEvent.setId(new ProjectEventKey(project.getId(), event.getId()));
        projectEvent.setProject(project);
        projectEvent.setEvent(event);
        projectEvent.setDefDay(1);

        return projectEvent;
    }

    private Project createProject(User captain) {
        Project project = new Project();

        project.setExternalId(1001);
        project.setQuantityOfStudents(4);
        project.setCaptainName("Иван Иванов");
        project.setFull(true);
        project.setTrack("Backend");
        project.setTechnologies("Java, Spring");
        project.setName("MMCS Nexus " + java.util.UUID.randomUUID());
        project.setDescription("Описание проекта");
        project.setType("WEB_APP");
        project.setYear(2026);
        project.setCaptain(captain);

        return project;
    }

    private Event createEvent(String name) {
        Event event = new Event();

        event.setName(name);
        event.setEventType(EventType.IDEA);
        event.setYear(2026);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);
        event.setSubmissionStartDate(LocalDate.now().minusDays(1));
        event.setSubmissionDeadlineDate(LocalDate.now().plusDays(1));

        return event;
    }

    private User createUser(String login) {
        User user = new User();

        user.setLogin(login + java.util.UUID.randomUUID());
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail(login + java.util.UUID.randomUUID() + "@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_USER);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }
}