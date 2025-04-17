package ru.sfedu.mmcs_nexus.controller.v1.publiсapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.service.EventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PublicEventController {
    private final EventService eventService;

    @Autowired
    public PublicEventController(EventService eventService) {
        this.eventService = eventService;

    }

    @GetMapping(value = "/api/v1/public/events", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventsByYear(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "2024") int year)
    {

        List<Event> events = eventService.getEventsByYear(sort, order, year);

        Map<String, Object> response = new HashMap<>();
        response.put("content", events);
        response.put("totalElements", events.size());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "/api/v1/public/events/years", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventsYears(
            @RequestParam(defaultValue = "year") String sort,
            @RequestParam(defaultValue = "asc") String order)
    {

        List<Integer> years = eventService.getEventsYears(sort, order);

        Map<String, Object> response = new HashMap<>();
        response.put("content", years);
        response.put("totalElements", years.size());

        return ResponseEntity.ok().body(response);
    }
}
