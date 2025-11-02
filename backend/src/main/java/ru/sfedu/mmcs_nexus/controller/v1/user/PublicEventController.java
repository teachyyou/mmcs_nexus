package ru.sfedu.mmcs_nexus.controller.v1.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.service.EventService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

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
    public ResponseEntity<Map<String, Object>> getEvents(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            @RequestParam(required = false, defaultValue = ApplicationConfig.DEFAULT_EVENT_YEAR) Integer year
    )
    {

        PaginationPayload paginationPayload = new PaginationPayload(limit, offset, sort, order, EntitySort.EVENT_SORT);

        Page<Event> events = eventService.findAll(year, paginationPayload);

        return ResponseEntity.ok().body(
                ResponseUtils.buildResponse(events.getContent(), events.getTotalElements())
        );
    }

    @GetMapping(value = "/api/v1/public/events/years", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEventsYears()
    {
        List<Integer> years = eventService.getEventsYears();

        return ResponseEntity.ok().body(ResponseUtils.buildResponse(years, years.size()));
    }
}
