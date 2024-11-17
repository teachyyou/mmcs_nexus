package ru.sfedu.mmcs_nexus.data.dto;

import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.grade.Grade;
import ru.sfedu.mmcs_nexus.data.project.Project;

import java.util.List;

public class GradeTableDTO {

    private int juriesCount;

    private int projectsCount;

    private Event event;

    private List<Project> projects;

    private List<UserDTO> juries;

    private List<Grade> grades;



}
