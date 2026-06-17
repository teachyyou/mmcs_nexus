package ru.sfedu.mmcs_nexus.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "events")
@Data
public class Event {
    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Integer year;
    private Integer maxPresPoints;
    private Integer maxBuildPoints;

    @Column(name = "submission_start_date")
    private LocalDate submissionStartDate;

    @Column(name = "submission_deadline_date")
    private LocalDate submissionDeadlineDate;

    public Event() {}

    public Event(String name, EventType type, Integer year, Integer maxPresPoints, Integer maxBuildPoints) {
        this.name = name;
        this.eventType = type;
        this.year = year;
        this.maxPresPoints = maxPresPoints;
        this.maxBuildPoints = maxBuildPoints;
    }
}
