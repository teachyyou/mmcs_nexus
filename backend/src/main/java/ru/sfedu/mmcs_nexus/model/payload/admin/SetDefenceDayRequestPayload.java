package ru.sfedu.mmcs_nexus.model.payload.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class SetDefenceDayRequestPayload {

    @NotNull(message = "firstDayProjects cannot be null")
    private List<UUID> firstDayProjects;

    @NotNull(message = "secondDayProjects cannot be null")
    private List<UUID> secondDayProjects;


    public SetDefenceDayRequestPayload(List<UUID> firstDayProjects, List<UUID> secondDayProjects) {
        this.firstDayProjects = firstDayProjects;
        this.secondDayProjects = secondDayProjects;
    }

}
