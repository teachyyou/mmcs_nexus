package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.model.entity.Project;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByName(String name);
    List<Project> findByYear(Integer year, Sort sort);
    List<Project> findByYear(Integer year);


    boolean existsByName(String name);
}

