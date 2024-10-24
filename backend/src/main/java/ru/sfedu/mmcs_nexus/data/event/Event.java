package ru.sfedu.mmcs_nexus.data.event;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "events")
@Data
public class Event {
    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;
    private String name;

    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private int year;

    public Event() {

    }
    public Event(String name, EventType type, int year) {
        this.name = name;
        this.eventType = type;
        this.year = year;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void editExistingEvent(Event event) {
        setName(event.getName());
        setEventType(event.getEventType());
        setYear(event.getYear());
    }
}
