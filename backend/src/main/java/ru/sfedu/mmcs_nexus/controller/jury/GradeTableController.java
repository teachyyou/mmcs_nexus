package ru.sfedu.mmcs_nexus.controller.jury;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.data.dto.GradeDTO;
import ru.sfedu.mmcs_nexus.data.dto.GradeTableDTO;
import ru.sfedu.mmcs_nexus.data.dto.GradeTableRowDTO;
import ru.sfedu.mmcs_nexus.data.dto.UserDTO;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.event.EventService;
import ru.sfedu.mmcs_nexus.data.grade.Grade;
import ru.sfedu.mmcs_nexus.data.grade.GradeService;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectService;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventService;

import java.util.*;
import java.util.stream.Collectors;

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
        Optional<Event> eventOptional = eventService.findById(eventId);

        if (eventOptional.isEmpty()) {
            Map<String, Object> errorResponse = Map.of(
                    "error", "Event not found",
                    "eventId", eventId
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);        }
        Event event = eventOptional.get();

        System.out.println("WOWOWO DEBUG 1");
        List<Project> eventProjects = projectEventService.findByEventId(eventId);
        List<UserDTO> eventJuries = projectJuryEventService.getJuriesByEvent(eventId);

        GradeTableDTO table = new GradeTableDTO();
        table.setEvent(event);
        table.setJuries(eventJuries);
        table.setProjects(eventProjects);

        //Создаем строки для объекта таблицы - каждому проекту ставим в соответствие несколько gradeDTO в формате Map
        for (Project project : eventProjects) {
            GradeTableRowDTO row = new GradeTableRowDTO(project.getId(), project.getName());
            List<Grade> grades = gradeService.findByEventAndProject(event.getId(), project.getId());
            Map<UUID, GradeDTO> rowMap = grades.stream().collect(Collectors.toMap(grade -> grade.getJury().getId(), grade ->
                    new GradeDTO(
                            grade.getId(),
                            project.getName(),
                            grade.getJury().getFullName(),
                            event.getName(),
                            grade.getComment(),
                            grade.getPresPoints(),
                            grade.getPresPoints()

                    )));
            row.setTableRow(rowMap);
            table.addGradeRow(row);
        }

        //temporary for testing

        Map<String, Object> response = new HashMap<>();
        response.put("content", table);
        response.put("totalElements", table.getProjectsCount());

        return ResponseEntity.ok().body(response);
    }


}
