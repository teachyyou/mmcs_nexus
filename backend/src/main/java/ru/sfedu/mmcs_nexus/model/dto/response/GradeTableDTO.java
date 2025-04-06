package ru.sfedu.mmcs_nexus.model.dto.response;

import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;

import java.util.*;

public class GradeTableDTO {

    private int juriesCount;

    private int projectsCount;

    private Event event;

    private List<Project> projects;

    private List<UserDTO> juries;

    //table rows for every project in the table
    private List<GradeTableRowDTO> rows;

    public GradeTableDTO() {
        this.rows = new ArrayList<>();
    }


    public int getJuriesCount() {
        return juriesCount;
    }

    public void setJuriesCount(int juriesCount) {
        this.juriesCount = juriesCount;
    }

    public int getProjectsCount() {
        return projectsCount;
    }

    public void setProjectsCount(int projectsCount) {
        this.projectsCount = projectsCount;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        this.projectsCount=projects.size();
    }

    public List<UserDTO> getJuries() {
        return juries;
    }

    public void setJuries(List<UserDTO> juries) {
        this.juries = juries;
        this.juriesCount=juries.size();
    }

    public List<GradeTableRowDTO> getRows() {
        return rows;
    }

    public void setRows(List<GradeTableRowDTO> rows) {
        this.rows = rows;
    }

    public void addGradeRow(GradeTableRowDTO row) {
        this.rows.add(row);
    }

    //temporary unused `
//    public void addGrade(Project project, User jury, GradeDTO gradeDTO) {
//
//        rows.stream().filter(x->x.getProjectId().equals(project.getId())).findFirst().get();
//
//        if (this.rows.containsKey(project.getId())) {
//            rows.get(project.getId()).getTableRow().add(gradeDTO);
//        } else {
//            this.rows.put(project.getId(),new GradeTableRowDTO(project.getId(), project.getName(), new ArrayList<>()));
//        }
//    }
}
