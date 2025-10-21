package ru.sfedu.mmcs_nexus.model.internal;

import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;

import java.util.List;
import java.util.UUID;

public class GradeTableRow {

    private UUID projectId;

    private UUID mentorId;

    private String projectDisplayName;

    //maps jury to its grade in table
    private List<GradeDTO> tableRow;

    public GradeTableRow(UUID projectId, UUID mentorId, String projectDisplayName, List<GradeDTO> tableRow) {
        this.projectDisplayName=projectDisplayName;
        this.mentorId=mentorId;
        this.projectId=projectId;
        this.tableRow=tableRow;
    }

    public GradeTableRow(UUID projectId, UUID mentorId, String projectDisplayName) {
        this.projectDisplayName=projectDisplayName;
        this.mentorId=mentorId;
        this.projectId=projectId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getProjectDisplayName() {
        return projectDisplayName;
    }

    public void setProjectDisplayName(String projectDisplayName) {
        this.projectDisplayName = projectDisplayName;
    }

    public List<GradeDTO> getTableRow() {
        return tableRow;
    }

    public void setTableRow(List<GradeDTO> tableRow) {
        this.tableRow = tableRow;
    }

    public UUID getMentorId() {
        return mentorId;
    }

    public void setMentorId(UUID mentorId) {
        this.mentorId = mentorId;
    }
}
