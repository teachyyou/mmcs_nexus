package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateEventRequestPayload;
import ru.sfedu.mmcs_nexus.repository.EventRepository;

import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Page<Event> findAll(Integer year, PaginationPayload paginationPayload) {
        Pageable pageable = paginationPayload.getPageable();

        if (year != null) {
            return eventRepository.findByYear(year, pageable);
        }

        return eventRepository.findAll(pageable);
    }

    public Event find(String id){
        return getById(id);
    }

    public List<Integer> getEventsYears() {
        return eventRepository.findAllEventsYears();
    }

    @Transactional
    public void create(CreateEventRequestPayload payload) {
        Event event = new Event(
                payload.getName().trim(),
                payload.getEventType(),
                payload.getYear(),
                payload.getMaxPresPoints(),
                payload.getMaxBuildPoints()
        );
        eventRepository.save(event);
    }

    @Transactional
    public Event edit(String eventId, CreateEventRequestPayload payload) {
        Event event = getById(eventId);

        event.setName(payload.getName());
        event.setEventType(payload.getEventType());
        event.setYear(payload.getYear());
        event.setMaxPresPoints(payload.getMaxPresPoints());
        event.setMaxBuildPoints(payload.getMaxBuildPoints());

        return event;
    }

    public boolean existsById(String eventId) {
        return eventRepository.existsById(UUID.fromString(eventId));
    }

    @Transactional
    public void deleteEventById(String eventId) {

        Event event = getById(eventId);
        eventRepository.delete(event);
    }

    private Event getById(String id) throws EntityNotFoundException {
        return eventRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{id} not found"));
    }
}
