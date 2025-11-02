package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEvent;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.LinkProjectsToEventRequestPayload;
import ru.sfedu.mmcs_nexus.repository.ProjectEventRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProjectEventService {

    private final ProjectEventRepository projectEventRepository;
    private final EventService eventService;
    private final ProjectService projectService;


    @Autowired
    public ProjectEventService(ProjectEventRepository projectEventRepository,
                               EventService eventService, ProjectService projectService) {
        this.projectEventRepository = projectEventRepository;
        this.eventService = eventService;
        this.projectService = projectService;
    }

    public Page<Project> findProjectsByEventId(String eventId, Integer day, PaginationPayload paginationPayload) {
        if (!eventService.existsById(eventId)) {
            throw new EntityNotFoundException(STR."Event with id \{eventId} not found");
        }

        Pageable pageable = paginationPayload.getPageable();

        return projectEventRepository.findProjectsByEventId(UUID.fromString(eventId), day, pageable);
    }

    public List<Project> findProjectsByEventId(String eventId, Integer day) {
        if (!eventService.existsById(eventId)) {
            throw new EntityNotFoundException(STR."Event with id \{eventId} not found");
        }

        return projectEventRepository.findProjectsByEventId(UUID.fromString(eventId), day);
    }

    public Page<Event> findEventsByProjectId(String projectId, Integer day, PaginationPayload paginationPayload) {
        if (!projectService.existsById(UUID.fromString(projectId))) {
            throw new EntityNotFoundException(STR."Project with id \{projectId} not found");
        }

        Pageable pageable = paginationPayload.getPageable();

        return projectEventRepository.findEventsByProjectId(UUID.fromString(projectId), day, pageable);
    }

    public List<Event> findEventsByProjectId(String projectId) {
        if (!projectService.existsById(UUID.fromString(projectId))) {
            throw new EntityNotFoundException(STR."Project with id \{projectId} not found");
        }

        return projectEventRepository.findEventsByProjectId(UUID.fromString(projectId), null);
    }

    public Optional<ProjectEvent> findById(ProjectEventKey id) {
        return projectEventRepository.findById(id);
    }

    @Transactional
    public void setProjectsForEvent(String eventId, LinkProjectsToEventRequestPayload payload) {
        Event event = eventService.find(eventId);

        List<Project> desiredProjects = payload.isLinkAllProjects()
                ? projectService.findAll(event.getYear())
                : projectService.findAll(payload.getProjectIds());

        //Тут важно не перезаписывать день защиты для тех связей, которые уже ранее существовали.
        List<ProjectEvent> existing = projectEventRepository.findByEventId(event.getId());
        Map<UUID, ProjectEvent> byProjectId = existing.stream()
                .collect(Collectors.toMap(pe -> pe.getProject().getId(), Function.identity()));

        List<ProjectEvent> toInsert = new ArrayList<>();
        Set<UUID> desiredIds = new HashSet<>();

        for (Project p : desiredProjects) {
            desiredIds.add(p.getId());
            ProjectEvent kept = byProjectId.get(p.getId());
            if (kept == null) {
                ProjectEventKey key = new ProjectEventKey(p.getId(), event.getId());
                toInsert.add(new ProjectEvent(key, event, p));
            }
        }

        List<ProjectEvent> toDelete = existing.stream()
                .filter(pe -> !desiredIds.contains(pe.getProject().getId()))
                .toList();

        if (!toDelete.isEmpty()) {
            projectEventRepository.deleteAllInBatch(toDelete);
        }

        if (!toInsert.isEmpty()) {
            projectEventRepository.saveAll(toInsert);
        }
    }

    public void setDaysForProjectAndEvent(String eventId, List<UUID> firstDayProjects, List<UUID> secondDayProjects) {
        Event event = eventService.find(eventId);

        if (firstDayProjects != null && secondDayProjects != null) {
            boolean hasOverlap = firstDayProjects.stream()
                    .anyMatch(secondDayProjects::contains);
            if (hasOverlap) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Project lists for day 1 and day 2 must not overlap"
                );
            }
        }

        if (firstDayProjects != null) for (UUID uuid : firstDayProjects) {
            if (!projectEventRepository.existsById(new ProjectEventKey(uuid, event.getId()))) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        STR."Project \{uuid} is not linked to event \{eventId}"
                );
            }
        }
        if (secondDayProjects != null) for (UUID uuid : secondDayProjects) {
            if (!projectEventRepository.existsById(new ProjectEventKey(uuid, event.getId()))) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        STR."Project \{uuid} is not linked to event \{eventId}"
                );
            }
        }

        List<ProjectEvent> links = projectEventRepository.findByEventId(event.getId());

        Set<UUID> firstSet  = firstDayProjects == null
                ? Collections.emptySet()
                : new HashSet<>(firstDayProjects);
        Set<UUID> secondSet = secondDayProjects == null
                ? Collections.emptySet()
                : new HashSet<>(secondDayProjects);

        for (ProjectEvent pe : links) {
            UUID projectId = pe.getProject().getId();
            if (firstSet.contains(projectId)) {
                pe.setDefDay(1);
            } else if (secondSet.contains(projectId)) {
                pe.setDefDay(2);
            } else {
                pe.setDefDay(null);
            }
        }

        projectEventRepository.saveAll(links);

    }
}
