package ru.sfedu.mmcs_nexus.model.payload.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UUID;

import java.util.List;


@Setter
@Getter
public class AssignJuriesRequestPayload {

    @UUID
    @NotNull
    private String projectId;

    @UUID
    @NotNull
    private String eventId;

    @NotNull(message = "willingJuries cannot be null")
    private List<String> willingJuries;
    @NotNull(message = "obligedJuries cannot be null")
    private List<String> obligedJuries;
    @NotNull(message = "mentors cannot be null")
    private List<String> mentors;

    private boolean applyToAllEvents;

}
