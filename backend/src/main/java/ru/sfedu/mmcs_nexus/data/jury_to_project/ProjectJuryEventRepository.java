package ru.sfedu.mmcs_nexus.data.jury_to_project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.data.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectJuryEventRepository extends JpaRepository<ProjectJuryEvent, ProjectJuryEventKey> {

    List<ProjectJuryEvent> findByJury(User user);

    List<ProjectJuryEvent> findByProjectIdAndEventId(UUID projectId, UUID eventId);

    Optional<ProjectJuryEvent> findByProjectIdAndEventIdAndJuryId(UUID projectId, UUID eventId, UUID juryId);

    @Query("SELECT pje.jury FROM ProjectJuryEvent pje WHERE pje.event.id = :eventId")
    List<User> findJuriesByEventId(UUID eventId);

    @Modifying
    @Query("DELETE FROM ProjectJuryEvent pje WHERE pje.event.id = :eventId AND pje.project.id = :projectId")
    void deleteByProjectAndEvent(@Param("projectId") UUID projectId, @Param("eventId") UUID eventId);
}
