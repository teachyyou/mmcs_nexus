package ru.sfedu.mmcs_nexus.controller.v1.admin;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.AssignJuriesRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateProjectRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.ImportResponsePayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.ProjectJuryEventResponsePayload;
import ru.sfedu.mmcs_nexus.service.ImportService;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;
import ru.sfedu.mmcs_nexus.service.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.service.ProjectService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import java.util.Map;

import static ru.sfedu.mmcs_nexus.util.ResponseUtils.buildPageResponse;

@RestController
public class AdminProjectController {

    private final ProjectService projectService;
    private final ProjectEventService projectEventService;
    private final ProjectJuryEventService projectJuryEventService;
    private final ImportService importService;

    @Autowired
    public AdminProjectController(
            ProjectService projectService,
            ProjectEventService projectEventService,
            ProjectJuryEventService projectJuryEventService,
            ImportService importService
    ) {
        this.projectService = projectService;
        this.projectEventService = projectEventService;
        this.projectJuryEventService = projectJuryEventService;
        this.importService = importService;
    }

    @GetMapping(value = "/api/v1/admin/projects", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getProjectsList(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            @RequestParam(required = false) Integer year
    ) {
        PaginationPayload paginationPayload = new PaginationPayload(limit, offset, sort, order, EntitySort.PROJECT_SORT);

        Page<ProjectDTO> projects = projectService.findAll(year, paginationPayload);

        return buildPageResponse(projects);
    }

    @GetMapping(value = "/api/v1/admin/projects/{id}", produces = "application/json")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable("id") @UUID String projectId) {
        ProjectDTO project = new ProjectDTO(projectService.find(projectId));

        return ResponseEntity.ok(project);
    }

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
    public ResponseEntity<ProjectDTO> editProjectById(
            @PathVariable("id") @UUID String projectId,
            @Valid @RequestBody CreateProjectRequestPayload payload
    ) {
        Project project = projectService.edit(projectId, payload);

        return ResponseEntity.ok(new ProjectDTO(project));
    }

    @DeleteMapping("/api/v1/admin/projects/{id}")
    public ResponseEntity<?> deleteProjectById(@PathVariable("id") @UUID String projectId) {
        projectService.delete(projectId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            value = "/api/v1/admin/projects/from_csv",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "application/json"
    )
    public ResponseEntity<ImportResponsePayload<ProjectDTO>> importProjectsFromCsv(
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "limit", defaultValue = "2") int limit
    ) {
        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, limit);

        ImportResponsePayload<ProjectDTO> response = new ImportResponsePayload<>(
                result.created().stream()
                        .map(ProjectDTO::new)
                        .toList(),
                result.skipped()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/api/v1/admin/projects/{project_id}/juries/{event_id}", produces = "application/json")
    public ResponseEntity<?> getProjectEventJuriesById(
            @PathVariable("project_id") @UUID String projectId,
            @PathVariable("event_id") @UUID String eventId
    ) {
        ProjectJuryEventResponsePayload response = projectJuryEventService.getJuriesByProjectAndEvent(projectId, eventId);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/api/v1/admin/projects/assign", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> saveProjectEventJuries(@Valid @RequestBody AssignJuriesRequestPayload request) {
        projectJuryEventService.assignJuries(request);

        return ResponseUtils.success(HttpStatus.OK, "saved successfully");
    }
}