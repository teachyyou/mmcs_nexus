package ru.sfedu.mmcs_nexus.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;

@Setter
@Getter
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
    private Integer defDay;

    public ProjectEvent() {
    }

    public ProjectEvent(ProjectEventKey id, Event event, Project project) {
        this.id = id;
        this.event = event;
        this.project = project;
    }

    public ProjectEvent(ProjectEventKey id, Event event, Project project, Integer day) {
        this.id = id;
        this.event = event;
        this.project = project;
        this.defDay = day;
    }

}
