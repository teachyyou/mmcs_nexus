package ru.sfedu.mmcs_nexus.controller.jury;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.data.grade.Grade;
import ru.sfedu.mmcs_nexus.data.grade.GradeKey;
import ru.sfedu.mmcs_nexus.data.grade.GradeService;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserService;

import java.util.*;

public class GradeSpreadsheetController {

    private final GradeService gradeService;
    private final UserService userService;
    private final ProjectJuryEventService projectJuryEventService;

    @Autowired
    public GradeSpreadsheetController(GradeService gradeService, UserService userService, ProjectJuryEventService projectJuryEventService) {
        this.gradeService = gradeService;
        this.userService = userService;
        this.projectJuryEventService = projectJuryEventService;
    }

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
    public ResponseEntity<Map<String, Object>> getAllGradesByYear(
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
            @RequestBody Grade grade
    ) {

        //Нужно убедиться, что в качестве создателя запишется тот, с чьего аккаунта был отправлен запрос
        User creator = userService.findByGithubLogin(authentication).orElseThrow(()->new UsernameNotFoundException("Jury not found"));

        ProjectJuryEvent.RelationType relationType = projectJuryEventService.getRelationType(
                grade.getProject().getId(),
                grade.getEvent().getId(),
                creator.getId());

        if (relationType == null || relationType == ProjectJuryEvent.RelationType.MENTOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Жюри не имеет права оценивать данный проект");
        }

        grade.setJury(creator);
        GradeKey key = new GradeKey(
                grade.getProject().getId(),
                grade.getEvent().getId(),
                creator.getId()
                );
        grade.setId(key);

        gradeService.save(grade);

        return ResponseEntity.ok().body(grade);

    }

    @PutMapping(value = "/api/v1/jury/grades", produces = "application/json")
    public ResponseEntity<?> updateGrade(
            Authentication authentication,
            @RequestBody Grade grade
    ) {

        User editor = userService.findByGithubLogin(authentication)
                .orElseThrow(() -> new UsernameNotFoundException("Jury not found"));

        Grade existingGrade = gradeService.findById(grade.getId())
                .orElseThrow(() -> new IllegalArgumentException("Grade not found for given ID"));

        // Проверяем, что текущий пользователь имеет право редактировать оценку
        ProjectJuryEvent.RelationType relationType = projectJuryEventService.getRelationType(
                grade.getProject().getId(),
                grade.getEvent().getId(),
                editor.getId());

        if (relationType == null || relationType == ProjectJuryEvent.RelationType.MENTOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Жюри не имеет права оценивать данный проект");
        }

        if (grade.getPresPoints() != null) {
            existingGrade.setPresPoints(grade.getPresPoints());
        }
        if (grade.getBuildPoints() != null) {
            existingGrade.setBuildPoints(grade.getBuildPoints());
        }
        if (grade.getComment() != null) {
            existingGrade.setComment(grade.getComment());
        }

        gradeService.save(existingGrade);

        return ResponseEntity.ok().body(existingGrade);
    }

}
