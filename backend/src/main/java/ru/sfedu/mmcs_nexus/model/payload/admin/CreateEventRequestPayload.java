package ru.sfedu.mmcs_nexus.model.payload.admin;

import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.validators.EventName;
import ru.sfedu.mmcs_nexus.validators.EventYear;
import ru.sfedu.mmcs_nexus.validators.MaxPoints;

public class CreateEventRequestPayload {
    @EventName
    private String name;
    private EventType eventType;
    @EventYear
    private Integer year;

    @MaxPoints
    private Integer maxPresPoints;

    @MaxPoints
    private Integer maxBuildPoints;

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
