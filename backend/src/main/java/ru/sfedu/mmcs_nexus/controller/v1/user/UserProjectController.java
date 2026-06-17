package ru.sfedu.mmcs_nexus.controller.v1.user;

import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectUserDTO;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.service.ProjectService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import java.time.Year;
import java.util.Map;

@RestController
public class UserProjectController {

    private final ProjectService projectService;

    @Autowired
    public UserProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping(value = "/api/v1/user/projects", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getProjects(
            @AuthenticationPrincipal OAuth2User user,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            @RequestParam(required = false) Integer year
    ) {
        String githubLogin = user.getAttribute("login");

        PaginationPayload paginationPayload = new PaginationPayload(limit, offset, sort, order, EntitySort.PROJECT_SORT);

        Page<ProjectUserDTO> projects = projectService.findAllForUser(
                year != null ? year : Year.now().getValue(),
                paginationPayload,
                githubLogin
        );

        return ResponseUtils.buildPageResponse(projects);
    }

    @GetMapping(value = "/api/v1/user/projects/{id}", produces = "application/json")
    public ResponseEntity<ProjectUserDTO> getProject(
            @AuthenticationPrincipal OAuth2User user,
            @PathVariable("id") @UUID String projectId
    ) {
        String githubLogin = user.getAttribute("login");

        return ResponseEntity.ok(projectService.findForUser(projectId, githubLogin));
    }

    @PostMapping(value = "/api/v1/user/projects/{id}/claim-captain", produces = "application/json")
    public ResponseEntity<?> claimCaptain(
            @AuthenticationPrincipal OAuth2User user,
            @PathVariable("id") @UUID String projectId
    ) {
        String githubLogin = user.getAttribute("login");

        ProjectUserDTO project = projectService.assignCaptain(projectId, githubLogin);

        return ResponseUtils.success(
                HttpStatus.OK,
                "captain assigned successfully",
                "project",
                project
        );
    }

    @GetMapping(value = "/api/v1/user/projects/my", produces = "application/json")
    public ResponseEntity<?> getMyProject(@AuthenticationPrincipal OAuth2User user) {
        String githubLogin = user.getAttribute("login");

        return projectService.findCaptainProject(githubLogin)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseUtils.error(HttpStatus.NOT_FOUND, "Captain project not found"));
    }
}