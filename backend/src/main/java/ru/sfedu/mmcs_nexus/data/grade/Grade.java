package ru.sfedu.mmcs_nexus.data.grade;

import jakarta.persistence.*;
import lombok.Data;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.user.User;

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

    private int presPoints;

    private int buildPoints;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getJury() {
        return jury;
    }

    public void setJury(User jury) {
        this.jury = jury;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getPresPoints() {
        return presPoints;
    }

    public void setPresPoints(int presPoints) {
        this.presPoints = presPoints;
    }

    public int getBuildPoints() {
        return buildPoints;
    }

    public void setBuildPoints(int buildPoints) {
        this.buildPoints = buildPoints;
    }

    public void setId(GradeKey gradeKey) {
        this.id = gradeKey;
    }

    public GradeKey getId() {
        return this.id;
    }
}
