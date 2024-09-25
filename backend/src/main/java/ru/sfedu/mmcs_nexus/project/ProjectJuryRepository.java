package ru.sfedu.mmcs_nexus.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectJuryRepository extends JpaRepository<ProjectJury, ProjectJuryKey> {
}
