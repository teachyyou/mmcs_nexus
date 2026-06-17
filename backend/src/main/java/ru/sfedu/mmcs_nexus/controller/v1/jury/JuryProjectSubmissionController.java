package ru.sfedu.mmcs_nexus.controller.v1.jury;

import org.hibernate.validator.constraints.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.dto.entity.JuryProjectSubmissionDTO;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.jury.GetJurySubmissionEventsResponsePayload;
import ru.sfedu.mmcs_nexus.service.ProjectEventSubmissionService;

import java.util.Map;

import static ru.sfedu.mmcs_nexus.util.ResponseUtils.buildPageResponse;

@RestController
public class JuryProjectSubmissionController {

    private final ProjectEventSubmissionService projectEventSubmissionService;

    public JuryProjectSubmissionController(ProjectEventSubmissionService projectEventSubmissionService) {
        this.projectEventSubmissionService = projectEventSubmissionService;
    }

    @GetMapping(value = "/api/v1/jury/submissions/events", produces = "application/json")
    public ResponseEntity<GetJurySubmissionEventsResponsePayload> getSubmissionEvents(
            @RequestParam(required = false) Integer year
    ) {
        int targetYear = year != null
                ? year
                : java.time.Year.now().getValue();

        return ResponseEntity.ok(projectEventSubmissionService.getJurySubmissionEvents(targetYear));
    }

    @GetMapping(value = "/api/v1/jury/submissions/events/{eventId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getSubmissionsByEvent(
            @PathVariable("eventId") @UUID String eventId,
            @RequestParam(defaultValue = "200") Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset
    ) {
        PaginationPayload paginationPayload = new PaginationPayload(
                limit,
                offset
        );

        Page<JuryProjectSubmissionDTO> submissions =
                projectEventSubmissionService.getJurySubmissionsByEvent(eventId, paginationPayload);

        return buildPageResponse(submissions);
    }
}