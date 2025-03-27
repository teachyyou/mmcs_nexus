package ru.sfedu.mmcs_nexus.data.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import ru.sfedu.mmcs_nexus.data.grade.Grade;

import java.util.UUID;

public class GradeDTO {

    @NotNull(message="project UUID is required")
//    @ru.sfedu.mmcs_nexus.valigators.UUID(message = "projectId: Incorrect UUID format")
    private UUID projectId;

//    @ru.sfedu.mmcs_nexus.valigators.UUID(message = "eventId: Incorrect UUID format")
    @NotNull(message="event UUID is required")
    private UUID eventId;

    private UUID juryId;

    @Length(max=1024, message = "comment: length limit is 1024")
    private String comment;

    @Min(value = 0, message = "Presentation points cannot be negative")
    @Max(value = 99, message = "Presentation points cannot be greater than 99")
    private Integer presPoints;

    @Min(value = 0, message = "Build points cannot be negative")
    @Max(value = 99, message = "Build points cannot be greater than 99")
    private Integer buildPoints;

    public GradeDTO() {

    }

    public GradeDTO(Grade grade) {
        this.projectId = grade.getId().getProjectId();
        this.eventId = grade.getId().getEventId();
        this.juryId = grade.getId().getJuryId();
        this.comment = grade.getComment();
        this.presPoints = grade.getPresPoints();
        this.buildPoints = grade.getBuildPoints();
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getJuryId() {
        return juryId;
    }

    public void setJuryId(UUID juryId) {
        this.juryId = juryId;
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
