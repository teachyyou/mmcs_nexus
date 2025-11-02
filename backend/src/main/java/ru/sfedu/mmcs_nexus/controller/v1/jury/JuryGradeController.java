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

import java.util.UUID;

@RestController
public class JuryGradeController {
    private final GradeService gradeService;
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectEventService projectEventService;
    private final EventService eventService;
    private final ProjectJuryEventService projectJuryEventService;

    @Autowired
    public JuryGradeController(GradeService gradeService, UserService userService, ProjectService projectService, ProjectEventService projectEventService, EventService eventService, ProjectJuryEventService projectJuryEventService) {
        this.gradeService = gradeService;
        this.userService = userService;
        this.projectService = projectService;
        this.projectEventService = projectEventService;
        this.eventService = eventService;
        this.projectJuryEventService = projectJuryEventService;
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
        } else if (projectEventService.findById(new ProjectEventKey(gradeDTO.getProjectId(), gradeDTO.getEventId())).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project and event are not linked");
        }

        Grade existingGrade = gradeService.find(key);
        Event event = eventService.find(gradeDTO.getEventId().toString());

        if (gradeDTO.getPresPoints() != null && gradeDTO.getPresPoints() > event.getMaxPresPoints()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(STR."Maximum presentation score for \{event.getName()} is \{event.getMaxPresPoints()}");
        } else if (gradeDTO.getBuildPoints() != null && gradeDTO.getBuildPoints() > event.getMaxBuildPoints()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(STR."Maximum build score for \{event.getName()} is \{event.getMaxBuildPoints()}");
        }

        existingGrade.setPresPoints(gradeDTO.getPresPoints());
        existingGrade.setBuildPoints(gradeDTO.getBuildPoints());
        existingGrade.setComment(gradeDTO.getComment());

        gradeService.save(existingGrade);

        return ResponseEntity.ok().body(new GradeDTO(existingGrade));
    }


}
