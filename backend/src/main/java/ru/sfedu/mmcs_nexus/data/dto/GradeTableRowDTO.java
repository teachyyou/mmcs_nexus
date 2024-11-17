package ru.sfedu.mmcs_nexus.data.dto;

import java.util.Map;
import java.util.UUID;

public class GradeTableRowDTO {

    private UUID projectId;

    private String projectDisplayName;

    private Map<UUID,GradeDTO> tableRow;

    public GradeTableRowDTO(UUID projectId, String projectDisplayName, Map<UUID,GradeDTO> tableRow) {
        this.projectDisplayName=projectDisplayName;
        this.projectId=projectId;
        this.tableRow=tableRow;

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

    public Map<UUID, GradeDTO> getTableRow() {
        return tableRow;
    }

    public void setTableRow(Map<UUID, GradeDTO> tableRow) {
        this.tableRow = tableRow;
    }
}
