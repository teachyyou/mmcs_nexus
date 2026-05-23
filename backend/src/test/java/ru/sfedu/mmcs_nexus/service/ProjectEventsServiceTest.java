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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEvent;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.LinkProjectsToEventRequestPayload;
import ru.sfedu.mmcs_nexus.repository.EventRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectEventRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectEventsServiceTest {

    @Mock
    private ProjectEventRepository projectEventRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectEventService projectEventService;

    @Test
    void shouldFindProjectsByEventIdWithPagination() {
        Event event = createEvent(2026);
        Project project = createProject("MMCS Nexus", 2026);
        PaginationPayload paginationPayload = new PaginationPayload(10, 0);

        when(eventRepository.existsById(event.getId())).thenReturn(true);
        when(projectEventRepository.findProjectsByEventId(eq(event.getId()), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(project)));

        Page<Project> result = projectEventService.findProjectsByEventId(
                event.getId().toString(),
                null,
                paginationPayload
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(project.getId(), result.getContent().getFirst().getId());

        verify(eventRepository).existsById(event.getId());
        verify(projectEventRepository).findProjectsByEventId(eq(event.getId()), eq(null), any());
    }

    @Test
    void shouldFindProjectsByEventIdAndDayAsList() {
        Event event = createEvent(2026);
        Project project = createProject("MMCS Nexus", 2026);

        when(eventRepository.existsById(event.getId())).thenReturn(true);
        when(projectEventRepository.findProjectsByEventId(event.getId(), 1))
                .thenReturn(List.of(project));

        List<Project> result = projectEventService.findProjectsByEventId(event.getId().toString(), 1);

        assertEquals(1, result.size());
        assertEquals(project.getId(), result.getFirst().getId());

        verify(eventRepository).existsById(event.getId());
        verify(projectEventRepository).findProjectsByEventId(event.getId(), 1);
    }

    @Test
    void shouldThrowWhenFindingProjectsForUnknownEvent() {
        UUID eventId = UUID.randomUUID();
        PaginationPayload paginationPayload = new PaginationPayload(10, 0);

        when(eventRepository.existsById(eventId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectEventService.findProjectsByEventId(eventId.toString(), null, paginationPayload)
        );

        assertEquals("Event with id " + eventId + " not found", exception.getMessage());

        verify(projectEventRepository, never()).findProjectsByEventId(any(), any(), any());
    }

    @Test
    void shouldFindEventsByProjectIdWithPagination() {
        Project project = createProject("MMCS Nexus", 2026);
        Event event = createEvent(2026);
        PaginationPayload paginationPayload = new PaginationPayload(10, 0);

        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(projectEventRepository.findEventsByProjectId(eq(project.getId()), eq(2), any()))
                .thenReturn(new PageImpl<>(List.of(event)));

        Page<Event> result = projectEventService.findEventsByProjectId(
                project.getId().toString(),
                2,
                paginationPayload
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(event.getId(), result.getContent().getFirst().getId());

        verify(projectRepository).existsById(project.getId());
        verify(projectEventRepository).findEventsByProjectId(eq(project.getId()), eq(2), any());
    }

    @Test
    void shouldFindEventsByProjectIdAsList() {
        Project project = createProject("MMCS Nexus", 2026);
        Event event = createEvent(2026);

        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(projectEventRepository.findEventsByProjectId(project.getId(), null))
                .thenReturn(List.of(event));

        List<Event> result = projectEventService.findEventsByProjectId(project.getId().toString());

        assertEquals(1, result.size());
        assertEquals(event.getId(), result.getFirst().getId());

        verify(projectRepository).existsById(project.getId());
        verify(projectEventRepository).findEventsByProjectId(project.getId(), null);
    }

    @Test
    void shouldThrowWhenFindingEventsForUnknownProject() {
        UUID projectId = UUID.randomUUID();
        PaginationPayload paginationPayload = new PaginationPayload(10, 0);

        when(projectRepository.existsById(projectId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectEventService.findEventsByProjectId(projectId.toString(), null, paginationPayload)
        );

        assertEquals("Project with id " + projectId + " not found", exception.getMessage());

        verify(projectEventRepository, never()).findEventsByProjectId(any(), any(), any());
    }

    @Test
    void shouldSetProjectsForEventAndInsertNewLinks() {
        Event event = createEvent(2026);
        Project firstProject = createProject("First", 2026);
        Project secondProject = createProject("Second", 2026);
        LinkProjectsToEventRequestPayload payload = createLinkPayload(
                List.of(firstProject.getId(), secondProject.getId()),
                false
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectRepository.findAllById(payload.getProjectIds())).thenReturn(List.of(firstProject, secondProject));
        when(projectEventRepository.findByEventId(event.getId())).thenReturn(List.of());

        projectEventService.setProjectsForEvent(event.getId().toString(), payload);

        ArgumentCaptor<List<ProjectEvent>> saveCaptor = ArgumentCaptor.forClass(List.class);

        verify(projectEventRepository).saveAll(saveCaptor.capture());
        verify(projectEventRepository, never()).deleteAllInBatch(anyList());

        List<ProjectEvent> savedLinks = saveCaptor.getValue();

        assertEquals(2, savedLinks.size());
        assertTrue(savedLinks.stream().anyMatch(link -> link.getProject().equals(firstProject)));
        assertTrue(savedLinks.stream().anyMatch(link -> link.getProject().equals(secondProject)));
        assertTrue(savedLinks.stream().allMatch(link -> link.getEvent().equals(event)));
    }

    @Test
    void shouldSetAllProjectsForEventByEventYear() {
        Event event = createEvent(2026);
        Project firstProject = createProject("First", 2026);
        Project secondProject = createProject("Second", 2026);
        LinkProjectsToEventRequestPayload payload = createLinkPayload(List.of(), true);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectRepository.findAllByYear(event.getYear())).thenReturn(List.of(firstProject, secondProject));
        when(projectEventRepository.findByEventId(event.getId())).thenReturn(List.of());

        projectEventService.setProjectsForEvent(event.getId().toString(), payload);

        ArgumentCaptor<List<ProjectEvent>> saveCaptor = ArgumentCaptor.forClass(List.class);

        verify(projectRepository).findAllByYear(2026);
        verify(projectRepository, never()).findAllById(any());
        verify(projectEventRepository).saveAll(saveCaptor.capture());

        assertEquals(2, saveCaptor.getValue().size());
    }

    @Test
    void shouldKeepExistingLinkAndNotOverwriteDefenceDay() {
        Event event = createEvent(2026);
        Project project = createProject("Existing", 2026);
        ProjectEvent existingLink = new ProjectEvent(
                new ProjectEventKey(project.getId(), event.getId()),
                event,
                project,
                2
        );
        LinkProjectsToEventRequestPayload payload = createLinkPayload(List.of(project.getId()), false);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectRepository.findAllById(payload.getProjectIds())).thenReturn(List.of(project));
        when(projectEventRepository.findByEventId(event.getId())).thenReturn(List.of(existingLink));

        projectEventService.setProjectsForEvent(event.getId().toString(), payload);

        assertEquals(2, existingLink.getDefDay());

        verify(projectEventRepository, never()).saveAll(anyList());
        verify(projectEventRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    void shouldDeleteLinksThatAreNotDesiredAnymore() {
        Event event = createEvent(2026);
        Project keptProject = createProject("Kept", 2026);
        Project deletedProject = createProject("Deleted", 2026);

        ProjectEvent keptLink = new ProjectEvent(
                new ProjectEventKey(keptProject.getId(), event.getId()),
                event,
                keptProject,
                1
        );

        ProjectEvent deletedLink = new ProjectEvent(
                new ProjectEventKey(deletedProject.getId(), event.getId()),
                event,
                deletedProject,
                2
        );

        LinkProjectsToEventRequestPayload payload = createLinkPayload(List.of(keptProject.getId()), false);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectRepository.findAllById(payload.getProjectIds())).thenReturn(List.of(keptProject));
        when(projectEventRepository.findByEventId(event.getId())).thenReturn(List.of(keptLink, deletedLink));

        projectEventService.setProjectsForEvent(event.getId().toString(), payload);

        ArgumentCaptor<List<ProjectEvent>> deleteCaptor = ArgumentCaptor.forClass(List.class);

        verify(projectEventRepository).deleteAllInBatch(deleteCaptor.capture());
        verify(projectEventRepository, never()).saveAll(anyList());

        assertEquals(1, deleteCaptor.getValue().size());
        assertEquals(deletedProject.getId(), deleteCaptor.getValue().getFirst().getProject().getId());
    }

    @Test
    void shouldThrowWhenSettingProjectsForUnknownEvent() {
        UUID eventId = UUID.randomUUID();
        LinkProjectsToEventRequestPayload payload = createLinkPayload(List.of(UUID.randomUUID()), false);

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectEventService.setProjectsForEvent(eventId.toString(), payload)
        );

        assertEquals("Event with id " + eventId + " not found", exception.getMessage());

        verify(projectEventRepository, never()).saveAll(anyList());
        verify(projectEventRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    void shouldSetDefenceDaysForLinkedProjects() {
        Event event = createEvent(2026);
        Project firstProject = createProject("First", 2026);
        Project secondProject = createProject("Second", 2026);
        Project noDayProject = createProject("No day", 2026);

        ProjectEvent firstLink = new ProjectEvent(new ProjectEventKey(firstProject.getId(), event.getId()), event, firstProject);
        ProjectEvent secondLink = new ProjectEvent(new ProjectEventKey(secondProject.getId(), event.getId()), event, secondProject);
        ProjectEvent noDayLink = new ProjectEvent(new ProjectEventKey(noDayProject.getId(), event.getId()), event, noDayProject, 2);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(firstProject.getId(), event.getId()))).thenReturn(true);
        when(projectEventRepository.existsById(new ProjectEventKey(secondProject.getId(), event.getId()))).thenReturn(true);
        when(projectEventRepository.findByEventId(event.getId())).thenReturn(List.of(firstLink, secondLink, noDayLink));

        projectEventService.setDaysForProjectAndEvent(
                event.getId().toString(),
                List.of(firstProject.getId()),
                List.of(secondProject.getId())
        );

        assertEquals(1, firstLink.getDefDay());
        assertEquals(2, secondLink.getDefDay());
        assertNull(noDayLink.getDefDay());

        verify(projectEventRepository).saveAll(List.of(firstLink, secondLink, noDayLink));
    }

    @Test
    void shouldThrowWhenDefenceDayListsOverlap() {
        Event event = createEvent(2026);
        UUID projectId = UUID.randomUUID();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectEventService.setDaysForProjectAndEvent(
                        event.getId().toString(),
                        List.of(projectId),
                        List.of(projectId)
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Project lists for day 1 and day 2 must not overlap", exception.getReason());

        verify(projectEventRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowWhenFirstDayProjectIsNotLinkedToEvent() {
        Event event = createEvent(2026);
        UUID projectId = UUID.randomUUID();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(projectId, event.getId()))).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectEventService.setDaysForProjectAndEvent(
                        event.getId().toString(),
                        List.of(projectId),
                        List.of()
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Project " + projectId + " is not linked to event " + event.getId(), exception.getReason());

        verify(projectEventRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowWhenSettingDaysForUnknownEvent() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectEventService.setDaysForProjectAndEvent(eventId.toString(), List.of(), List.of())
        );

        assertEquals("Event with id " + eventId + " not found", exception.getMessage());

        verify(projectEventRepository, never()).saveAll(anyList());
    }

    private LinkProjectsToEventRequestPayload createLinkPayload(List<UUID> projectIds, boolean linkAllProjects) {
        LinkProjectsToEventRequestPayload payload = new LinkProjectsToEventRequestPayload();

        payload.setProjectIds(projectIds);
        payload.setLinkAllProjects(linkAllProjects);

        return payload;
    }

    private Event createEvent(int year) {
        Event event = new Event();

        event.setId(UUID.randomUUID());
        event.setName("Идея");
        event.setEventType(EventType.IDEA);
        event.setYear(year);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);

        return event;
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
}