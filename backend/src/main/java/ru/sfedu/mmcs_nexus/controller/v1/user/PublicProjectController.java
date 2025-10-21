package ru.sfedu.mmcs_nexus.controller.v1.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;
import ru.sfedu.mmcs_nexus.service.ProjectService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import java.util.Map;


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
    public ResponseEntity<Map<String, Object>> getProjectsByEvent
    (
            @PathVariable("id") @UUID String eventId,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            @RequestParam(required = false) @Min(1) @Max(2) Integer day
    ) {
        PaginationPayload paginationPayload = new PaginationPayload(limit, offset, sort, order, EntitySort.PROJECT_SORT);

        Page<Project> projects = projectEventService.findProjectsByEvent(eventId, day, paginationPayload);

        return ResponseEntity.ok().body(
                ResponseUtils.buildResponse(projects.getContent(), projects.getTotalElements())
        );
    }
}
