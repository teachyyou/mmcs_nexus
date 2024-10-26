package ru.sfedu.mmcs_nexus.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sfedu.mmcs_nexus.data.event.EventService;
import ru.sfedu.mmcs_nexus.data.grade.Grade;
import ru.sfedu.mmcs_nexus.data.grade.GradeKey;
import ru.sfedu.mmcs_nexus.data.grade.GradeService;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.data.project.ProjectService;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventService;
import ru.sfedu.mmcs_nexus.data.user.User;

import java.util.UUID;

@Controller
public class AdminGradeController {

    private final ProjectService projectService;

    private final ProjectEventService projectEventService;

    private final ProjectJuryEventService projectJuryEventService;

    private final EventService eventService;

    private final GradeService gradeService;

    public AdminGradeController(ProjectService projectService, ProjectEventService projectEventService, ProjectJuryEventService projectJuryEventService, EventService eventService, GradeService gradeService) {
        this.projectService = projectService;
        this.projectEventService = projectEventService;
        this.projectJuryEventService = projectJuryEventService;
        this.eventService = eventService;
        this.gradeService = gradeService;
    }

    @GetMapping(value = "/api/v1/admin/grades", produces = "application/json")
    public ResponseEntity<Grade> getGradeById(
            @RequestParam("projectId") UUID projectId,
            @RequestParam("juryId") UUID juryId,
            @RequestParam("eventId") UUID eventId,
            Authentication authentication) {
        Grade grade = gradeService.findById(new GradeKey(projectId, eventId, juryId))
                .orElseThrow(() -> new EntityNotFoundException("Grade not found"));
        return ResponseEntity.ok(grade);
    }


}
