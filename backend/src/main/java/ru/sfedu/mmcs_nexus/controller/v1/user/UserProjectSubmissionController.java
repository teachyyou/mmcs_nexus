package ru.sfedu.mmcs_nexus.controller.v1.user;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectEventSubmissionDTO;
import ru.sfedu.mmcs_nexus.model.payload.user.GetProjectEventSubmissionsResponsePayload;
import ru.sfedu.mmcs_nexus.model.payload.user.SaveProjectEventSubmissionRequestPayload;
import ru.sfedu.mmcs_nexus.service.ProjectEventSubmissionService;

@RestController
public class UserProjectSubmissionController {

    private final ProjectEventSubmissionService projectEventSubmissionService;

    @Autowired
    public UserProjectSubmissionController(ProjectEventSubmissionService projectEventSubmissionService) {
        this.projectEventSubmissionService = projectEventSubmissionService;
    }

    @GetMapping(value = "/api/v1/user/projects/{projectId}/submissions", produces = "application/json")
    public ResponseEntity<GetProjectEventSubmissionsResponsePayload> getSubmissions(
            @AuthenticationPrincipal OAuth2User user,
            @PathVariable("projectId") @UUID String projectId
    ) {
        String githubLogin = user.getAttribute("login");

        return ResponseEntity.ok(projectEventSubmissionService.getSubmissions(projectId, githubLogin));
    }

    @PutMapping(value = "/api/v1/user/projects/{projectId}/events/{eventId}/submission", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProjectEventSubmissionDTO> saveSubmission(
            @AuthenticationPrincipal OAuth2User user,
            @PathVariable("projectId") @UUID String projectId,
            @PathVariable("eventId") @UUID String eventId,
            @Valid @RequestBody SaveProjectEventSubmissionRequestPayload payload
    ) {
        String githubLogin = user.getAttribute("login");

        return ResponseEntity.ok(projectEventSubmissionService.saveSubmission(projectId, eventId, githubLogin, payload));
    }
}