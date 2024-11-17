package ru.sfedu.mmcs_nexus.data.dto;

import ru.sfedu.mmcs_nexus.data.grade.GradeKey;

import java.util.UUID;

public class GradeDTO {

    private GradeKey id;

    private String projectDisplayName;

    private String juryName;

    private String eventName;

    private String comment;

    private Integer presPoints;

    private Integer buildPoints;

    public GradeDTO(GradeKey id, String projectDisplayName, String juryName, String eventName, String comment, Integer presPoints, Integer buildPoints) {
        this.id=id;
        this.projectDisplayName=projectDisplayName;
        this.juryName=juryName;
        this.eventName=eventName;
        this.comment=comment;
        this.presPoints=presPoints;
        this.buildPoints=buildPoints;
    }



    public GradeKey getId() {
        return id;
    }

    public void setId(GradeKey id) {
        this.id = id;
    }

    public String getProjectDisplayName() {
        return projectDisplayName;
    }

    public void setProjectDisplayName(String projectDisplayName) {
        this.projectDisplayName = projectDisplayName;
    }

    public String getJuryName() {
        return juryName;
    }

    public void setJuryName(String juryName) {
        this.juryName = juryName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getPresPoints() {
        return presPoints;
    }

    public void setPresPoints(Integer presPoints) {
        this.presPoints = presPoints;
    }

    public Integer getBuildPoints() {
        return buildPoints;
    }

    public void setBuildPoints(Integer buildPoints) {
        this.buildPoints = buildPoints;
    }
}
