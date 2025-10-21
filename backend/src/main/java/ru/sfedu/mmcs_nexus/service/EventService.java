package ru.sfedu.mmcs_nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateEventRequestPayload;
import ru.sfedu.mmcs_nexus.repository.EventRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Page<Event> getEvents(Integer year, PaginationPayload paginationPayload) {
        Pageable pageable = paginationPayload.getPageable();

        if (year != null) {
            return eventRepository.findByYear(year, pageable);
        }

        return eventRepository.findAll(pageable);
    }

    public Optional<Event> findById(String id) {
        return eventRepository.findById(UUID.fromString(id));
    }


    public void createEvent(CreateEventRequestPayload payload) {
        Event event = new Event(
                payload.getName().trim(),
                payload.getEventType(),
                payload.getYear(),
                payload.getMaxPresPoints(),
                payload.getMaxBuildPoints()
        );
        saveEvent(event);
    }

    public Event editEvent(Event event, CreateEventRequestPayload payload) {
        event.setName(payload.getName());
        event.setEventType(payload.getEventType());
        event.setYear(payload.getYear());
        event.setMaxPresPoints(payload.getMaxPresPoints());
        event.setMaxBuildPoints(payload.getMaxBuildPoints());

        saveEvent(event);

        return event;
    }

    public boolean existsById(String id) {
        return eventRepository.existsById(UUID.fromString(id));
    }

    public void deleteEventById(String id) {
        eventRepository.deleteById(UUID.fromString(id));
    }

    public List<Integer> getEventsYears(String sort, String order) {
        return eventRepository.findAllEventsYears();
    }

    private void saveEvent(Event event) {
        eventRepository.saveAndFlush(event);
    }

}
