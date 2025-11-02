package ru.sfedu.mmcs_nexus.model.payload.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class LinkProjectsToEventRequestPayload {

    @NotNull(message = "projectIds cannot be null")
    private List<UUID> projectIds;

    private boolean linkAllProjects;

}
