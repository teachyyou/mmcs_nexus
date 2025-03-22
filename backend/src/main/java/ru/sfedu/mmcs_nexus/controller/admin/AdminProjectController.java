package ru.sfedu.mmcs_nexus.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.data.dto.ProjectEventJuryAssignmentRequest;
import ru.sfedu.mmcs_nexus.data.dto.UserDTO;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.event.EventService;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectService;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventService;
import ru.sfedu.mmcs_nexus.data.user.UserService;

import java.util.*;

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
            @RequestParam(defaultValue = "2025") String year,
            Authentication authentication) {

        List<Project> projects = projectService.getProjects(sort, order, year);

        Map<String, Object> response = new HashMap<>();
        response.put("content", projects);
        response.put("totalElements", projects.size());

        return ResponseEntity.ok().body(response);
    }

    //Получить инфу по проекту по id
    @GetMapping(value = "/api/v1/admin/projects/{id}", produces = "application/json")
    public ResponseEntity<Project> getProjectById(@PathVariable("id") UUID id, Authentication authentication) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Project with id \{id} not found"));
        return ResponseEntity.ok(project);
    }

    //Получить список всех событий для данного проекта по id
    @GetMapping(value = "/api/v1/admin/projects/{id}/events", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getProjectEventsById(@PathVariable("id") UUID id, Authentication authentication) {

        List<Event> events = projectEventService.findByProjectId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("content", events);
        response.put("totalElements", events.size());

        return ResponseEntity.ok().body(response);
    }

    //Получить список всех жюри у данных проекта и события по id
    @GetMapping(value = "api/v1/admin/projects/{project_id}/juries/{event_id}", produces = "application/json")
    public ResponseEntity<?> getProjectEventJuriesById(
            @PathVariable("project_id") UUID projectId,
            @PathVariable("event_id") UUID eventId,
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

    @PostMapping(value = "api/v1/admin/projects/{id}/juries", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveProjectEventJuries(@PathVariable("id") UUID id, Authentication authentication, @RequestBody ProjectEventJuryAssignmentRequest request) {

        Project project = projectService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Project with id \{id} not found"));

        if (request.isApplyToAllEvents()) {
            List<Event> events = projectEventService.findByProjectId(id);
            projectJuryEventService.clearProjectEventsJuries(project);
            for (Event event : events) {
                projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getMentors(), ProjectJuryEvent.RelationType.MENTOR);
                projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getObligedJuries(), ProjectJuryEvent.RelationType.OBLIGED);
                projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getWillingJuries(), ProjectJuryEvent.RelationType.WILLING);
            }

        } else {
            Event event = eventService.findById(request.getEventId()).orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{id} not found"));
            projectJuryEventService.clearProjectEventJuries(project, event);

            projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getMentors(), ProjectJuryEvent.RelationType.MENTOR);
            projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getObligedJuries(), ProjectJuryEvent.RelationType.OBLIGED);
            projectJuryEventService.saveJuriesToProjectEvent(project, event, request.getWillingJuries(), ProjectJuryEvent.RelationType.WILLING);
        }

        return null;
    }

    @PutMapping(value = "/api/v1/admin/projects/{id}", produces = "application/json")
    public ResponseEntity<Project> editProjectById(@PathVariable("id") UUID id, Authentication authentication, @RequestBody Project project) {

        Project existingProject = projectService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Project with id \{id} not found"));

        existingProject.editExistingProject(project);
        projectService.saveProject(existingProject);

        return ResponseEntity.ok(existingProject);
    }
    @PostMapping(value = "/api/v1/admin/projects", produces = "application/json")
    public ResponseEntity<?> createProject(Authentication authentication, @RequestBody Project project) {

        if (projectService.existsByName(project.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(STR."Project with name \{project.getName()} already exists!");
        }

        projectService.saveProject(project);

        return ResponseEntity.ok(project);
    }

    @DeleteMapping(value = "/api/v1/admin/projects/{id}")
    public ResponseEntity<Void> deleteProjectById(@PathVariable("id") UUID id) {
        if (!projectService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        projectService.deleteProjectById(id);

        return ResponseEntity.noContent().build();
    }
}
