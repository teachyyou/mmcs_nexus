package ru.sfedu.mmcs_nexus.model.payload.admin;

import java.util.List;
import java.util.UUID;

public class LinkProjectsToEventRequestPayload {
    private List<UUID> projectIds;
    private boolean linkAllProjects;

    public List<UUID> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<UUID> projectIds) {
        this.projectIds = projectIds;
    }

    public boolean isLinkAllProjects() {
        return linkAllProjects;
    }

    public void setLinkAllProjects(boolean linkAllProjects) {
        this.linkAllProjects = linkAllProjects;
    }
}
