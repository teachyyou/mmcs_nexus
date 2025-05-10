package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectJuryEventKey;
import ru.sfedu.mmcs_nexus.model.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectJuryEventRepository extends JpaRepository<ProjectJuryEvent, ProjectJuryEventKey> {

    List<ProjectJuryEvent> findByJury(User user);

    //Поиск всех проектов, для которых есть связь с жюри
    @Query("SELECT DISTINCT pje.project FROM ProjectJuryEvent pje WHERE pje.event.id = :eventId")
    List<Project> findDistinctProjectByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT pje.jury FROM ProjectJuryEvent pje WHERE pje.event.id=:eventId AND pje.project.id=:projectId AND pje.relationType='MENTOR'")
    List<User> findMentorsByProjectIdAndEventId(@Param("eventId") UUID eventId, @Param("projectId") UUID projectId);

    //Достаем только те проекты, что связаны с данным жюри и проверяются в указанный день
    @Query("SELECT pje.project FROM ProjectJuryEvent pje " +
            "JOIN ProjectEvent pe ON pje.project.id = pe.project.id AND pje.event.id = pe.event.id " +
            "WHERE pje.event.id = :eventId " +
            "AND pje.jury.id = :juryId " +
            "AND (:day IS NULL OR pe.defDay = :day)")
    List<Project> findProjectByEventAssignedToJury(
            @Param("eventId") UUID eventId,
            @Param("juryId") UUID juryId,
            @Param("day") Integer day);

    //Достаем только те проекты, что связаны менторством с данным жюри и проверяются в указанный день
    @Query("SELECT pje.project FROM ProjectJuryEvent pje " +
            "JOIN ProjectEvent pe ON pje.project.id = pe.project.id AND pje.event.id = pe.event.id " +
            "WHERE pje.event.id = :eventId " +
            "AND pje.jury.id = :juryId " +
            "AND pje.relationType = 'MENTOR' " +
            "AND (:day IS NULL OR pe.defDay = :day)")
    List<Project> findProjectByEventMentoredByJury(
            @Param("eventId") UUID eventId,
            @Param("juryId") UUID juryId,
            @Param("day") Integer day);

    //Достаем только тех жюри, что связаны с проектами, что связаны с данным жюри и проверяются в указанный день
    @Query("""
    SELECT DISTINCT pje.jury
    FROM ProjectJuryEvent pje
    WHERE pje.event.id = :eventId
    AND pje.project IN (
        SELECT pe.project FROM ProjectEvent pe
        JOIN ProjectJuryEvent j ON j.project = pe.project AND j.event = pe.event
        WHERE pe.event.id = :eventId
        AND j.jury.id = :juryId
        AND (:day IS NULL OR pe.defDay = :day))
    """)
    List<User> findJuriesForProjectsAssignedToJuryByEvent(
            @Param("eventId") UUID eventId,
            @Param("juryId") UUID juryId,
            @Param("day") Integer day);

    //Достаем только тех жюри, что связаны с проектами, что связаны менторством с данным жюри и проверяются в указанный день
    @Query("""
    SELECT DISTINCT pje.jury
    FROM ProjectJuryEvent pje
    WHERE pje.event.id = :eventId
    AND pje.project IN (
        SELECT pe.project
        FROM ProjectEvent pe
        JOIN ProjectJuryEvent mentorPje ON
            mentorPje.project = pe.project AND
            mentorPje.event = pe.event
        WHERE pe.event.id = :eventId
        AND mentorPje.jury.id = :juryId
        AND mentorPje.relationType = 'MENTOR'
        AND (:day IS NULL OR pe.defDay = :day)
    )
    """)
    List<User> findJuriesForProjectsMentoredByJuryByEvent(
            @Param("eventId") UUID eventId,
            @Param("juryId") UUID juryId,
            @Param("day") Integer day);


    List<ProjectJuryEvent> findByProjectIdAndEventId(UUID projectId, UUID eventId);

    Optional<ProjectJuryEvent> findByProjectIdAndEventIdAndJuryId(UUID projectId, UUID eventId, UUID juryId);


    //Достаем только тех жюри, что связаны с проектами, что проверяются в указанный день
    @Query("SELECT pje.jury FROM ProjectJuryEvent pje " +
            "JOIN ProjectEvent pe ON pje.project.id = pe.project.id AND pje.event.id = pe.event.id " +
            "WHERE pje.event.id = :eventId " +
            "AND (:day IS NULL OR pe.defDay = :day)")
    List<User> findJuriesByEventId(@Param("eventId") UUID eventId, @Param("day") Integer day);

    @Modifying
    @Query("DELETE FROM ProjectJuryEvent pje WHERE pje.event.id = :eventId AND pje.project.id = :projectId")
    void deleteByProjectAndEvent(@Param("projectId") UUID projectId, @Param("eventId") UUID eventId);
}
