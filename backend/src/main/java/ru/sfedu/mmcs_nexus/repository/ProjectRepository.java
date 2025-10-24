package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.model.entity.Project;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByName(String name);

    Page<Project> findAllByYear(Integer year, Pageable pageable);
    List<Project> findAllByYear(Integer year);


    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
}

