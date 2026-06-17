package ru.sfedu.mmcs_nexus.model.dto.entity;

import lombok.Getter;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.SubmissionAvailabilityStatus;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class JurySubmissionEventDTO {

    private final UUID id;
    private final String name;
    private final EventType eventType;
    private final Integer year;
    private final LocalDate submissionStartDate;
    private final LocalDate submissionDeadlineDate;
    private final SubmissionAvailabilityStatus submissionStatus;

    public JurySubmissionEventDTO(Event event, SubmissionAvailabilityStatus submissionStatus) {
        this.id = event.getId();
        this.name = event.getName();
        this.eventType = event.getEventType();
        this.year = event.getYear();
        this.submissionStartDate = event.getSubmissionStartDate();
        this.submissionDeadlineDate = event.getSubmissionDeadlineDate();
        this.submissionStatus = submissionStatus;
    }
}