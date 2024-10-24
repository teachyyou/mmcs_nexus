package ru.sfedu.mmcs_nexus.data.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public List<Event> getEvents(String sort, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return eventRepository.findAll(Sort.by(direction, sort));
    }

    public Optional<Event> findById(UUID id) {
        return eventRepository.findById(id);
    }

    public void saveEvent(Event event) {
        eventRepository.saveAndFlush(event);
    }

    public boolean existsById(UUID id) {
        return eventRepository.existsById(id);
    }

    public void deleteEventById(UUID id) {
        eventRepository.deleteById(id);
    }
}
