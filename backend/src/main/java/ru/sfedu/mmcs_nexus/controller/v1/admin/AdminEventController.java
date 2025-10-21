package ru.sfedu.mmcs_nexus.controller.v1.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateEventRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.LinkProjectsToEventRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.SetDefenceDayRequestPayload;
import ru.sfedu.mmcs_nexus.service.EventService;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AdminEventController {
    private final EventService eventService;
    private final ProjectEventService projectEventService;

    @Autowired
    public AdminEventController(EventService eventService, ProjectEventService projectEventService) {
        this.eventService = eventService;
        this.projectEventService = projectEventService;
    }

    @GetMapping(value = "/api/v1/admin/events", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventsList
    (
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            @RequestParam(required = false) Integer year
    ) {

        PaginationPayload paginationPayload = new PaginationPayload(limit, offset, sort, order, EntitySort.EVENT_SORT);

        Page<Event> events = eventService.getEvents(year, paginationPayload);

        return ResponseEntity.ok().body(
                ResponseUtils.buildResponse(events.getContent(), events.getTotalElements())
        );
    }

    @GetMapping(value = "/api/v1/admin/events/{id}", produces = "application/json")
    public ResponseEntity<Event> getEventById(@PathVariable("id") @UUID String id) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{id} not found"));
        return ResponseEntity.ok(event);
    }

    @PostMapping(value = "/api/v1/admin/events", produces = "application/json")
    public ResponseEntity<?> createEvent(@Valid @RequestBody CreateEventRequestPayload payload) {
        eventService.createEvent(payload);

        return ResponseUtils.success(HttpStatus.OK, "saved successfully");
    }

    @PutMapping(value = "/api/v1/admin/events/{id}", produces = "application/json")
    public ResponseEntity<Event> editEventById(@PathVariable("id") @UUID String id, @Valid @RequestBody CreateEventRequestPayload payload) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{id} not found"));

        event = eventService.editEvent(event, payload);

        return ResponseEntity.ok(event);
    }

    @DeleteMapping(value = "/api/v1/admin/events/{id}")
    public ResponseEntity<Void> deleteEventById(@PathVariable("id") @UUID String id) {
        if (!eventService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        eventService.deleteEventById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/api/v1/admin/events/{id}/projects", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventProjectsById(
            @PathVariable("id") @UUID String eventId,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            @RequestParam(required = false) Integer day
    ) {
        PaginationPayload paginationPayload = new PaginationPayload(limit, offset);

        Page<Project> projects = projectEventService.findProjectsByEvent(eventId, day, paginationPayload);

        return ResponseEntity.ok().body(
                ResponseUtils.buildResponse(projects.getContent(), projects.getTotalElements())
        );
    }

    @GetMapping(value = "/api/v1/admin/events/{id}/projects/days", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventProjectsByIdForDays(@PathVariable("id") @UUID String eventId) {

        List<Project> firstDayProjects = projectEventService.findProjectsByEvent(eventId, 1);
        List<Project> secondDayProjects = projectEventService.findProjectsByEvent(eventId, 2);


        Map<String, List<Project>> content = new HashMap<>();
        content.put("firstDayProjects", firstDayProjects);
        content.put("secondDayProjects", secondDayProjects);

        return ResponseEntity.ok().body(
                ResponseUtils.buildResponse(content, firstDayProjects.size()+secondDayProjects.size())
        );
    }

    @PostMapping(value = "/api/v1/admin/events/{id}/projects", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveEventProjects(
            @PathVariable("id") @UUID String eventId,
            @RequestBody LinkProjectsToEventRequestPayload request
    ) {

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{eventId} not found"));

        projectEventService.setProjectsForEvent(event, request);

        return ResponseEntity.ok().build();
    }

    //Распределение проектов по дням
    @PostMapping(value = "/api/v1/admin/events/{id}/days", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveEventProjectDefDays(
            @PathVariable("id") @UUID String eventId,
            @Valid @RequestBody SetDefenceDayRequestPayload request
    ) {

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{eventId} not found"));

        projectEventService.setDaysForProjectAndEvent(event, request.getFirstDayProjects(), request.getSecondDayProjects());
        return ResponseUtils.success(HttpStatus.OK, "saved successfully");
    }
}
