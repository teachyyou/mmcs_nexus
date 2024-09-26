package ru.sfedu.mmcs_nexus.data.project_to_event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectEventRepository extends JpaRepository<ProjectEvent, ProjectEventKey> {
}
