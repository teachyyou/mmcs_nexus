package ru.sfedu.mmcs_nexus.model.dto.entity;

import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.entity.Grade;

import java.util.UUID;

@Setter
@Getter
public class GradeDTO {
    private UUID projectId;
    private UUID eventId;
    private UUID juryId;

    private String comment;
    private Integer presPoints;
    private Integer buildPoints;

    public GradeDTO(Grade grade) {
        this.projectId = grade.getProject().getId();
        this.eventId = grade.getEvent().getId();
        this.juryId = grade.getJury().getId();
        this.comment = grade.getComment();
        this.presPoints = grade.getPresPoints();
        this.buildPoints = grade.getBuildPoints();
    }

}
