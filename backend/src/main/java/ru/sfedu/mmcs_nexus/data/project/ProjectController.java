package ru.sfedu.mmcs_nexus.data.project;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Используем @RequestParam для получения имени из URL
    @GetMapping
    public List<Project> getProjects(@RequestParam("firstName") String firstName) {
        return projectService.findByFirstname(firstName);
    }
}

