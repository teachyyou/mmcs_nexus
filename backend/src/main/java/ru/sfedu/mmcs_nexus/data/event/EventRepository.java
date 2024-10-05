package ru.sfedu.mmcs_nexus.data.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long>  {

}
