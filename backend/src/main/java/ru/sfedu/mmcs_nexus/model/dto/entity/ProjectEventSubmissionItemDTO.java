package ru.sfedu.mmcs_nexus.model.dto.entity;

import lombok.Getter;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEventSubmission;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.SubmissionAvailabilityStatus;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class ProjectEventSubmissionItemDTO {

    private final UUID eventId;
    private final String eventName;
    private final EventType eventType;
    private final Integer year;
    private final LocalDate submissionStartDate;
    private final LocalDate submissionDeadlineDate;
    private final SubmissionAvailabilityStatus submissionStatus;
    private final boolean editable;
    private final String message;
    private final ProjectEventSubmissionDTO submission;

    public ProjectEventSubmissionItemDTO(
            Event event,
            SubmissionAvailabilityStatus submissionStatus,
            boolean editable,
            String message,
            ProjectEventSubmission submission
    ) {
        this.eventId = event.getId();
        this.eventName = event.getName();
        this.eventType = event.getEventType();
        this.year = event.getYear();
        this.submissionStartDate = event.getSubmissionStartDate();
        this.submissionDeadlineDate = event.getSubmissionDeadlineDate();
        this.submissionStatus = submissionStatus;
        this.editable = editable;
        this.message = message;
        this.submission = submission == null ? null : new ProjectEventSubmissionDTO(submission);
    }
}