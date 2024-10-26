package ru.sfedu.mmcs_nexus.controller.jury;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sfedu.mmcs_nexus.data.grade.Grade;
import ru.sfedu.mmcs_nexus.data.grade.GradeService;
import ru.sfedu.mmcs_nexus.data.user.UserService;

import java.util.*;

public class GradeSpreadsheetController {

    private final GradeService gradeService;
    private final UserService userService;

    @Autowired
    public GradeSpreadsheetController(GradeService gradeService, UserService userService) {
        this.gradeService = gradeService;
        this.userService = userService;
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
}
