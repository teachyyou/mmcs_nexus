package ru.sfedu.mmcs_nexus.controller.v1.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;
import ru.sfedu.mmcs_nexus.service.EventService;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;
import ru.sfedu.mmcs_nexus.service.GradeService;
import ru.sfedu.mmcs_nexus.service.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.service.ProjectService;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;

@RestController
public class AdminGradeController {

    private final ProjectService projectService;

    private final ProjectEventService projectEventService;

    private final ProjectJuryEventService projectJuryEventService;

    private final EventService eventService;

    private final GradeService gradeService;

    @Autowired
    public AdminGradeController(ProjectService projectService, ProjectEventService projectEventService, ProjectJuryEventService projectJuryEventService, EventService eventService, GradeService gradeService) {
        this.projectService = projectService;
        this.projectEventService = projectEventService;
        this.projectJuryEventService = projectJuryEventService;
        this.eventService = eventService;
        this.gradeService = gradeService;
    }

    @GetMapping(value = "/api/v1/admin/grades", produces = "application/json")
    public ResponseEntity<GradeDTO> getGradeById(@Valid GradeKey key, Authentication authentication) {
        GradeDTO gradeDTO = new GradeDTO(gradeService.findById(key)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found")));
        return ResponseEntity.ok(gradeDTO);
    }


}
