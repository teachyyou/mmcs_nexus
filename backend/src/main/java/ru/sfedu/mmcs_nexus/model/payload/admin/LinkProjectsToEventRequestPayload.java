package ru.sfedu.mmcs_nexus.model.payload.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class LinkProjectsToEventRequestPayload {
    private List<UUID> projectIds;
    private boolean linkAllProjects;

}
