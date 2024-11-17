package ru.sfedu.mmcs_nexus.data.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>  {

    @Query("SELECT DISTINCT e.year FROM Event e ORDER BY e.year")
    List<Integer> findAllEventsYears();
}
