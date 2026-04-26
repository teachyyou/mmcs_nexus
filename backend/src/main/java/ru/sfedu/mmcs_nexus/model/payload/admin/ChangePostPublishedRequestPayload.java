package ru.sfedu.mmcs_nexus.model.payload.admin;

import jakarta.validation.constraints.NotNull;

public class ChangePostPublishedRequestPayload {

    @NotNull
    private Boolean published;

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }
}
