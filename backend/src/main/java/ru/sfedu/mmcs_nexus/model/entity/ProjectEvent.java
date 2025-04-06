package ru.sfedu.mmcs_nexus.model.entity;

import jakarta.persistence.*;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;

@Entity
public class ProjectEvent {

    @EmbeddedId
    private ProjectEventKey id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    public ProjectEvent() {
    }

    public ProjectEvent(ProjectEventKey id, Event event, Project project) {
        this.id = id;
        this.event = event;
        this.project = project;
    }

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

    public ProjectEventKey getId() {
        return id;
    }

    public void setId(ProjectEventKey id) {
        this.id = id;
    }
}
