package ru.sfedu.mmcs_nexus.data.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GradeTableRowDTO {

    private UUID projectId;

    private String projectDisplayName;

    //maps jury to its grade in table
    private List<GradeDTO> tableRow;

    public GradeTableRowDTO(UUID projectId, String projectDisplayName, List<GradeDTO> tableRow) {
        this.projectDisplayName=projectDisplayName;
        this.projectId=projectId;
        this.tableRow=tableRow;
    }

    public GradeTableRowDTO(UUID projectId, String projectDisplayName) {
        this.projectDisplayName=projectDisplayName;
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
}
