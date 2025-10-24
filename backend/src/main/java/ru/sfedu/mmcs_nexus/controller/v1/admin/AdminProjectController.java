package ru.sfedu.mmcs_nexus.controller.v1.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.AssignJuriesRequestPayload;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateProjectRequestPayload;
import ru.sfedu.mmcs_nexus.service.EventService;
import ru.sfedu.mmcs_nexus.model.entity.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.service.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.service.ProjectService;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;
import ru.sfedu.mmcs_nexus.service.UserService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;
import org.hibernate.validator.constraints.UUID;

import java.util.*;

import static ru.sfedu.mmcs_nexus.util.ResponseUtils.buildPageResponse;

@RestController
public class AdminProjectController {

    private final ProjectService projectService;

    private final ProjectEventService projectEventService;

    private final ProjectJuryEventService projectJuryEventService;

    private final EventService eventService;

    @Autowired
    public AdminProjectController(ProjectService projectService, ProjectEventService projectEventService, ProjectJuryEventService projectJuryEventService, EventService eventService, UserService userService) {
        this.projectService = projectService;
        this.projectEventService = projectEventService;
        this.projectJuryEventService = projectJuryEventService;
        this.eventService = eventService;}


    //Получить список всех проектов с сортировкой за указанный год, по дефолту - 2024
    @GetMapping(value = "/api/v1/admin/projects", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getProjectsList(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            @RequestParam(required = false) Integer year
    ) {
        PaginationPayload paginationPayload = new PaginationPayload(limit, offset, sort, order, EntitySort.PROJECT_SORT);

        Page<Project> projects = projectService.findAll(year, paginationPayload);

        return buildPageResponse(projects);
    }

    //Получить инфу по проекту по id
    @GetMapping(value = "/api/v1/admin/projects/{id}", produces = "application/json")
    public ResponseEntity<Project> getProjectById(@PathVariable("id") @UUID String projectId) {
        Project project = projectService.find(projectId);

        return ResponseEntity.ok(project);
    }

    //Получить список всех событий для данного проекта по id
    @GetMapping(value = "/api/v1/admin/projects/{id}/events", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getProjectEventsById(
            @PathVariable("id") @UUID String projectId,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            @RequestParam(required = false) Integer day
    ) {
        PaginationPayload paginationPayload = new PaginationPayload(limit, offset);
        Page<Event> events = projectEventService.findEventsByProjectId(projectId, day, paginationPayload);

        return buildPageResponse(events);
    }

    @PostMapping(value = "/api/v1/admin/projects", produces = "application/json")
    public ResponseEntity<?> createProject(@Valid @RequestBody CreateProjectRequestPayload payload) {
        projectService.create(payload);

        return ResponseUtils.success(HttpStatus.OK, "saved successfully");
    }

    @PutMapping(value = "/api/v1/admin/projects/{id}", produces = "application/json")
    public ResponseEntity<Project> editProjectById(@PathVariable("id") @UUID String projectId, @Valid @RequestBody CreateProjectRequestPayload payload) {
        Project project = projectService.edit(projectId, payload);

        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/api/v1/admin/projects/{id}")
    public ResponseEntity<?> deleteProjectById(@PathVariable("id") @UUID String projectId) {
        projectService.deleteById(projectId);

        return ResponseEntity.noContent().build();
    }

    //todo rewrite this
    @GetMapping(value = "api/v1/admin/projects/{project_id}/juries/{event_id}", produces = "application/json")
    public ResponseEntity<?> getProjectEventJuriesById(
            @PathVariable("project_id") @UUID String projectId,
            @PathVariable("event_id") @UUID String eventId,
            Authentication authentication) {

        try {
            Map<String, List<UserDTO>> response = projectJuryEventService.getJuriesByProjectAndEvent(projectId, eventId);
            return ResponseEntity.ok().body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An unexpected error occurred"));
        }
    }

    //todo rewrite this
    @PostMapping(value = "api/v1/admin/projects/{id}/juries", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveProjectEventJuries(@PathVariable("id") @UUID String id, @RequestBody AssignJuriesRequestPayload request) {

        Project project = projectService.find(id);
        if (request.isApplyToAllEvents()) {
            List<Event> events = projectEventService.findEventsByProjectId(id);
            projectJuryEventService.clearProjectEventsJuries(project);
            for (Event event : events) {
                projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getMentors(), ProjectJuryEvent.RelationType.MENTOR);
                projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getObligedJuries(), ProjectJuryEvent.RelationType.OBLIGED);
                projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getWillingJuries(), ProjectJuryEvent.RelationType.WILLING);
            }

        } else {
            Event event = eventService.find(request.getEventId().toString());
            projectJuryEventService.clearProjectEventJuries(project, event);

            projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getMentors(), ProjectJuryEvent.RelationType.MENTOR);
            projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getObligedJuries(), ProjectJuryEvent.RelationType.OBLIGED);
            projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getWillingJuries(), ProjectJuryEvent.RelationType.WILLING);
        }

        return null;
    }
}
