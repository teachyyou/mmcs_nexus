package ru.sfedu.mmcs_nexus.controller.v1.jury;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;
import ru.sfedu.mmcs_nexus.model.enums.controller.jury.GradeTableEnums;
import ru.sfedu.mmcs_nexus.model.payload.jury.CreateGradeRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.jury.GetGradeTableResponsePayload;
import ru.sfedu.mmcs_nexus.service.GradeService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import java.util.Map;

@RestController
public class JuryGradeController {
    private final GradeService gradeService;

    @Autowired
    public JuryGradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping(value = "/api/v1/jury/grades/table/{eventId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getGradesTable(
            @AuthenticationPrincipal OAuth2User user,
            @PathVariable("eventId") @UUID String eventId,
            @RequestParam(value = "show", defaultValue = "all") String showParam,
            @Nullable @RequestParam(value = "day") Integer day)
    {
        GradeTableEnums.ShowFilter show;

        try {
            show = GradeTableEnums.ShowFilter.valueOf(showParam.toUpperCase());

        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                    "Incorrect filter parameter",
                    "value", showParam
            );
        }

        String githubLogin = user.getAttribute("login");

        GetGradeTableResponsePayload table = gradeService.getTable(githubLogin, eventId, show, day);

        return ResponseEntity.ok().body(ResponseUtils.buildResponse(table, table.getProjectsCount()));
    }


    @PostMapping(value = "/api/v1/jury/grades", produces = "application/json")
    public ResponseEntity<?> createGrade(
            @AuthenticationPrincipal OAuth2User user,
            @Valid @RequestBody CreateGradeRequestPayload request
    ) {

        String githubLogin = user.getAttribute("login");
        GradeDTO gradeDTO = gradeService.create(githubLogin, request);

        return ResponseEntity.ok().body(gradeDTO);

    }

    @PutMapping(value = "/api/v1/jury/grades", produces = "application/json")
    public ResponseEntity<?> updateGrade(
            @AuthenticationPrincipal OAuth2User user,
            @Valid @RequestBody CreateGradeRequestPayload request
    ) {
        String githubLogin = user.getAttribute("login");
        GradeDTO gradeDTO = gradeService.edit(githubLogin, request);

        return ResponseEntity.ok().body(gradeDTO);
    }


}
