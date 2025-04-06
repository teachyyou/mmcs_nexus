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

    @Query("SELECT pje.project FROM ProjectJuryEvent pje WHERE pje.event.id = :eventId AND pje.jury.id = :juryId")
    List<Project> findDistinctProjectByEventAssignedToJury(@Param("eventId") UUID eventId, @Param("juryId") UUID juryId);

    @Query("SELECT pje.project FROM ProjectJuryEvent pje WHERE pje.event.id = :eventId AND pje.jury.id = :juryId AND relationType = 'MENTOR'")
    List<Project> findDistinctProjectByEventMentoredByJury(@Param("eventId") UUID eventId, @Param("juryId") UUID juryId);

    List<ProjectJuryEvent> findByProjectIdAndEventId(UUID projectId, UUID eventId);

    Optional<ProjectJuryEvent> findByProjectIdAndEventIdAndJuryId(UUID projectId, UUID eventId, UUID juryId);

    @Query("SELECT pje.jury FROM ProjectJuryEvent pje WHERE pje.event.id = :eventId")
    List<User> findJuriesByEventId(UUID eventId);

    @Modifying
    @Query("DELETE FROM ProjectJuryEvent pje WHERE pje.event.id = :eventId AND pje.project.id = :projectId")
    void deleteByProjectAndEvent(@Param("projectId") UUID projectId, @Param("eventId") UUID eventId);
}
