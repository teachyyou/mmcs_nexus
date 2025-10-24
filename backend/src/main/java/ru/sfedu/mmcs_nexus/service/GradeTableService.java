package ru.sfedu.mmcs_nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;
import ru.sfedu.mmcs_nexus.model.payload.jury.GetGradeTableResponsePayload;
import ru.sfedu.mmcs_nexus.model.internal.GradeTableRow;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.controller.jury.GradeTableEnums;

import java.util.*;

@Service
public class GradeTableService {

    private final ProjectJuryEventService projectJuryEventService;
    private final ProjectEventService projectEventService;
    private final ProjectService projectService;
    private final GradeService gradeService;
    private final EventService eventService;

    @Autowired
    public GradeTableService(
            ProjectJuryEventService projectJuryEventService,
            ProjectEventService projectEventService,
            ProjectService projectService,
            GradeService gradeService,
            EventService eventService) {
        this.projectJuryEventService = projectJuryEventService;
        this.projectEventService = projectEventService;
        this.projectService = projectService;
        this.gradeService = gradeService;
        this.eventService = eventService;
    }

    //Получение таблицы оценок
    public GetGradeTableResponsePayload getGradeTable(UUID eventId, GradeTableEnums.ShowFilter showFilter, Integer day, User user) {
        Event event = eventService.find(eventId.toString());

        //Находим в зависимости от параметра show, в случае all - все проекты привязанные к событию, иначе - только те, с которыми есть связь у отправителя запроса
        List<Project> eventProjects = projectJuryEventService.findProjectsForEvent(event.getId(), showFilter, user.getId(), day).stream().sorted(Comparator.comparing(Project::getName)).toList();
        List<UserDTO> eventJuries = projectJuryEventService.findJuriesForEvent(event.getId(), showFilter, user.getId(), day).stream().sorted(Comparator.comparing(UserDTO::getLastName)).toList();

        GetGradeTableResponsePayload table = new GetGradeTableResponsePayload();
        table.setEvent(event);
        table.setJuries(eventJuries);
        table.setProjects(eventProjects);

        //Создаем строки для объекта таблицы - каждому проекту ставим в соответствие несколько gradeDTO в формате Map
        for (Project project : eventProjects) {
            UUID mentorId = Optional.ofNullable(projectJuryEventService.getMentor(project.getId(), event.getId())).map(UserDTO::getId).orElse(null);
            GradeTableRow row = new GradeTableRow(project.getId(), mentorId, project.getName());
            List<GradeDTO> grades = gradeService.findByEventAndProject(event.getId(), project.getId())
                    .stream().map(GradeDTO::new).toList();
            row.setTableRow(grades);
            table.addGradeRow(row);
        }

        return table;
    }
}