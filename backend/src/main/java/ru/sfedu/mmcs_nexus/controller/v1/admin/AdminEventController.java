package ru.sfedu.mmcs_nexus.controller.v1.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.model.dto.request.EventProjectDayRequestDTO;
import ru.sfedu.mmcs_nexus.model.dto.request.EventProjectsRequestDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.service.EventService;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;
import ru.sfedu.mmcs_nexus.service.ProjectService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import java.util.*;

@RestController
public class AdminEventController {

    private final EventService eventService;
    private final ProjectEventService projectEventService;

    private final ProjectService projectService;

    @Autowired
    public AdminEventController(EventService eventService, ProjectEventService projectEventService, ProjectService projectService) {
        this.eventService = eventService;
        this.projectEventService = projectEventService;
        this.projectService = projectService;
    }

    @GetMapping(value = "/api/v1/admin/events", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventsList(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Authentication authentication) {

        List<Event> events = eventService.getEvents(sort, order);

        Map<String, Object> response = new HashMap<>();
        response.put("content", events);
        response.put("totalElements", events.size());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/api/v1/admin/events/{id}", produces = "application/json")
    public ResponseEntity<Event> getEventById(@PathVariable("id") UUID id, Authentication authentication) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{id} not found"));
        return ResponseEntity.ok(event);
    }

    @GetMapping(value = "/api/v1/admin/events/{id}/projects", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventProjectsById(@PathVariable("id") UUID id, Authentication authentication) {

        List<Project> projects = projectEventService.findByEventId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("content", projects);
        response.put("totalElements", projects.size());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/api/v1/admin/events/{id}/projects/days", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventProjectsByIdForDays(@PathVariable("id") UUID id, Authentication authentication) {

        List<Project> firstDayProjects = projectEventService.findByEventIdForDay(id, 1).stream().sorted(Comparator.comparing(Project::getName)).toList();
        List<Project> secondDayProjects = projectEventService.findByEventIdForDay(id, 2).stream().sorted(Comparator.comparing(Project::getName)).toList();

        Map<String, List<Project>> content = new HashMap<>();
        content.put("firstDayProjects", firstDayProjects);
        content.put("secondDayProjects", secondDayProjects);

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalElements", firstDayProjects.size()+secondDayProjects.size());

        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/api/v1/admin/events/{id}/projects", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveEventProjects(@PathVariable("id") UUID eventId,
                                               @RequestBody EventProjectsRequestDTO request,
                                               Authentication authentication) {

        if (!eventService.existsById(eventId)) {
            return ResponseUtils.error(HttpStatus.NOT_FOUND, "Event not found", "eventId", eventId);
        }
        Event event = eventService.findById(eventId).get();

        List<Project> projects;
        if (request.isLinkAllProjects()) {
            // Fetch all projects for the same year as the event
            projects = projectService.findByYear(event.getYear());
            // Link all projects to the event
        } else {
            // Fetch specified projects by IDs
            projects = projectService.findByIds(request.getProjectIds());
            // Link specified projects to the event
        }
        projectEventService.setProjectsForEvent(event, projects);

        // Return a success response
        return ResponseEntity.ok().build();
    }

    //Распределение проектов по дням
    @PostMapping(value = "/api/v1/admin/events/{id}/days", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveEventProjectDefDays(@PathVariable("id") UUID eventId,
                                               @Valid @RequestBody EventProjectDayRequestDTO request,
                                               Authentication authentication) {

        projectEventService.setDaysForProjectAndEvent(eventId, request.getFirstDayProjects(), request.getSecondDayProjects());
        return ResponseUtils.success(HttpStatus.OK, "saved successfully");
    }



    @PutMapping(value = "/api/v1/admin/events/{id}", produces = "application/json")
    public ResponseEntity<Event> editEventById(@PathVariable("id") UUID id, Authentication authentication, @RequestBody Event event) {

        Event existingEvent = eventService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{id} not found"));

        existingEvent.editExistingEvent(event);
        eventService.saveEvent(existingEvent);

        return ResponseEntity.ok(existingEvent);
    }

    @PostMapping(value = "/api/v1/admin/events", produces = "application/json")
    public ResponseEntity<Event> createEvent(Authentication authentication, @Valid @RequestBody Event event) {
        
        eventService.saveEvent(event);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping(value = "/api/v1/admin/events/{id}")
    public ResponseEntity<Void> deleteEventById(@PathVariable("id") UUID id) {
        if (!eventService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        eventService.deleteEventById(id);

        return ResponseEntity.noContent().build();
    }
}
