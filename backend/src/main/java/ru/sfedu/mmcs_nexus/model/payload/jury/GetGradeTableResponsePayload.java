package ru.sfedu.mmcs_nexus.model.payload.jury;

import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.internal.GradeTableRow;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetGradeTableResponsePayload {
    private int juriesCount;
    private int projectsCount;

    private Event event;

    private List<Project> projects;
    private List<UserDTO> juries;

    //table rows for every project in the table
    private List<GradeTableRow> rows;

    public GetGradeTableResponsePayload() {
        this.rows = new ArrayList<>();
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        this.projectsCount=projects.size();
    }

    public void setJuries(List<UserDTO> juries) {
        this.juries = juries;
        this.juriesCount=juries.size();
    }

    public void addGradeRow(GradeTableRow row) {
        this.rows.add(row);
    }

}
