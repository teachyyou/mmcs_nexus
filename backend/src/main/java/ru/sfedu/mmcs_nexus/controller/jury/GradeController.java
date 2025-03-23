package ru.sfedu.mmcs_nexus.controller.jury;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.data.dto.GradeDTO;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.event.EventService;
import ru.sfedu.mmcs_nexus.data.grade.Grade;
import ru.sfedu.mmcs_nexus.data.grade.GradeKey;
import ru.sfedu.mmcs_nexus.data.grade.GradeService;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectService;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserService;

import java.util.*;

@RestController
public class GradeController {

    private final GradeService gradeService;
    private final UserService userService;
    private final ProjectService projectService;
    private final EventService eventService;
    private final ProjectJuryEventService projectJuryEventService;

    @Autowired
    public GradeController(GradeService gradeService, UserService userService, ProjectService projectService, EventService eventService, ProjectJuryEventService projectJuryEventService) {
        this.gradeService = gradeService;
        this.userService = userService;
        this.projectService = projectService;
        this.eventService = eventService;
        this.projectJuryEventService = projectJuryEventService;
    }




    //todo вынести эти методы по другим контроллерам

    @GetMapping(value = "/api/v1/jury/grades/my", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getGradesByYearForJury(
            Authentication authentication,
            @RequestParam(defaultValue = "2024") int year)
    {
        UUID juryId = userService.findByGithubLogin(authentication).orElseThrow(()->new EntityNotFoundException("Jury not found")).getId();
        List<Grade> grades = gradeService.findByJuryForYear(juryId, year);

        Map<String, Object> response = new HashMap<>();
        response.put("content", grades);
        response.put("totalElements", grades.size());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/api/v1/jury/grades", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getGradesByYear(
            Authentication authentication,
            @RequestParam(defaultValue = "2024") int year)
    {
        List<Grade> grades = gradeService.findByYear(year);

        Map<String, Object> response = new HashMap<>();
        response.put("content", grades);
        response.put("totalElements", grades.size());

        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/api/v1/jury/grades", produces = "application/json")
    public ResponseEntity<?> createGrade(
            Authentication authentication,
            @Valid @RequestBody GradeDTO gradeDTO
    ) {

        // Получаем пользователя (проверяющего) из аутентификации
        User creator = userService.findByGithubLogin(authentication)
                .orElseThrow(() -> new UsernameNotFoundException("Jury not found"));
        gradeDTO.setJuryId(creator.getId());


        if (projectService.findById(gradeDTO.getProjectId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project not found");
        } else if (eventService.findById(gradeDTO.getEventId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Event not found");
        }


        ProjectJuryEvent.RelationType relationType = projectJuryEventService.getRelationType(
                gradeDTO.getProjectId(),
                gradeDTO.getEventId(),
                creator.getId());

        //Если ментор - запрещаем, если отсутствует связь - ставим как проверяющего по желанию
        if (relationType == ProjectJuryEvent.RelationType.MENTOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Ментор не имеет права оценивать закрепленный за собой проект");
        } else if (relationType == null) {
            projectJuryEventService.addJuryToProjectEvent(gradeDTO.getProjectId(), gradeDTO.getEventId(), creator.getId(), ProjectJuryEvent.RelationType.WILLING);
        }


        GradeKey key = new GradeKey(
                gradeDTO.getProjectId(),
                gradeDTO.getEventId(),
                creator.getId()
        );

        Grade grade = new Grade();

        grade.setId(key);
        grade.setComment(gradeDTO.getComment());
        grade.setPresPoints(gradeDTO.getPresPoints());
        grade.setBuildPoints(gradeDTO.getBuildPoints());
        grade.setEvent(eventService.findById(gradeDTO.getEventId()).get());
        grade.setProject(projectService.findById(gradeDTO.getProjectId()).get());
        grade.setJury(creator);

        gradeService.save(grade);

        return ResponseEntity.ok().body(gradeDTO);

    }

    @PutMapping(value = "/api/v1/jury/grades", produces = "application/json")
    public ResponseEntity<?> updateGrade(
            Authentication authentication,
            @Valid @RequestBody GradeDTO gradeDTO
    ) {

        User editor = userService.findByGithubLogin(authentication)
                .orElseThrow(() -> new UsernameNotFoundException("Jury not found"));

        GradeKey key = new GradeKey(
                gradeDTO.getProjectId(),
                gradeDTO.getEventId(),
                editor.getId()
        );

        Grade existingGrade;

        if (gradeService.findById(key).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Grade not found");
        } else {
            existingGrade = gradeService.findById(key).get();
        }


        if (gradeDTO.getPresPoints() != null) {
            existingGrade.setPresPoints(gradeDTO.getPresPoints());
        }
        if (gradeDTO.getBuildPoints() != null) {
            existingGrade.setBuildPoints(gradeDTO.getBuildPoints());
        }
        if (gradeDTO.getComment() != null) {
            existingGrade.setComment(gradeDTO.getComment());
        }

        gradeService.save(existingGrade);

        return ResponseEntity.ok().body(existingGrade);
    }

}
