package ru.sfedu.mmcs_nexus.model.payload.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePostPublishedRequestPayload {

    @NotNull
    private Boolean published;

}
