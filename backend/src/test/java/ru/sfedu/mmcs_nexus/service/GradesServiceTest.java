package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityExistsException;
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
import ru.sfedu.mmcs_nexus.exceptions.WrongGradePointsException;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;
import ru.sfedu.mmcs_nexus.model.entity.*;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.enums.controller.jury.GradeTableEnums;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.JuryRelationType;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.internal.GradeTableRow;
import ru.sfedu.mmcs_nexus.model.payload.jury.CreateGradeRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.jury.GetGradeTableResponsePayload;
import ru.sfedu.mmcs_nexus.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradesServiceTest {

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

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private GradeService gradeService;

    @Test
    void shouldCreateGradeForAssignedJury() {
        User jury = createUser("jury");
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        CreateGradeRequestPayload payload = createPayload(project.getId(), event.getId(), 15, 25, "Хорошая работа");

        ProjectJuryEvent projectJuryEvent = new ProjectJuryEvent();
        projectJuryEvent.setRelationType(JuryRelationType.OBLIGED);

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);
        when(projectJuryEventRepository.findByProjectIdAndEventIdAndJuryId(project.getId(), event.getId(), jury.getId()))
                .thenReturn(Optional.of(projectJuryEvent));
        when(gradeRepository.findById(any(GradeKey.class))).thenReturn(Optional.empty());

        GradeDTO result = gradeService.create("jury", payload);

        assertEquals(project.getId(), result.getProjectId());
        assertEquals(event.getId(), result.getEventId());
        assertEquals(jury.getId(), result.getJuryId());
        assertEquals(15, result.getPresPoints());
        assertEquals(25, result.getBuildPoints());
        assertEquals("Хорошая работа", result.getComment());

        ArgumentCaptor<Grade> gradeCaptor = ArgumentCaptor.forClass(Grade.class);

        verify(gradeRepository).save(gradeCaptor.capture());

        Grade savedGrade = gradeCaptor.getValue();

        assertEquals(project, savedGrade.getProject());
        assertEquals(event, savedGrade.getEvent());
        assertEquals(jury, savedGrade.getJury());
        assertEquals(15, savedGrade.getPresPoints());
        assertEquals(25, savedGrade.getBuildPoints());
        assertEquals("Хорошая работа", savedGrade.getComment());

        verify(projectJuryEventRepository, never()).save(any(ProjectJuryEvent.class));
    }

    @Test
    void shouldCreateWillingRelationWhenJuryWasNotAssignedBefore() {
        User jury = createUser("jury");
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        CreateGradeRequestPayload payload = createPayload(project.getId(), event.getId(), 10, 20, "ok");

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);
        when(projectJuryEventRepository.findByProjectIdAndEventIdAndJuryId(project.getId(), event.getId(), jury.getId()))
                .thenReturn(Optional.empty());
        when(gradeRepository.findById(any(GradeKey.class))).thenReturn(Optional.empty());

        gradeService.create("jury", payload);

        ArgumentCaptor<ProjectJuryEvent> projectJuryEventCaptor =
                ArgumentCaptor.forClass(ProjectJuryEvent.class);

        verify(projectJuryEventRepository).save(projectJuryEventCaptor.capture());

        ProjectJuryEvent savedRelation = projectJuryEventCaptor.getValue();

        assertEquals(project, savedRelation.getProject());
        assertEquals(event, savedRelation.getEvent());
        assertEquals(jury, savedRelation.getJury());
        assertEquals(JuryRelationType.WILLING, savedRelation.getRelationType());

        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void shouldThrowWhenMentorTriesToCreateGrade() {
        User jury = createUser("jury");
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        CreateGradeRequestPayload payload = createPayload(project.getId(), event.getId(), 10, 20, "ok");

        ProjectJuryEvent projectJuryEvent = new ProjectJuryEvent();
        projectJuryEvent.setRelationType(JuryRelationType.MENTOR);

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);
        when(projectJuryEventRepository.findByProjectIdAndEventIdAndJuryId(project.getId(), event.getId(), jury.getId()))
                .thenReturn(Optional.of(projectJuryEvent));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gradeService.create("jury", payload)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Mentor is not allowed to grade a project assigned to them", exception.getReason());

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void shouldThrowWhenGradeAlreadyExists() {
        User jury = createUser("jury");
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        CreateGradeRequestPayload payload = createPayload(project.getId(), event.getId(), 10, 20, "ok");

        ProjectJuryEvent projectJuryEvent = new ProjectJuryEvent();
        projectJuryEvent.setRelationType(JuryRelationType.WILLING);

        Grade existingGrade = createGrade(project, event, jury, 1, 2, "old");

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);
        when(projectJuryEventRepository.findByProjectIdAndEventIdAndJuryId(project.getId(), event.getId(), jury.getId()))
                .thenReturn(Optional.of(projectJuryEvent));
        when(gradeRepository.findById(any(GradeKey.class))).thenReturn(Optional.of(existingGrade));

        EntityExistsException exception = assertThrows(
                EntityExistsException.class,
                () -> gradeService.create("jury", payload)
        );

        assertEquals("Grade already exists", exception.getMessage());

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void shouldThrowWhenProjectAndEventAreNotLinked() {
        User jury = createUser("jury");
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        CreateGradeRequestPayload payload = createPayload(project.getId(), event.getId(), 10, 20, "ok");

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> gradeService.create("jury", payload)
        );

        assertEquals("Given project and Event are not linked", exception.getMessage());

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void shouldThrowWhenPresentationPointsAreTooHigh() {
        User jury = createUser("jury");
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        CreateGradeRequestPayload payload = createPayload(project.getId(), event.getId(), 21, 20, "too high");

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);

        WrongGradePointsException exception = assertThrows(
                WrongGradePointsException.class,
                () -> gradeService.create("jury", payload)
        );

        assertEquals("Maximum presentation score for Идея is 20", exception.getMessage());

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void shouldThrowWhenBuildPointsAreTooHigh() {
        User jury = createUser("jury");
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        CreateGradeRequestPayload payload = createPayload(project.getId(), event.getId(), 10, 31, "too high");

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))).thenReturn(true);

        WrongGradePointsException exception = assertThrows(
                WrongGradePointsException.class,
                () -> gradeService.create("jury", payload)
        );

        assertEquals("Maximum build score for Идея is 30", exception.getMessage());

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void shouldEditGrade() {
        User jury = createUser("jury");
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        Grade existingGrade = createGrade(project, event, jury, 10, 20, "old");
        CreateGradeRequestPayload payload = createPayload(project.getId(), event.getId(), 18, 28, "new");

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(gradeRepository.findById(any(GradeKey.class))).thenReturn(Optional.of(existingGrade));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        GradeDTO result = gradeService.edit("jury", payload);

        assertEquals(18, result.getPresPoints());
        assertEquals(28, result.getBuildPoints());
        assertEquals("new", result.getComment());

        assertEquals(18, existingGrade.getPresPoints());
        assertEquals(28, existingGrade.getBuildPoints());
        assertEquals("new", existingGrade.getComment());

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void shouldThrowWhenEditingUnknownGrade() {
        User jury = createUser("jury");
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        CreateGradeRequestPayload payload = createPayload(projectId, eventId, 10, 20, "new");

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(gradeRepository.findById(any(GradeKey.class))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> gradeService.edit("jury", payload)
        );

        assertEquals("Grade not found", exception.getMessage());

        verify(eventRepository, never()).findById(any());
    }

    @Test
    void shouldReturnGradeTableForAllProjects() {
        User jury = createUser("jury");
        User mentor = createUser("mentor");
        User otherJury = createUser("other");
        Event event = createEvent();
        Project betaProject = createProject("Beta");
        Project alphaProject = createProject("Alpha");

        Grade grade = createGrade(alphaProject, event, otherJury, 11, 22, "ok");

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectEventRepository.findProjectsByEventId(event.getId(), 1)).thenReturn(List.of(betaProject, alphaProject));
        when(projectJuryEventRepository.findJuriesByEventId(event.getId(), 1)).thenReturn(List.of(otherJury, mentor));
        when(projectJuryEventRepository.findMentorsByProjectIdAndEventId(event.getId(), alphaProject.getId())).thenReturn(List.of(mentor));
        when(projectJuryEventRepository.findMentorsByProjectIdAndEventId(event.getId(), betaProject.getId())).thenReturn(List.of());
        when(gradeRepository.findByEventAndProject(event.getId(), alphaProject.getId())).thenReturn(List.of(grade));
        when(gradeRepository.findByEventAndProject(event.getId(), betaProject.getId())).thenReturn(List.of());

        GetGradeTableResponsePayload result = gradeService.getTable(
                "jury",
                event.getId().toString(),
                GradeTableEnums.ShowFilter.ALL,
                1
        );

        assertEquals(event, result.getEvent());
        assertEquals(2, result.getProjectsCount());
        assertEquals(2, result.getJuriesCount());

        assertEquals("Alpha", result.getProjects().get(0).getName());
        assertEquals("Beta", result.getProjects().get(1).getName());

        assertEquals("Иванов", result.getJuries().get(0).getLastName());
        assertEquals(2, result.getRows().size());

        GradeTableRow alphaRow = result.getRows().get(0);

        assertEquals(alphaProject.getId(), alphaRow.getProjectId());
        assertEquals(mentor.getId(), alphaRow.getMentorId());
        assertEquals("Alpha", alphaRow.getProjectDisplayName());
        assertEquals(1, alphaRow.getTableRow().size());
        assertEquals(11, alphaRow.getTableRow().getFirst().getPresPoints());
    }

    @Test
    void shouldUseAssignedFilterForGradeTable() {
        User jury = createUser("jury");
        Event event = createEvent();
        Project project = createProject("Assigned");

        when(userRepository.findByLogin("jury")).thenReturn(Optional.of(jury));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(projectJuryEventRepository.findProjectByEventAssignedToJury(event.getId(), jury.getId(), 2))
                .thenReturn(List.of(project));
        when(projectJuryEventRepository.findJuriesForProjectsAssignedToJuryByEvent(event.getId(), jury.getId(), 2))
                .thenReturn(List.of(jury));
        when(projectJuryEventRepository.findMentorsByProjectIdAndEventId(event.getId(), project.getId()))
                .thenReturn(List.of());
        when(gradeRepository.findByEventAndProject(event.getId(), project.getId()))
                .thenReturn(List.of());

        GetGradeTableResponsePayload result = gradeService.getTable(
                "jury",
                event.getId().toString(),
                GradeTableEnums.ShowFilter.ASSIGNED,
                2
        );

        assertEquals(1, result.getProjectsCount());
        assertEquals("Assigned", result.getProjects().getFirst().getName());

        verify(projectJuryEventRepository).findProjectByEventAssignedToJury(event.getId(), jury.getId(), 2);
        verify(projectJuryEventRepository).findJuriesForProjectsAssignedToJuryByEvent(event.getId(), jury.getId(), 2);
    }

    @Test
    void shouldThrowWhenGettingTableForUnknownUser() {
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> gradeService.getTable("unknown", UUID.randomUUID().toString(), GradeTableEnums.ShowFilter.ALL, null)
        );

        assertEquals("User unknown is not found", exception.getMessage());
    }

    public CreateGradeRequestPayload createPayload(
            UUID projectId,
            UUID eventId,
            Integer presPoints,
            Integer buildPoints,
            String comment
    ) {
        CreateGradeRequestPayload payload = new CreateGradeRequestPayload();

        payload.setProjectId(projectId);
        payload.setEventId(eventId);
        payload.setPresPoints(presPoints);
        payload.setBuildPoints(buildPoints);
        payload.setComment(comment);

        return payload;
    }

    private Grade createGrade(Project project, Event event, User jury, Integer presPoints, Integer buildPoints, String comment) {
        Grade grade = new Grade();

        grade.setId(new GradeKey(project.getId(), event.getId(), jury.getId()));
        grade.setProject(project);
        grade.setEvent(event);
        grade.setJury(jury);
        grade.setPresPoints(presPoints);
        grade.setBuildPoints(buildPoints);
        grade.setComment(comment);

        return grade;
    }

    private Project createProject(String name) {
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
        project.setYear(2026);

        return project;
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