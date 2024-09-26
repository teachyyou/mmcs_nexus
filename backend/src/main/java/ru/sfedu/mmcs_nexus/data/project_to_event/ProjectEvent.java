package ru.sfedu.mmcs_nexus.data.project_to_event;

import jakarta.persistence.*;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.project.Project;

@Entity
public class ProjectEvent {

    @EmbeddedId
    private ProjectEventKey id;

    public ProjectEvent() {
    }

    public ProjectEvent(ProjectEventKey id, Event events, Project projects) {
        this.id = id;
        this.events = events;
        this.projects = projects;
    }

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event events;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project projects;

    public Event getEvents() {
        return events;
    }

    public void setEvents(Event events) {
        this.events = events;
    }

    public Project getProjects() {
        return projects;
    }

    public void setProjects(Project projects) {
        this.projects = projects;
    }

    public ProjectEventKey getId() {
        return id;
    }

    public void setId(ProjectEventKey id) {
        this.id = id;
    }
}
