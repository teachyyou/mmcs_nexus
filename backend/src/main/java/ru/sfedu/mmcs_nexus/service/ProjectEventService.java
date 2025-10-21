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

    public Page<Project> findProjectsByEvent(String eventId, Integer day, PaginationPayload paginationPayload) {
        if (!eventService.existsById(eventId)) {
            throw new EntityNotFoundException(STR."Event with id \{eventId} not found");
        }

        Pageable pageable = paginationPayload.getPageable();

        return projectEventRepository.findProjectsByEventId(UUID.fromString(eventId), day, pageable);
    }

    public List<Project> findProjectsByEvent(String eventId, Integer day) {
        if (!eventService.existsById(eventId)) {
            throw new EntityNotFoundException(STR."Event with id \{eventId} not found");
        }

        return projectEventRepository.findProjectsByEventId(UUID.fromString(eventId), day);
    }

    public List<Event> findByProjectId(UUID projectId) {
        if (!projectService.existsById(projectId)) {
            throw new EntityNotFoundException(STR."Project with id \{projectId} not found");
        }

        return projectEventRepository.findEventsByProjectId(projectId);
    }

    public Optional<ProjectEvent> findById(ProjectEventKey id) {
        return projectEventRepository.findById(id);
    }

    @Transactional
    public void deleteLinksByEventId(UUID eventId) {
        projectEventRepository.deleteByEventId(eventId);
    }

    @Transactional
    public void setProjectsForEvent(Event event, LinkProjectsToEventRequestPayload payload) {
        List<Project> projects;

        if (payload.isLinkAllProjects()) {
            projects = projectService.findByYear(event.getYear());
        } else {
            projects = projectService.findByIds(payload.getProjectIds());
        }

        deleteLinksByEventId(event.getId());

        for (Project project : projects) {
            ProjectEventKey key = new ProjectEventKey(project.getId(), event.getId());
            ProjectEvent projectEvent = new ProjectEvent(key, event, project);
            projectEventRepository.save(projectEvent);
        }
    }

    public void setDaysForProjectAndEvent(Event event, List<UUID> firstDayProjects, List<UUID> secondDayProjects) {

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

        UUID eventId = event.getId();

        if (firstDayProjects != null) for (UUID uuid : firstDayProjects) {
            if (!projectEventRepository.existsById(new ProjectEventKey(uuid, eventId))) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        STR."Project \{uuid} is not linked to event \{eventId}"
                );
            }
        }
        if (secondDayProjects != null) for (UUID uuid : secondDayProjects) {
            if (!projectEventRepository.existsById(new ProjectEventKey(uuid, eventId))) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        STR."Project \{uuid} is not linked to event \{eventId}"
                );
            }
        }

        List<ProjectEvent> links = projectEventRepository.findByEventId(eventId);

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
