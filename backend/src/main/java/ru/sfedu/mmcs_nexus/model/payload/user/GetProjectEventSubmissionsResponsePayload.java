package ru.sfedu.mmcs_nexus.model.payload.user;

import lombok.Getter;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectEventSubmissionItemDTO;

import java.util.List;
import java.util.UUID;

@Getter
public class GetProjectEventSubmissionsResponsePayload {

    private final UUID defaultEventId;
    private final List<ProjectEventSubmissionItemDTO> items;

    public GetProjectEventSubmissionsResponsePayload(UUID defaultEventId, List<ProjectEventSubmissionItemDTO> items) {
        this.defaultEventId = defaultEventId;
        this.items = items;
    }
}