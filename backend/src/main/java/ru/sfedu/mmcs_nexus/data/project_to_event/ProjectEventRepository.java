package ru.sfedu.mmcs_nexus.data.project_to_event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectEventRepository extends JpaRepository<ProjectEvent, ProjectEventKey> {
}
