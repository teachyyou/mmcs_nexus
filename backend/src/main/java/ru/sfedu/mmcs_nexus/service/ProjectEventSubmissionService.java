package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.model.dto.entity.JuryProjectSubmissionDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.JurySubmissionEventDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectEventSubmissionDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectEventSubmissionItemDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEventSubmission;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.SubmissionAvailabilityStatus;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.jury.GetJurySubmissionEventsResponsePayload;
import ru.sfedu.mmcs_nexus.model.payload.user.GetProjectEventSubmissionsResponsePayload;
import ru.sfedu.mmcs_nexus.model.payload.user.SaveProjectEventSubmissionRequestPayload;
import ru.sfedu.mmcs_nexus.repository.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProjectEventSubmissionService {

    private final ProjectEventSubmissionRepository projectEventSubmissionRepository;
    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;
    private final ProjectEventRepository projectEventRepository;
    private final UserRepository userRepository;

    public ProjectEventSubmissionService(
            ProjectEventSubmissionRepository projectEventSubmissionRepository,
            ProjectRepository projectRepository,
            EventRepository eventRepository,
            ProjectEventRepository projectEventRepository,
            UserRepository userRepository
    ) {
        this.projectEventSubmissionRepository = projectEventSubmissionRepository;
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
        this.projectEventRepository = projectEventRepository;
        this.userRepository = userRepository;
    }

    public GetProjectEventSubmissionsResponsePayload getSubmissions(String projectId, String githubLogin) {
        User user = getVerifiedUser(githubLogin);
        Project project = getProject(projectId);

        ensureUserIsProjectCaptain(project, user);

        List<Event> events = projectEventRepository.findEventsByProjectId(project.getId(), null);

        List<ProjectEventSubmission> submissions = projectEventSubmissionRepository.findAllByProjectId(project.getId());

        List<ProjectEventSubmissionItemDTO> items = events.stream()
                .sorted(Comparator.comparing(Event::getSubmissionStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Event::getSubmissionDeadlineDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Event::getName))
                .map(event -> {
                    ProjectEventSubmission submission = submissions.stream()
                            .filter(item -> item.getEvent().getId().equals(event.getId()))
                            .findFirst()
                            .orElse(null);

                    SubmissionAvailabilityStatus status = getStatus(event);
                    boolean editable = status == SubmissionAvailabilityStatus.OPEN;

                    return new ProjectEventSubmissionItemDTO(
                            event,
                            status,
                            editable,
                            getMessage(status),
                            submission
                    );
                })
                .toList();

        UUID defaultEventId = chooseDefaultEventId(items);

        return new GetProjectEventSubmissionsResponsePayload(defaultEventId, items);
    }

    public GetJurySubmissionEventsResponsePayload getJurySubmissionEvents(Integer year) {
        List<JurySubmissionEventDTO> events = eventRepository.findAllByYearOrderByNameAsc(year).stream()
                .sorted(Comparator.comparing(Event::getSubmissionStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Event::getSubmissionDeadlineDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Event::getName))
                .map(event -> new JurySubmissionEventDTO(event, getStatus(event)))
                .toList();

        UUID defaultEventId = chooseDefaultJuryEventId(events);

        return new GetJurySubmissionEventsResponsePayload(defaultEventId, events);
    }

    public Page<JuryProjectSubmissionDTO> getJurySubmissionsByEvent(String eventId, PaginationPayload paginationPayload) {
        Event event = getEvent(eventId);
        Pageable pageable = paginationPayload.getPageable();

        Page<Project> projects = projectEventRepository.findProjectsByEventId(event.getId(), pageable);
        List<ProjectEventSubmission> submissions = projectEventSubmissionRepository.findAllByEventId(event.getId());

        return projects.map(project -> {
            ProjectEventSubmission submission = submissions.stream()
                    .filter(item -> item.getProject().getId().equals(project.getId()))
                    .findFirst()
                    .orElse(null);

            return new JuryProjectSubmissionDTO(project, submission);
        });
    }

    private UUID chooseDefaultJuryEventId(List<JurySubmissionEventDTO> events) {
        return events.stream()
                .filter(event -> event.getSubmissionStatus() == SubmissionAvailabilityStatus.OPEN)
                .map(JurySubmissionEventDTO::getId)
                .findFirst()
                .orElseGet(() -> events.stream()
                        .filter(event -> event.getSubmissionStatus() == SubmissionAvailabilityStatus.FUTURE)
                        .map(JurySubmissionEventDTO::getId)
                        .findFirst()
                        .orElseGet(() -> events.stream()
                                .filter(event -> event.getSubmissionStatus() == SubmissionAvailabilityStatus.CLOSED)
                                .reduce((first, second) -> second)
                                .map(JurySubmissionEventDTO::getId)
                                .orElse(null)));
    }

    @Transactional
    public ProjectEventSubmissionDTO saveSubmission(
            String projectId,
            String eventId,
            String githubLogin,
            SaveProjectEventSubmissionRequestPayload payload
    ) {
        User user = getVerifiedUser(githubLogin);
        Project project = getProject(projectId);
        Event event = getEvent(eventId);

        ensureUserIsProjectCaptain(project, user);
        ensureProjectEventExists(project, event);
        ensureEventIsOpen(event);

        ProjectEventSubmission submission = projectEventSubmissionRepository
                .findByProjectIdAndEventId(project.getId(), event.getId())
                .orElseGet(() -> new ProjectEventSubmission(project, event, user));

        submission.setPresentationUrl(clean(payload.getPresentationUrl()));
        submission.setRepositoryUrl(clean(payload.getRepositoryUrl()));
        submission.setReleaseUrl(clean(payload.getReleaseUrl()));
        submission.setComment(clean(payload.getComment()));
        submission.setSubmittedBy(user);

        ProjectEventSubmission savedSubmission = projectEventSubmissionRepository.save(submission);

        return new ProjectEventSubmissionDTO(savedSubmission);
    }

    private User getVerifiedUser(String githubLogin) {
        User user = userRepository.findByLogin(githubLogin)
                .orElseThrow(() -> new UsernameNotFoundException("User " + githubLogin + " is not found"));

        if (user.getStatus() != UserEnums.UserStatus.VERIFIED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not verified");
        }

        return user;
    }

    private Project getProject(String projectId) {
        return projectRepository.findById(UUID.fromString(projectId))
                .orElseThrow(() -> new EntityNotFoundException("Project with id " + projectId + " not found"));
    }

    private Event getEvent(String eventId) {
        return eventRepository.findById(UUID.fromString(eventId))
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
    }

    private void ensureUserIsProjectCaptain(Project project, User user) {
        if (project.getCaptain() == null || !project.getCaptain().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project captain can manage submissions");
        }
    }

    private void ensureProjectEventExists(Project project, Event event) {
        ProjectEventKey key = new ProjectEventKey(project.getId(), event.getId());

        if (!projectEventRepository.existsById(key)) {
            throw new EntityNotFoundException("Given project and Event are not linked");
        }
    }

    private void ensureEventIsOpen(Event event) {
        SubmissionAvailabilityStatus status = getStatus(event);

        if (status == SubmissionAvailabilityStatus.NOT_CONFIGURED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Submission window is not configured");
        }

        if (status == SubmissionAvailabilityStatus.FUTURE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Submission is not available yet");
        }

        if (status == SubmissionAvailabilityStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Submission deadline has passed");
        }
    }

    private SubmissionAvailabilityStatus getStatus(Event event) {
        if (event.getSubmissionStartDate() == null || event.getSubmissionDeadlineDate() == null) {
            return SubmissionAvailabilityStatus.NOT_CONFIGURED;
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(event.getSubmissionStartDate())) {
            return SubmissionAvailabilityStatus.FUTURE;
        }

        if (today.isAfter(event.getSubmissionDeadlineDate())) {
            return SubmissionAvailabilityStatus.CLOSED;
        }

        return SubmissionAvailabilityStatus.OPEN;
    }

    private String getMessage(SubmissionAvailabilityStatus status) {
        return switch (status) {
            case NOT_CONFIGURED -> "Период сдачи материалов не настроен";
            case FUTURE -> "Загрузка будет доступна позже";
            case OPEN -> null;
            case CLOSED -> "Этап завершён, редактирование недоступно";
        };
    }

    private UUID chooseDefaultEventId(List<ProjectEventSubmissionItemDTO> items) {
        return items.stream()
                .filter(item -> item.getSubmissionStatus() == SubmissionAvailabilityStatus.OPEN)
                .map(ProjectEventSubmissionItemDTO::getEventId)
                .findFirst()
                .orElseGet(() -> items.stream()
                        .filter(item -> item.getSubmissionStatus() == SubmissionAvailabilityStatus.FUTURE)
                        .map(ProjectEventSubmissionItemDTO::getEventId)
                        .findFirst()
                        .orElseGet(() -> items.stream()
                                .filter(item -> item.getSubmissionStatus() == SubmissionAvailabilityStatus.CLOSED)
                                .reduce((first, second) -> second)
                                .map(ProjectEventSubmissionItemDTO::getEventId)
                                .orElse(null)));
    }

    private String clean(String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        String trimmedValue = value.trim();

        return trimmedValue.isBlank() ? null : trimmedValue;
    }
}