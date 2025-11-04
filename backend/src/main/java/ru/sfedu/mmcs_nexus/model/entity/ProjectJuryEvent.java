package ru.sfedu.mmcs_nexus.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectJuryEventKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.JuryRelationType;

@Setter
@Getter
@Entity
public class ProjectJuryEvent {

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
    private JuryRelationType relationType;

    public ProjectJuryEvent() {
    }

    public ProjectJuryEvent(ProjectJuryEventKey id, User jury, Project project, Event event, JuryRelationType relationType) {
        this.id = id;
        this.jury = jury;
        this.project = project;
        this.event = event;
        this.relationType = relationType;
    }


}
