package ru.sfedu.mmcs_nexus.data.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.NumberFormat;

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

    @NotBlank(message = "Name is required")
    @Length(min=1, max=32, message="Name is too long")
    private String name;

    @NotNull(message = "Event type is required")
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Min(value = 2020, message = "Incorrect Year")
    @Max(value= 2030, message="Incorrect Year")
    private Integer year;

    @NotNull(message = "Max presentation points are required")
    @Min(value = 0, message = "Max presentation points cannot be negative")
    @Max(value = 99, message = "Max presentation points cannot be greater than 99")
    private Integer maxPresPoints;

    @NotNull(message = "Max build points are required")
    @Min(value = 0, message = "Max build points cannot be negative")
    @Max(value = 99, message = "Max build points cannot be greater than 99")
    private Integer maxBuildPoints;

    public Event() {

    }
    public Event(String name, EventType type, Integer year, Integer maxPresPoints, Integer maxBuildPoints) {
        this.name = name;
        this.eventType = type;
        this.year = year;
        this.maxPresPoints = maxPresPoints;
        this.maxBuildPoints = maxBuildPoints;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void editExistingEvent(Event event) {
        setName(event.getName());
        setEventType(event.getEventType());
        setYear(event.getYear());
        setMaxPresPoints(event.getMaxPresPoints());
        setMaxBuildPoints(event.getMaxBuildPoints());
    }

    public Integer getMaxPresPoints() {
        return maxPresPoints;
    }

    public void setMaxPresPoints(Integer maxPresPoints) {
        this.maxPresPoints = maxPresPoints;
    }

    public Integer getMaxBuildPoints() {
        return maxBuildPoints;
    }

    public void setMaxBuildPoints(Integer maxBuildPoints) {
        this.maxBuildPoints = maxBuildPoints;
    }
}
