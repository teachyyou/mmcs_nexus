package ru.sfedu.mmcs_nexus.project;

import jakarta.persistence.*;
import ru.sfedu.mmcs_nexus.user.User;


@Entity
public class ProjectJury {

    public enum RelationType {
        MENTOR,
        WILLING,
        OBLIGED
    }

    @EmbeddedId
    ProjectJuryKey id;

    @ManyToOne
    @MapsId("juryId")
    @JoinColumn(name="jury_id")
    User jury;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name="project_id")
    Project project;

    @Enumerated(EnumType.STRING)
    RelationType relationType;


}
