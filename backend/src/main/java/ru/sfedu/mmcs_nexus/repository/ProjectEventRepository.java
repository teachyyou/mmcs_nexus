package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
  SELECT p
  FROM ProjectEvent pe
  JOIN pe.project p
  WHERE pe.event.id = :eventId
    AND (:day IS NULL OR pe.defDay = :day)
    ORDER BY p.name ASC
""")
    Page<Project> findProjectsByEventId(@Param("eventId") UUID eventId, @Param("day") Integer day, Pageable pageable);

    @Query("""
  SELECT p
  FROM ProjectEvent pe
  JOIN pe.project p
  WHERE pe.event.id = :eventId
    AND (:day IS NULL OR pe.defDay = :day)
    ORDER BY p.name ASC
""")
    List<Project> findProjectsByEventId(@Param("eventId") UUID eventId, @Param("day") Integer day);

    @Query("SELECT pe FROM ProjectEvent pe WHERE pe.event.id = :eventId")
    List<ProjectEvent> findByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT pe.event FROM ProjectEvent pe WHERE pe.project.id = :projectId")
    List<Event> findEventsByProjectId(@Param("projectId") UUID projectId);

    @Modifying
    @Query("DELETE FROM ProjectEvent pe WHERE pe.event.id = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);


}
