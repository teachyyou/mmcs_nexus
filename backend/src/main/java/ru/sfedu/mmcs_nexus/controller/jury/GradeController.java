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
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventKey;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventService;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserService;

import java.util.*;

@RestController
public class GradeController {

    private final GradeService gradeService;
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectEventService projectEventService;
    private final EventService eventService;
    private final ProjectJuryEventService projectJuryEventService;

    @Autowired
    public GradeController(GradeService gradeService, UserService userService, ProjectService projectService, ProjectEventService projectEventService, EventService eventService, ProjectJuryEventService projectJuryEventService) {
        this.gradeService = gradeService;
        this.userService = userService;
        this.projectService = projectService;
        this.projectEventService = projectEventService;
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
        List<GradeDTO> grades = gradeService.findByJuryForYear(juryId, year).stream().map(GradeDTO::new).toList();

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
        List<GradeDTO> grades = gradeService.findByYear(year).stream().map(GradeDTO::new).toList();

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
        if (userService.findByGithubLogin(authentication).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (projectService.findById(gradeDTO.getProjectId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project not found");
        } else if (eventService.findById(gradeDTO.getEventId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Event not found");
        } else if (projectEventService.findById(new ProjectEventKey(gradeDTO.getProjectId(), gradeDTO.getEventId())).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project and event are not linked");
        }

        Event event = eventService.findById(gradeDTO.getEventId()).get();
        Project project = projectService.findById(gradeDTO.getProjectId()).get();
        User creator = userService.findByGithubLogin(authentication).get();

        gradeDTO.setJuryId(creator.getId());


        if (gradeDTO.getPresPoints() != null && gradeDTO.getPresPoints() > event.getMaxPresPoints()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(STR."Maximum presentation score for \{event.getName()} is \{event.getMaxPresPoints()}");
        } else if (gradeDTO.getBuildPoints() != null && gradeDTO.getBuildPoints() > event.getMaxBuildPoints()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(STR."Maximum build score for \{event.getName()} is \{event.getMaxBuildPoints()}");
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

        if (gradeService.findById(key).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Grade already exists");
        }

        Grade grade = new Grade();

        grade.setId(key);
        grade.setEvent(event);
        grade.setProject(project);
        grade.setJury(creator);

        if (gradeDTO.getPresPoints() != null) {
            grade.setPresPoints(gradeDTO.getPresPoints());
        }
        if (gradeDTO.getBuildPoints() != null) {
            grade.setBuildPoints(gradeDTO.getBuildPoints());
        }
        if (gradeDTO.getComment() != null) {
            grade.setComment(gradeDTO.getComment());
        }

        gradeService.save(grade);

        return ResponseEntity.ok().body(gradeDTO);

    }

    @PutMapping(value = "/api/v1/jury/grades", produces = "application/json")
    public ResponseEntity<?> updateGrade(
            Authentication authentication,
            @Valid @RequestBody GradeDTO gradeDTO
    ) {

        if (userService.findByGithubLogin(authentication).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User editor = userService.findByGithubLogin(authentication).get();

        GradeKey key = new GradeKey(
                gradeDTO.getProjectId(),
                gradeDTO.getEventId(),
                editor.getId()
        );

        // Получаем пользователя (проверяющего) из аутентификации
        if (userService.findByGithubLogin(authentication).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (gradeService.findById(key).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Grade not found");
        } else if (projectService.findById(gradeDTO.getProjectId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project not found");
        } else if (eventService.findById(gradeDTO.getEventId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Event not found");
        } else if (projectEventService.findById(new ProjectEventKey(gradeDTO.getProjectId(), gradeDTO.getEventId())).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project and event are not linked");
        }

        Grade existingGrade = gradeService.findById(key).get();
        Event event = eventService.findById(gradeDTO.getEventId()).get();

        if (gradeDTO.getPresPoints() != null && gradeDTO.getPresPoints() > event.getMaxPresPoints()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(STR."Maximum presentation score for \{event.getName()} is \{event.getMaxPresPoints()}");
        } else if (gradeDTO.getBuildPoints() != null && gradeDTO.getBuildPoints() > event.getMaxBuildPoints()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(STR."Maximum build score for \{event.getName()} is \{event.getMaxBuildPoints()}");
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

        return ResponseEntity.ok().body(new GradeDTO(existingGrade));
    }



}
