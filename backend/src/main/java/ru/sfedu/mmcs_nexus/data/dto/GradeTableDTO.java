package ru.sfedu.mmcs_nexus.data.dto;

import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GradeTableDTO {

    private int juriesCount;

    private int projectsCount;

    private Event event;

    private List<Project> projects;

    private List<UserDTO> juries;

    //table rows for every project in the table
    private Map<UUID,GradeTableRowDTO> rows;

    public GradeTableDTO() {
        this.rows = new HashMap<>();
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

    public Map<UUID, GradeTableRowDTO> getRows() {
        return rows;
    }

    public void setRows(Map<UUID, GradeTableRowDTO> rows) {
        this.rows = rows;
    }

    public void addGradeRow(GradeTableRowDTO row) {
        this.rows.put(row.getProjectId(), row);
    }

    public void addGrade(Project project, User jury, GradeDTO gradeDTO) {
        if (this.rows.containsKey(project.getId())) {
            rows.get(project.getId()).getTableRow().put(jury.getId(), gradeDTO);
        } else {
            this.rows.put(project.getId(),new GradeTableRowDTO(project.getId(), project.getName(), new HashMap<UUID ,GradeDTO>()));
        }
    }
}
