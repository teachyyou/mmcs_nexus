package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectJuryEventKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.JuryRelationType;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.payload.admin.AssignJuriesRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.ProjectJuryEventResponsePayload;
import ru.sfedu.mmcs_nexus.repository.EventRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectEventRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectJuryEventRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectRepository;
import ru.sfedu.mmcs_nexus.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectJuryEventsServiceTest {

    @Mock
    private ProjectJuryEventRepository projectJuryEventRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectEventRepository projectEventRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectJuryEventService projectJuryEventService;

    @Test
    void shouldAssignJuriesToSingleProjectEvent() {
        Project project = createProject("MMCS Nexus", 2026);
        Event event = createEvent("Идея", 2026);

        User mentor = createUser("mentor");
        User obligedJury = createUser("obliged");
        User willingJury = createUser("willing");

        AssignJuriesRequestPayload payload = createPayload(
                project.getId(),
                event.getId(),
                List.of(mentor.getId()),
                List.of(obligedJury.getId()),
                List.of(willingJury.getId()),
                false
        );

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findAllById(List.of(mentor.getId()))).thenReturn(List.of(mentor));
        when(userRepository.findAllById(List.of(obligedJury.getId()))).thenReturn(List.of(obligedJury));
        when(userRepository.findAllById(List.of(willingJury.getId()))).thenReturn(List.of(willingJury));

        projectJuryEventService.assignJuries(payload);

        verify(projectJuryEventRepository).deleteByProjectAndEvent(project.getId(), event.getId());

        ArgumentCaptor<Iterable<ProjectJuryEvent>> linksCaptor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(projectJuryEventRepository, times(3)).saveAll(linksCaptor.capture());

        List<ProjectJuryEvent> savedLinks = flatten(linksCaptor.getAllValues());

        assertEquals(3, savedLinks.size());

        assertTrue(savedLinks.stream().anyMatch(link ->
                link.getJury().equals(mentor)
                        && link.getProject().equals(project)
                        && link.getEvent().equals(event)
                        && link.getRelationType() == JuryRelationType.MENTOR
        ));

        assertTrue(savedLinks.stream().anyMatch(link ->
                link.getJury().equals(obligedJury)
                        && link.getRelationType() == JuryRelationType.OBLIGED
        ));

        assertTrue(savedLinks.stream().anyMatch(link ->
                link.getJury().equals(willingJury)
                        && link.getRelationType() == JuryRelationType.WILLING
        ));
    }

    @Test
    void shouldAssignOnlyMentorsWhenOtherListsAreEmpty() {
        Project project = createProject("MMCS Nexus", 2026);
        Event event = createEvent("Идея", 2026);
        User mentor = createUser("mentor");

        AssignJuriesRequestPayload payload = createPayload(
                project.getId(),
                event.getId(),
                List.of(mentor.getId()),
                List.of(),
                List.of(),
                false
        );

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findAllById(List.of(mentor.getId()))).thenReturn(List.of(mentor));

        projectJuryEventService.assignJuries(payload);

        verify(projectJuryEventRepository).deleteByProjectAndEvent(project.getId(), event.getId());
        verify(projectJuryEventRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldAssignJuriesToAllProjectEvents() {
        Project project = createProject("MMCS Nexus", 2026);
        Event ideaEvent = createEvent("Идея", 2026);
        Event releaseEvent = createEvent("Релиз", 2026);
        User mentor = createUser("mentor");

        AssignJuriesRequestPayload payload = createPayload(
                project.getId(),
                ideaEvent.getId(),
                List.of(mentor.getId()),
                List.of(),
                List.of(),
                true
        );

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectEventRepository.findEventsByProjectId(project.getId(), null))
                .thenReturn(List.of(ideaEvent, releaseEvent));
        when(userRepository.findAllById(List.of(mentor.getId()))).thenReturn(List.of(mentor));

        projectJuryEventService.assignJuries(payload);

        verify(eventRepository, never()).findById(any());
        verify(projectJuryEventRepository).deleteByProjectAndEvent(project.getId(), ideaEvent.getId());
        verify(projectJuryEventRepository).deleteByProjectAndEvent(project.getId(), releaseEvent.getId());
        verify(projectJuryEventRepository, times(2)).saveAll(anyList());
    }

    @Test
    void shouldThrowWhenAssigningJuriesToUnknownProject() {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        AssignJuriesRequestPayload payload = createPayload(
                projectId,
                eventId,
                List.of(),
                List.of(),
                List.of(),
                false
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectJuryEventService.assignJuries(payload)
        );

        assertEquals("Project with id " + projectId + " not found", exception.getMessage());

        verify(eventRepository, never()).findById(any());
        verify(projectEventRepository, never()).findEventsByProjectId(any(), any());
        verify(projectJuryEventRepository, never()).deleteByProjectAndEvent(any(), any());
        verify(projectJuryEventRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowWhenAssigningJuriesToUnknownEvent() {
        Project project = createProject("MMCS Nexus", 2026);
        UUID eventId = UUID.randomUUID();

        AssignJuriesRequestPayload payload = createPayload(
                project.getId(),
                eventId,
                List.of(),
                List.of(),
                List.of(),
                false
        );

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectJuryEventService.assignJuries(payload)
        );

        assertEquals("Event with id " + eventId + " not found", exception.getMessage());

        verify(projectJuryEventRepository, never()).deleteByProjectAndEvent(any(), any());
        verify(projectJuryEventRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowWhenOneOfJuriesDoesNotExist() {
        Project project = createProject("MMCS Nexus", 2026);
        Event event = createEvent("Идея", 2026);
        UUID existingJuryId = UUID.randomUUID();
        UUID missingJuryId = UUID.randomUUID();

        User existingJury = createUser("existing");
        existingJury.setId(existingJuryId);

        AssignJuriesRequestPayload payload = createPayload(
                project.getId(),
                event.getId(),
                List.of(existingJuryId, missingJuryId),
                List.of(),
                List.of(),
                false
        );

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findAllById(List.of(existingJuryId, missingJuryId))).thenReturn(List.of(existingJury));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectJuryEventService.assignJuries(payload)
        );

        assertEquals("Jury with ids [" + missingJuryId + "] not found", exception.getMessage());

        verify(projectJuryEventRepository).deleteByProjectAndEvent(project.getId(), event.getId());
        verify(projectJuryEventRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldReturnJuriesByProjectAndEventGroupedByRelationType() {
        Project project = createProject("MMCS Nexus", 2026);
        Event event = createEvent("Идея", 2026);

        User mentor = createUser("mentor");
        User obligedJury = createUser("obliged");
        User willingJury = createUser("willing");

        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(eventRepository.existsById(event.getId())).thenReturn(true);
        when(projectJuryEventRepository.findByProjectIdAndEventId(project.getId(), event.getId()))
                .thenReturn(List.of(
                        createLink(project, event, mentor, JuryRelationType.MENTOR),
                        createLink(project, event, obligedJury, JuryRelationType.OBLIGED),
                        createLink(project, event, willingJury, JuryRelationType.WILLING)
                ));

        ProjectJuryEventResponsePayload result =
                projectJuryEventService.getJuriesByProjectAndEvent(project.getId().toString(), event.getId().toString());

        assertEquals(1, result.getMentors().size());
        assertEquals("mentor", result.getMentors().getFirst().getLogin());

        assertEquals(1, result.getObligedJuries().size());
        assertEquals("obliged", result.getObligedJuries().getFirst().getLogin());

        assertEquals(1, result.getWillingJuries().size());
        assertEquals("willing", result.getWillingJuries().getFirst().getLogin());
    }

    @Test
    void shouldReturnEmptyJuryGroupsWhenNoLinksExist() {
        Project project = createProject("MMCS Nexus", 2026);
        Event event = createEvent("Идея", 2026);

        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(eventRepository.existsById(event.getId())).thenReturn(true);
        when(projectJuryEventRepository.findByProjectIdAndEventId(project.getId(), event.getId()))
                .thenReturn(List.of());

        ProjectJuryEventResponsePayload result =
                projectJuryEventService.getJuriesByProjectAndEvent(project.getId().toString(), event.getId().toString());

        assertTrue(result.getMentors().isEmpty());
        assertTrue(result.getObligedJuries().isEmpty());
        assertTrue(result.getWillingJuries().isEmpty());
    }

    @Test
    void shouldThrowWhenGettingJuriesForUnknownProject() {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(projectRepository.existsById(projectId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectJuryEventService.getJuriesByProjectAndEvent(projectId.toString(), eventId.toString())
        );

        assertEquals("Project with id " + projectId + " not found", exception.getMessage());

        verify(eventRepository, never()).existsById(any());
        verify(projectJuryEventRepository, never()).findByProjectIdAndEventId(any(), any());
    }

    @Test
    void shouldThrowWhenGettingJuriesForUnknownEvent() {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(eventRepository.existsById(eventId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectJuryEventService.getJuriesByProjectAndEvent(projectId.toString(), eventId.toString())
        );

        assertEquals("Event with id " + eventId + " not found", exception.getMessage());

        verify(projectJuryEventRepository, never()).findByProjectIdAndEventId(any(), any());
    }

    private AssignJuriesRequestPayload createPayload(
            UUID projectId,
            UUID eventId,
            List<UUID> mentors,
            List<UUID> obligedJuries,
            List<UUID> willingJuries,
            boolean applyToAllEvents
    ) {
        AssignJuriesRequestPayload payload = new AssignJuriesRequestPayload();

        payload.setProjectId(projectId.toString());
        payload.setEventId(eventId.toString());
        payload.setMentors(mentors.stream().map(UUID::toString).toList());
        payload.setObligedJuries(obligedJuries.stream().map(UUID::toString).toList());
        payload.setWillingJuries(willingJuries.stream().map(UUID::toString).toList());
        payload.setApplyToAllEvents(applyToAllEvents);

        return payload;
    }

    private List<ProjectJuryEvent> flatten(List<Iterable<ProjectJuryEvent>> values) {
        List<ProjectJuryEvent> result = new ArrayList<>();

        for (Iterable<ProjectJuryEvent> iterable : values) {
            iterable.forEach(result::add);
        }

        return result;
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

    private Event createEvent(String name, int year) {
        Event event = new Event();

        event.setId(UUID.randomUUID());
        event.setName(name);
        event.setEventType(EventType.IDEA);
        event.setYear(year);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);

        return event;
    }

    private User createUser(String login) {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setLogin(login);
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail(login + "@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_JURY);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }
}