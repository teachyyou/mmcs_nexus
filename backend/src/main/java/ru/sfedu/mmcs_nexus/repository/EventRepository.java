package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.model.entity.Event;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>  {

    @Query("SELECT DISTINCT e.year FROM Event e ORDER BY e.year")
    List<Integer> findAllEventsYears();

    Page<Event> findByYear(Integer year, Pageable pageable);

}
