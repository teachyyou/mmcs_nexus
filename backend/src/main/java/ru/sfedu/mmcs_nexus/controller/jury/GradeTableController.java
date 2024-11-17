package ru.sfedu.mmcs_nexus.controller.jury;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.data.dto.UserDTO;
import ru.sfedu.mmcs_nexus.data.event.EventService;
import ru.sfedu.mmcs_nexus.data.grade.GradeService;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectService;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class GradeTableController {

    private final ProjectService projectService;

    private final ProjectEventService projectEventService;

    private final ProjectJuryEventService projectJuryEventService;

    private final EventService eventService;

    private final GradeService gradeService;


    @Autowired
    public GradeTableController(ProjectService projectService, ProjectEventService projectEventService, ProjectJuryEventService projectJuryEventService, EventService eventService, GradeService gradeService) {
        this.projectService = projectService;
        this.projectEventService = projectEventService;
        this.projectJuryEventService = projectJuryEventService;
        this.eventService = eventService;
        this.gradeService = gradeService;
    }


    @GetMapping(value = "/api/v1/jury/table/{eventId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getGradesTable(
            Authentication authentication,
            @PathVariable("eventId") UUID eventId)
    {
        System.out.println("WOWOWO DEBUG 0");
        //todo check for event existence
        {

        }

        System.out.println("WOWOWO DEBUG 1");
        List<Project> eventProjects = projectEventService.findByEventId(eventId);
        System.out.println("WOWOWO DEBUG 2");
        List<UserDTO> eventJuries = projectJuryEventService.getJuriesByEvent(eventId);
        System.out.println("WOWOWO DEBUG 3");
        for (UserDTO userDTO : eventJuries) {
            System.out.println(STR."WOWOWO \{userDTO.getFirstName()}");
        }

        //temporary for testing

        Map<String, Object> response = new HashMap<>();
        response.put("content", eventJuries);
        response.put("totalElements", eventJuries.size());

        return ResponseEntity.ok().body(response);
    }


}
