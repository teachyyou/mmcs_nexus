package ru.sfedu.mmcs_nexus.data.project_to_event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.project.Project;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectEventRepository extends JpaRepository<ProjectEvent, ProjectEventKey> {

    @Query("SELECT pe.projects FROM ProjectEvent pe WHERE pe.events.id = :eventId")
    List<Project> findByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT pe.events FROM ProjectEvent pe WHERE pe.projects.id = :projectId")
    List<Event> findByProjectId(@Param("projectId") UUID projectId);

}
