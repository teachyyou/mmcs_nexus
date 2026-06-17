package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEventSubmission;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventSubmissionKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectEventSubmissionRepository extends JpaRepository<ProjectEventSubmission, ProjectEventSubmissionKey> {

    Optional<ProjectEventSubmission> findByProjectIdAndEventId(UUID projectId, UUID eventId);

    List<ProjectEventSubmission> findAllByEventId(UUID eventId);

    List<ProjectEventSubmission> findAllByProjectId(UUID projectId);
}