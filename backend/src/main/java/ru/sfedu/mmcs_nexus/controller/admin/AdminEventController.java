package ru.sfedu.mmcs_nexus.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.event.EventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class AdminEventController {

    private final EventService eventService;

    @Autowired
    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping(value = "/api/v1/admin/events", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventsList(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Authentication authentication) {

        List<Event> events = eventService.getEvents(sort, order);

        Map<String, Object> response = new HashMap<>();
        response.put("content", events);
        response.put("totalElements", events.size());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/api/v1/admin/events/{id}", produces = "application/json")
    public ResponseEntity<Event> getEventById(@PathVariable("id") UUID id, Authentication authentication) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{id} not found"));
        return ResponseEntity.ok(event);
    }

    @PutMapping(value = "/api/v1/admin/events/{id}", produces = "application/json")
    public ResponseEntity<Event> editEventById(@PathVariable("id") UUID id, Authentication authentication, @RequestBody Event event) {

        Event existingEvent = eventService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{id} not found"));

        existingEvent.editExistingEvent(event);
        eventService.saveEvent(existingEvent);

        return ResponseEntity.ok(existingEvent);
    }

    @PostMapping(value = "/api/v1/admin/events", produces = "application/json")
    public ResponseEntity<Event> createEvent(Authentication authentication, @RequestBody Event event) {

        eventService.saveEvent(event);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping(value = "/api/v1/admin/events/{id}")
    public ResponseEntity<Void> deleteEventById(@PathVariable("id") UUID id) {
        if (!eventService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        eventService.deleteEventById(id);

        return ResponseEntity.noContent().build();
    }
}
