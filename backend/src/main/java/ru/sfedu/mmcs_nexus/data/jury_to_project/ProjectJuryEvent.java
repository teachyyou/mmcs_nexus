package ru.sfedu.mmcs_nexus.data.jury_to_project;

import jakarta.persistence.*;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.user.User;

@Entity
public class ProjectJuryEvent {

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public enum RelationType {
        MENTOR,
        WILLING,
        OBLIGED
    }

    @EmbeddedId
    private ProjectJuryEventKey id;

    @ManyToOne
    @MapsId("juryId")
    @JoinColumn(name = "jury_id")
    private User jury;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    private RelationType relationType;

    // Конструкторы

    public ProjectJuryEvent() {
    }

    public ProjectJuryEvent(ProjectJuryEventKey id, User jury, Project project, Event event, RelationType relationType) {
        this.id = id;
        this.jury = jury;
        this.project = project;
        this.event = event;
        this.relationType = relationType;
    }

    // Геттеры и сеттеры

    public ProjectJuryEventKey getId() {
        return id;
    }

    public void setId(ProjectJuryEventKey id) {
        this.id = id;
    }

    public User getJury() {
        return jury;
    }

    public void setJury(User jury) {
        this.jury = jury;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }
}
