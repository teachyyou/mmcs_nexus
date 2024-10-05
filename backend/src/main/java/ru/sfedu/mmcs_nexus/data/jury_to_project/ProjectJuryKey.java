package ru.sfedu.mmcs_nexus.data.jury_to_project;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class ProjectJuryKey implements Serializable {

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "jury_id")
    private UUID juryId;

    // Конструкторы
    public ProjectJuryKey() {
    }

    public ProjectJuryKey(UUID projectId, UUID juryId) {
        this.projectId = projectId;
        this.juryId = juryId;
    }

    // Геттеры и сеттеры

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getJuryId() {
        return juryId;
    }

    public void setJuryId(UUID juryId) {
        this.juryId = juryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectJuryKey that = (ProjectJuryKey) o;

        if (!projectId.equals(that.projectId)) return false;
        return juryId.equals(that.juryId);
    }

    @Override
    public int hashCode() {
        int result = projectId.hashCode();
        result = 31 * result + juryId.hashCode();
        return result;
    }
}
