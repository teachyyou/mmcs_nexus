package ru.sfedu.mmcs_nexus.controller.publiсapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectService;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class PublicProjectController {

    private final ProjectService projectService;
    private final ProjectEventService projectEventService;

    @Autowired
    public PublicProjectController(ProjectService projectService, ProjectEventService projectEventService) {
        this.projectService = projectService;
        this.projectEventService = projectEventService;
    }

    @GetMapping(value = "/api/v1/public/events/{id}/projects", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getProjectsByEvent(@PathVariable("id") UUID id, Authentication authentication) {

        List<Project> projects = projectEventService.findByEventId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("content", projects);
        response.put("totalElements", projects.size());

        return ResponseEntity.ok().body(response);
    }
}
