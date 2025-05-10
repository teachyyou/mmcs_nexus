package ru.sfedu.mmcs_nexus.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEvent;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.repository.ProjectEventRepository;

import java.util.*;

@Service
public class ProjectEventService {

    private final ProjectEventRepository projectEventRepository;

    private final EventService eventService;


    @Autowired
    public ProjectEventService(ProjectEventRepository projectEventRepository,
                               EventService eventService) {
        this.projectEventRepository = projectEventRepository;
        this.eventService = eventService;
    }

    public List<Project> findByEventId(UUID eventId) {
        return projectEventRepository.findProjectsByEventId(eventId, null);
    }

    public List<Project> findByEventIdForDay(UUID eventId, int day) {
        return projectEventRepository.findByEventIdForDay(eventId, day);
    }

    public List<Event> findByProjectId(UUID projectId) {
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
    public void setProjectsForEvent(Event event, List<Project> projects) {

        deleteLinksByEventId(event.getId());

        for (Project project : projects ) {
            ProjectEventKey key = new ProjectEventKey(project.getId(), event.getId());
            ProjectEvent projectEvent = new ProjectEvent(key, event, project);
            projectEventRepository.save(projectEvent);
        }
    }

    public void setDaysForProjectAndEvent(UUID eventId, List<UUID> firstDayProjects, List<UUID> secondDayProjects) {

        Event event = eventService.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

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
