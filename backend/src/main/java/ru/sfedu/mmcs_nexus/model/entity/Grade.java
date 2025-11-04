package ru.sfedu.mmcs_nexus.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;

@Entity
@Table(name = "grades")
@Data
public class Grade {

    @EmbeddedId
    private GradeKey id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @MapsId("juryId")
    @JoinColumn(name = "jury_id")
    private User jury;

    private String comment;

    private Integer presPoints;

    private Integer buildPoints;

    public Grade() {}

    public Grade(Grade grade) {
        this.id = grade.getId();
        this.project = grade.getProject();
        this.event = grade.getEvent();
        this.jury = grade.getJury();
        this.comment = grade.getComment();
        this.presPoints = grade.getPresPoints();
        this.buildPoints = grade.getBuildPoints();
    }

}
