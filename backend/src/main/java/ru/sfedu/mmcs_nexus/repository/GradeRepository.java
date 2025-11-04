package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.model.entity.Grade;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, GradeKey> {

    @Query("SELECT g FROM Grade g WHERE g.event.id = :eventId AND g.project.id = :projectId")
    List<Grade> findByEventAndProject(@Param("eventId") UUID eventId, @Param("projectId") UUID projectId);

}
