package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectEventSubmissionDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectEventSubmissionItemDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEventSubmission;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventSubmissionKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.SubmissionAvailabilityStatus;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.payload.user.GetProjectEventSubmissionsResponsePayload;
import ru.sfedu.mmcs_nexus.model.payload.user.SaveProjectEventSubmissionRequestPayload;
import ru.sfedu.mmcs_nexus.repository.EventRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectEventRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectEventSubmissionRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectRepository;
import ru.sfedu.mmcs_nexus.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectEventSubmissionsServiceTest {

    @Mock
    private ProjectEventSubmissionRepository projectEventSubmissionRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ProjectEventRepository projectEventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectEventSubmissionService projectEventSubmissionService;

    @Test
    void shouldReturnSubmissionsForProjectCaptain() {
        User captain = createUser("captain", UserEnums.UserStatus.VERIFIED);
        Project project = createProject(captain);

        Event closedEvent = createEvent("Идея", LocalDate.now().minusDays(10), LocalDate.now().minusDays(5));
        Event openEvent = createEvent("Промежуточная защита", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        Event futureEvent = createEvent("Итоговая защита", LocalDate.now().plusDays(5), LocalDate.now().plusDays(10));

        ProjectEventSubmission submission = createSubmission(project, openEvent, captain);

        when(userRepository.findByLogin("captain")).thenReturn(Optional.of(captain));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectEventRepository.findEventsByProjectId(project.getId(), null))
                .thenReturn(List.of(futureEvent, closedEvent, openEvent));
        when(projectEventSubmissionRepository.findAllByProjectId(project.getId()))
                .thenReturn(List.of(submission));

        GetProjectEventSubmissionsResponsePayload result =
                projectEventSubmissionService.getSubmissions(project.getId().toString(), "captain");

        assertEquals(openEvent.getId(), result.getDefaultEventId());
        assertEquals(3, result.getItems().size());

        ProjectEventSubmissionItemDTO closedItem = result.getItems().get(0);
        ProjectEventSubmissionItemDTO openItem = result.getItems().get(1);
        ProjectEventSubmissionItemDTO futureItem = result.getItems().get(2);

        assertEquals(closedEvent.getId(), closedItem.getEventId());
        assertEquals(SubmissionAvailabilityStatus.CLOSED, closedItem.getSubmissionStatus());
        assertFalse(closedItem.isEditable());

        assertEquals(openEvent.getId(), openItem.getEventId());
        assertEquals(SubmissionAvailabilityStatus.OPEN, openItem.getSubmissionStatus());
        assertTrue(openItem.isEditable());
        assertNotNull(openItem.getSubmission());
        assertEquals("https://github.com/teachyyou/mmcs_nexus", openItem.getSubmission().getRepositoryUrl());

        assertEquals(futureEvent.getId(), futureItem.getEventId());
        assertEquals(SubmissionAvailabilityStatus.FUTURE, futureItem.getSubmissionStatus());
        assertFalse(futureItem.isEditable());
    }

    @Test
    void shouldSaveNewSubmissionWhenEventIsOpen() {
        User captain = createUser("captain", UserEnums.UserStatus.VERIFIED);
        Project project = createProject(captain);
        Event event = createEvent("Промежуточная защита", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        SaveProjectEventSubmissionRequestPayload payload = createPayload();

        when(userRepository.findByLogin("captain")).thenReturn(Optional.of(captain));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);
        when(projectEventSubmissionRepository.findByProjectIdAndEventId(project.getId(), event.getId()))
                .thenReturn(Optional.empty());
        when(projectEventSubmissionRepository.save(any(ProjectEventSubmission.class)))
                .thenAnswer(invocation -> {
                    ProjectEventSubmission submission = invocation.getArgument(0);
                    submission.setCreatedAt(LocalDateTime.of(2026, 5, 24, 12, 0));
                    submission.setUpdatedAt(LocalDateTime.of(2026, 5, 24, 12, 0));
                    return submission;
                });

        ProjectEventSubmissionDTO result = projectEventSubmissionService.saveSubmission(
                project.getId().toString(),
                event.getId().toString(),
                "captain",
                payload
        );

        assertEquals(project.getId(), result.getProjectId());
        assertEquals(event.getId(), result.getEventId());
        assertEquals("https://docs.google.com/presentation/d/example", result.getPresentationUrl());
        assertEquals("https://github.com/teachyyou/mmcs_nexus", result.getRepositoryUrl());
        assertEquals("https://github.com/teachyyou/mmcs_nexus/releases/tag/v1", result.getReleaseUrl());
        assertEquals("Комментарий", result.getComment());
        assertEquals("captain", result.getSubmittedByLogin());

        ArgumentCaptor<ProjectEventSubmission> submissionCaptor =
                ArgumentCaptor.forClass(ProjectEventSubmission.class);

        verify(projectEventSubmissionRepository).save(submissionCaptor.capture());

        ProjectEventSubmission savedSubmission = submissionCaptor.getValue();

        assertEquals(project, savedSubmission.getProject());
        assertEquals(event, savedSubmission.getEvent());
        assertEquals(captain, savedSubmission.getSubmittedBy());
    }

    @Test
    void shouldUpdateExistingSubmissionWhenEventIsOpen() {
        User captain = createUser("captain", UserEnums.UserStatus.VERIFIED);
        Project project = createProject(captain);
        Event event = createEvent("Промежуточная защита", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        ProjectEventSubmission existingSubmission = createSubmission(project, event, captain);
        SaveProjectEventSubmissionRequestPayload payload = createPayload();

        payload.setComment("Новый комментарий");

        when(userRepository.findByLogin("captain")).thenReturn(Optional.of(captain));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);
        when(projectEventSubmissionRepository.findByProjectIdAndEventId(project.getId(), event.getId()))
                .thenReturn(Optional.of(existingSubmission));
        when(projectEventSubmissionRepository.save(existingSubmission)).thenReturn(existingSubmission);

        ProjectEventSubmissionDTO result = projectEventSubmissionService.saveSubmission(
                project.getId().toString(),
                event.getId().toString(),
                "captain",
                payload
        );

        assertEquals("Новый комментарий", result.getComment());
        assertEquals("Новый комментарий", existingSubmission.getComment());

        verify(projectEventSubmissionRepository).save(existingSubmission);
    }

    @Test
    void shouldTrimBlankValuesWhenSavingSubmission() {
        User captain = createUser("captain", UserEnums.UserStatus.VERIFIED);
        Project project = createProject(captain);
        Event event = createEvent("Промежуточная защита", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        SaveProjectEventSubmissionRequestPayload payload = new SaveProjectEventSubmissionRequestPayload();

        payload.setPresentationUrl("  https://example.com/presentation  ");
        payload.setRepositoryUrl("   ");
        payload.setReleaseUrl(null);
        payload.setComment("  Комментарий  ");

        when(userRepository.findByLogin("captain")).thenReturn(Optional.of(captain));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);
        when(projectEventSubmissionRepository.findByProjectIdAndEventId(project.getId(), event.getId()))
                .thenReturn(Optional.empty());
        when(projectEventSubmissionRepository.save(any(ProjectEventSubmission.class)))
                .thenAnswer(invocation -> {
                    ProjectEventSubmission submission = invocation.getArgument(0);
                    submission.setCreatedAt(LocalDateTime.of(2026, 5, 24, 12, 0));
                    submission.setUpdatedAt(LocalDateTime.of(2026, 5, 24, 12, 0));
                    return submission;
                });

        ProjectEventSubmissionDTO result = projectEventSubmissionService.saveSubmission(
                project.getId().toString(),
                event.getId().toString(),
                "captain",
                payload
        );

        assertEquals("https://example.com/presentation", result.getPresentationUrl());
        assertNull(result.getRepositoryUrl());
        assertNull(result.getReleaseUrl());
        assertEquals("Комментарий", result.getComment());
    }

    @Test
    void shouldThrowWhenUserIsNotProjectCaptain() {
        User captain = createUser("captain", UserEnums.UserStatus.VERIFIED);
        User otherUser = createUser("other", UserEnums.UserStatus.VERIFIED);
        Project project = createProject(captain);
        Event event = createEvent("Промежуточная защита", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        when(userRepository.findByLogin("other")).thenReturn(Optional.of(otherUser));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectEventSubmissionService.saveSubmission(
                        project.getId().toString(),
                        event.getId().toString(),
                        "other",
                        createPayload()
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Only project captain can manage submissions", exception.getReason());

        verify(projectEventSubmissionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSubmissionWindowIsNotConfigured() {
        User captain = createUser("captain", UserEnums.UserStatus.VERIFIED);
        Project project = createProject(captain);
        Event event = createEvent("Без дат", null, null);

        when(userRepository.findByLogin("captain")).thenReturn(Optional.of(captain));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectEventSubmissionService.saveSubmission(
                        project.getId().toString(),
                        event.getId().toString(),
                        "captain",
                        createPayload()
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Submission window is not configured", exception.getReason());

        verify(projectEventSubmissionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSubmissionIsFuture() {
        User captain = createUser("captain", UserEnums.UserStatus.VERIFIED);
        Project project = createProject(captain);
        Event event = createEvent("Будущий этап", LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        when(userRepository.findByLogin("captain")).thenReturn(Optional.of(captain));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectEventSubmissionService.saveSubmission(
                        project.getId().toString(),
                        event.getId().toString(),
                        "captain",
                        createPayload()
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Submission is not available yet", exception.getReason());

        verify(projectEventSubmissionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSubmissionIsClosed() {
        User captain = createUser("captain", UserEnums.UserStatus.VERIFIED);
        Project project = createProject(captain);
        Event event = createEvent("Прошедший этап", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));

        when(userRepository.findByLogin("captain")).thenReturn(Optional.of(captain));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectEventSubmissionService.saveSubmission(
                        project.getId().toString(),
                        event.getId().toString(),
                        "captain",
                        createPayload()
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Submission deadline has passed", exception.getReason());

        verify(projectEventSubmissionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenProjectAndEventAreNotLinked() {
        User captain = createUser("captain", UserEnums.UserStatus.VERIFIED);
        Project project = createProject(captain);
        Event event = createEvent("Промежуточная защита", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        when(userRepository.findByLogin("captain")).thenReturn(Optional.of(captain));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectEventSubmissionService.saveSubmission(
                        project.getId().toString(),
                        event.getId().toString(),
                        "captain",
                        createPayload()
                )
        );

        assertEquals("Given project and Event are not linked", exception.getMessage());

        verify(projectEventSubmissionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserIsNotVerified() {
        User user = createUser("captain", UserEnums.UserStatus.NON_VERIFIED);

        when(userRepository.findByLogin("captain")).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectEventSubmissionService.getSubmissions(UUID.randomUUID().toString(), "captain")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("User is not verified", exception.getReason());
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> projectEventSubmissionService.getSubmissions(UUID.randomUUID().toString(), "unknown")
        );

        assertEquals("User unknown is not found", exception.getMessage());
    }

    private SaveProjectEventSubmissionRequestPayload createPayload() {
        SaveProjectEventSubmissionRequestPayload payload = new SaveProjectEventSubmissionRequestPayload();

        payload.setPresentationUrl("https://docs.google.com/presentation/d/example");
        payload.setRepositoryUrl("https://github.com/teachyyou/mmcs_nexus");
        payload.setReleaseUrl("https://github.com/teachyyou/mmcs_nexus/releases/tag/v1");
        payload.setComment("Комментарий");

        return payload;
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
        submission.setCreatedAt(LocalDateTime.of(2026, 5, 24, 12, 0));
        submission.setUpdatedAt(LocalDateTime.of(2026, 5, 24, 13, 0));

        return submission;
    }

    private Project createProject(User captain) {
        Project project = new Project();

        project.setId(UUID.randomUUID());
        project.setExternalId(1001);
        project.setQuantityOfStudents(4);
        project.setCaptainName("Иван Иванов");
        project.setFull(true);
        project.setTrack("Backend");
        project.setTechnologies("Java, Spring");
        project.setName("MMCS Nexus");
        project.setDescription("Описание проекта");
        project.setType("WEB_APP");
        project.setYear(2026);
        project.setCaptain(captain);

        return project;
    }

    private Event createEvent(String name, LocalDate submissionStartDate, LocalDate submissionDeadlineDate) {
        Event event = new Event();

        event.setId(UUID.randomUUID());
        event.setName(name);
        event.setEventType(EventType.IDEA);
        event.setYear(2026);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);
        event.setSubmissionStartDate(submissionStartDate);
        event.setSubmissionDeadlineDate(submissionDeadlineDate);

        return event;
    }

    private User createUser(String login, UserEnums.UserStatus status) {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setLogin(login);
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail(login + "@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_USER);
        user.setStatus(status);

        return user;
    }
}