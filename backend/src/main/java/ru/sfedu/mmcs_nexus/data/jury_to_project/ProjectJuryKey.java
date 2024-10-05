package ru.sfedu.mmcs_nexus.data.jury_to_project;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ProjectJuryKey implements Serializable {

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "jury_id")
    private Long juryId;

    // Конструкторы
    public ProjectJuryKey() {
    }

    public ProjectJuryKey(Long projectId, Long juryId) {
        this.projectId = projectId;
        this.juryId = juryId;
    }

    // Геттеры и сеттеры

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getJuryId() {
        return juryId;
    }

    public void setJuryId(Long juryId) {
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
