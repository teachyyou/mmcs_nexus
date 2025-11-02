package ru.sfedu.mmcs_nexus.controller.v1.jury;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;
import ru.sfedu.mmcs_nexus.model.entity.*;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.payload.jury.CreateGradeRequestPayload;
import ru.sfedu.mmcs_nexus.service.*;

import java.util.Map;
import java.util.UUID;

@RestController
public class JuryGradeController {
    private final GradeService gradeService;

    @Autowired
    public JuryGradeController(GradeService gradeService) {
        this.gradeService = gradeService;
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
        GradeDTO gradeDTO = gradeService.create(githubLogin, request);

        return ResponseEntity.ok().body(gradeDTO);
    }


}
