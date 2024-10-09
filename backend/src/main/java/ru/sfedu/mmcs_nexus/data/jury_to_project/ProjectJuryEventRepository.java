package ru.sfedu.mmcs_nexus.data.jury_to_project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.data.user.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectJuryEventRepository extends JpaRepository<ProjectJuryEvent, ProjectJuryEventKey> {

    List<ProjectJuryEvent> findByJury(User user);

    List<ProjectJuryEvent> findByProjectIdAndEventId(UUID projectId, UUID eventId);
}
