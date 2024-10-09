package ru.sfedu.mmcs_nexus.data.dto;

import java.util.List;
import java.util.UUID;

public class    ProjectEventJuryAssignmentRequest {

    private UUID projectId;
    private UUID eventId;
    private List<UUID> willingJuries;
    private List<UUID> obligedJuries;
    private List<UUID> mentors;
    private boolean applyToAllEvents;

    // Getters and Setters

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public List<UUID> getWillingJuries() {
        return willingJuries;
    }

    public void setWillingJuries(List<UUID> willingJuries) {
        this.willingJuries = willingJuries;
    }

    public List<UUID> getObligedJuries() {
        return obligedJuries;
    }

    public void setObligedJuries(List<UUID> obligedJuries) {
        this.obligedJuries = obligedJuries;
    }

    public List<UUID> getMentors() {
        return mentors;
    }

    public void setMentors(List<UUID> mentors) {
        this.mentors = mentors;
    }

    public boolean isApplyToAllEvents() {
        return applyToAllEvents;
    }

    public void setApplyToAllEvents(boolean applyToAllEvents) {
        this.applyToAllEvents = applyToAllEvents;
    }
}
