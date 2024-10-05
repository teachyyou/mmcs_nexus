package ru.sfedu.mmcs_nexus.data.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByName(String name);

    boolean existsByName(String name);
}

