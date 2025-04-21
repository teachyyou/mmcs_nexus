package ru.sfedu.mmcs_nexus.model.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class EventProjectDayRequestDTO {

    @NotNull(message = "firstDayProjects cannot be null")
    private List<UUID> firstDayProjects;

    @NotNull(message = "secondDayProjects cannot be null")
    private List<UUID> secondDayProjects;


    public EventProjectDayRequestDTO(List<UUID> firstDayProjects, List<UUID> secondDayProjects) {
        this.firstDayProjects = firstDayProjects;
        this.secondDayProjects = secondDayProjects;
    }

    public List<UUID> getFirstDayProjects() {
        return firstDayProjects;
    }

    public void setFirstDayProjects(List<UUID> firstDayProjects) {
        this.firstDayProjects = firstDayProjects;
    }

    public List<UUID> getSecondDayProjects() {
        return secondDayProjects;
    }

    public void setSecondDayProjects(List<UUID> secondDayProjects) {
        this.secondDayProjects = secondDayProjects;
    }
}
