package ru.sfedu.mmcs_nexus.controller.v1.jury;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.model.payload.jury.GetGradeTableResponsePayload;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.controller.jury.GradeTableEnums;
import ru.sfedu.mmcs_nexus.service.*;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import java.util.*;

@RestController
public class GradeTableController {

    private final ProjectService projectService;

    private final ProjectEventService projectEventService;

    private final ProjectJuryEventService projectJuryEventService;

    private final EventService eventService;

    private final GradeService gradeService;

    private final UserService userService;

    private final GradeTableService gradeTableService;


    @Autowired
    public GradeTableController(ProjectService projectService, ProjectEventService projectEventService, ProjectJuryEventService projectJuryEventService, EventService eventService, GradeService gradeService, UserService userService, GradeTableService gradeTableService) {
        this.projectService = projectService;
        this.projectEventService = projectEventService;
        this.projectJuryEventService = projectJuryEventService;
        this.eventService = eventService;
        this.gradeService = gradeService;
        this.userService = userService;
        this.gradeTableService  = gradeTableService;
    }


    @GetMapping(value = "/api/v1/jury/table/{eventId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getGradesTable(
            Authentication authentication,
            @PathVariable("eventId") UUID eventId,
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

        User user = userService.findByGithubLogin(authentication)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        GetGradeTableResponsePayload table = gradeTableService.getGradeTable(eventId, show, day, user);

        return ResponseEntity.ok().body(ResponseUtils.buildResponse(table, table.getProjectsCount()));
    }



}
