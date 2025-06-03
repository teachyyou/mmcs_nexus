package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEvent;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectEventRepository extends JpaRepository<ProjectEvent, ProjectEventKey> {

    @Query("SELECT pe.project FROM ProjectEvent pe WHERE pe.event.id = :eventId " +
            "AND (:day IS NULL OR pe.defDay = :day)")
    List<Project> findProjectsByEventId(@Param("eventId") UUID eventId, @Param("day") Integer day);

    @Query("SELECT pe FROM ProjectEvent pe WHERE pe.event.id = :eventId")
    List<ProjectEvent> findByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT pe.project FROM ProjectEvent pe WHERE pe.event.id = :eventId AND pe.defDay = :day")
    List<Project> findByEventIdForDay(@Param("eventId") UUID eventId, @Param("day") Integer day);

    @Query("SELECT pe.event FROM ProjectEvent pe WHERE pe.project.id = :projectId")
    List<Event> findEventsByProjectId(@Param("projectId") UUID projectId);

    @Modifying
    @Query("DELETE FROM ProjectEvent pe WHERE pe.event.id = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);


}
