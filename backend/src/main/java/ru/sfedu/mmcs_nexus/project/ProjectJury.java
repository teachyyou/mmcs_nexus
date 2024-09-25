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
    private ProjectJuryKey id;

    @ManyToOne
    @MapsId("juryId")
    @JoinColumn(name = "jury_id")
    private User juries;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project projects;

    @Enumerated(EnumType.STRING)
    private RelationType relationType;

    // Конструкторы

    public ProjectJury() {
    }

    public ProjectJury(ProjectJuryKey id, User juries, Project projects, RelationType relationType) {
        this.id = id;
        this.juries = juries;
        this.projects = projects;
        this.relationType = relationType;
    }

    // Геттеры и сеттеры

    public ProjectJuryKey getId() {
        return id;
    }

    public void setId(ProjectJuryKey id) {
        this.id = id;
    }

    public User getJuries() {
        return juries;
    }

    public void setJuries(User juries) {
        this.juries = juries;
    }

    public Project getProjects() {
        return projects;
    }

    public void setProjects(Project projects) {
        this.projects = projects;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }
}
