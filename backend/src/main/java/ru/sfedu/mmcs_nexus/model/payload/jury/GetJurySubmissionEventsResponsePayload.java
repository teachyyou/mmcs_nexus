package ru.sfedu.mmcs_nexus.model.payload.jury;

import lombok.Getter;
import ru.sfedu.mmcs_nexus.model.dto.entity.JurySubmissionEventDTO;

import java.util.List;
import java.util.UUID;

@Getter
public class GetJurySubmissionEventsResponsePayload {

    private final UUID defaultEventId;
    private final List<JurySubmissionEventDTO> events;

    public GetJurySubmissionEventsResponsePayload(UUID defaultEventId, List<JurySubmissionEventDTO> events) {
        this.defaultEventId = defaultEventId;
        this.events = events;
    }
}