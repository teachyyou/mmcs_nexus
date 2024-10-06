package ru.sfedu.mmcs_nexus.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectService;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class AdminProjectController {

    private final ProjectService projectService;

    private final ProjectEventService projectEventService;

    @Autowired
    public AdminProjectController(ProjectService projectService, ProjectEventService projectEventService) {
        this.projectService = projectService;
        this.projectEventService = projectEventService;
    }


    @GetMapping(value = "/api/v1/admin/projects", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getProjectsList(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "2024") String year,
            Authentication authentication) {

        List<Project> projects = projectService.getProjects(sort, order, year);

        Map<String, Object> response = new HashMap<>();
        response.put("content", projects);
        response.put("totalElements", projects.size());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/api/v1/admin/projects/{id}", produces = "application/json")
    public ResponseEntity<Project> getProjectById(@PathVariable("id") UUID id, Authentication authentication) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Project with id \{id} not found"));
        return ResponseEntity.ok(project);
    }

    @GetMapping(value = "/api/v1/admin/projects/{id}/events", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getProjectEventsById(@PathVariable("id") UUID id, Authentication authentication) {

        List<Event> events = projectEventService.findByProjectId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("content", events);
        response.put("totalElements", events.size());

        return ResponseEntity.ok().body(response);
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