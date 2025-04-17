package ru.sfedu.mmcs_nexus.controller.v1.jury;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;
import ru.sfedu.mmcs_nexus.model.dto.response.GradeTableDTO;
import ru.sfedu.mmcs_nexus.model.dto.response.GradeTableRowDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.controller.jury.GradeTableEnums;
import ru.sfedu.mmcs_nexus.service.*;

import java.util.*;

@RestController
public class GradeTableController {

    private final ProjectService projectService;

    private final ProjectEventService projectEventService;

    private final ProjectJuryEventService projectJuryEventService;

    private final EventService eventService;

    private final GradeService gradeService;

    private final UserService userService;


    @Autowired
    public GradeTableController(ProjectService projectService, ProjectEventService projectEventService, ProjectJuryEventService projectJuryEventService, EventService eventService, GradeService gradeService, UserService userService) {
        this.projectService = projectService;
        this.projectEventService = projectEventService;
        this.projectJuryEventService = projectJuryEventService;
        this.eventService = eventService;
        this.gradeService = gradeService;
        this.userService = userService;
    }


    @GetMapping(value = "/api/v1/jury/table/{eventId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getGradesTable(
            Authentication authentication,
            @PathVariable("eventId") UUID eventId,
            @RequestParam(value = "show", defaultValue = "all") String showParam)
    {
        //todo check for enum parsing exceptions

        GradeTableEnums.ShowFilter show;

        try {
            show = GradeTableEnums.ShowFilter.valueOf(showParam.toUpperCase());

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = Map.of(
                    "error", "Incorrect filter parameter",
                    "value", showParam
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        if (userService.findByGithubLogin(authentication).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findByGithubLogin(authentication).get();

        Optional<Event> eventOptional = eventService.findById(eventId);

        if (eventOptional.isEmpty()) {
            Map<String, Object> errorResponse = Map.of(
                    "error", "Event not found",
                    "eventId", eventId
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        Event event = eventOptional.get();

        //Находим в зависимости от параметра show, в случае all - все проекты привязанные к событию, иначе - только те, с которыми есть связь у отправителя запроса
        List<Project> eventProjects = projectJuryEventService.findProjectsForEvent(eventId, show, user.getId()).stream().sorted(Comparator.comparing(Project::getName)).toList();
        List<UserDTO> eventJuries = projectJuryEventService.findJuriesForEvent(eventId, show, user.getId()).stream().sorted(Comparator.comparing(UserDTO::getLastName)).toList();

//        List<UserDTO> eventJuries = projectJuryEventService.getJuriesByEvent(eventId);

        GradeTableDTO table = new GradeTableDTO();
        table.setEvent(event);
        table.setJuries(eventJuries);
        table.setProjects(eventProjects);

        //Создаем строки для объекта таблицы - каждому проекту ставим в соответствие несколько gradeDTO в формате Map
        for (Project project : eventProjects) {
            GradeTableRowDTO row = new GradeTableRowDTO(project.getId(), project.getName());
            List<GradeDTO> grades = gradeService.findByEventAndProject(event.getId(), project.getId())
                    .stream().map(GradeDTO::new).toList();
            row.setTableRow(grades);
            table.addGradeRow(row);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", table);
        response.put("totalElements", table.getProjectsCount());

        return ResponseEntity.ok().body(response);
    }



}
