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

    // Оригинальные методы
    @Query("SELECT g FROM Grade g WHERE g.project.year = :year")
    List<Grade> findByYear(@Param("year") int year);

    @Query("SELECT g FROM Grade g WHERE g.jury.id = :juryId")
    List<Grade> findByJury(@Param("juryId") UUID juryId);

    @Query("SELECT g FROM Grade g WHERE g.event.id = :eventId")
    List<Grade> findByEvent(@Param("eventId") UUID eventId);

    @Query("SELECT g FROM Grade g WHERE g.project.id = :projectId")
    List<Grade> findByProject(@Param("projectId") UUID projectId);

    @Query("SELECT g FROM Grade g WHERE g.jury.id = :juryId AND g.event.id = :eventId")
    List<Grade> findByJuryAndEvent(@Param("juryId") UUID juryId, @Param("eventId") UUID eventId);

    @Query("SELECT g FROM Grade g WHERE g.jury.id = :juryId AND g.project.id = :projectId")
    List<Grade> findByJuryAndProject(@Param("juryId") UUID juryId, @Param("projectId") UUID projectId);

    @Query("SELECT g FROM Grade g WHERE g.event.id = :eventId AND g.project.id = :projectId")
    List<Grade> findByEventAndProject(@Param("eventId") UUID eventId, @Param("projectId") UUID projectId);

    // Методы с фильтрацией по году
    @Query("SELECT g FROM Grade g WHERE g.jury.id = :juryId AND g.project.year = :year")
    List<Grade> findByJuryForYear(@Param("juryId") UUID juryId, @Param("year") int year);

    @Query("SELECT g FROM Grade g WHERE g.event.id = :eventId AND g.project.year = :year")
    List<Grade> findByEventForYear(@Param("eventId") UUID eventId, @Param("year") int year);

    @Query("SELECT g FROM Grade g WHERE g.project.id = :projectId AND g.project.year = :year")
    List<Grade> findByProjectForYear(@Param("projectId") UUID projectId, @Param("year") int year);

    @Query("SELECT g FROM Grade g WHERE g.jury.id = :juryId AND g.event.id = :eventId AND g.project.year = :year")
    List<Grade> findByJuryAndEventForYear(@Param("juryId") UUID juryId, @Param("eventId") UUID eventId, @Param("year") int year);

    @Query("SELECT g FROM Grade g WHERE g.jury.id = :juryId AND g.project.id = :projectId AND g.project.year = :year")
    List<Grade> findByJuryAndProjectForYear(@Param("juryId") UUID juryId, @Param("projectId") UUID projectId, @Param("year") int year);

    @Query("SELECT g FROM Grade g WHERE g.event.id = :eventId AND g.project.id = :projectId AND g.project.year = :year")
    List<Grade> findByEventAndProjectForYear(@Param("eventId") UUID eventId, @Param("projectId") UUID projectId, @Param("year") int year);
}
