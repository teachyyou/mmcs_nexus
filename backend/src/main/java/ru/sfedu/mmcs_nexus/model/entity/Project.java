package ru.sfedu.mmcs_nexus.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    private Integer externalId;
    private Integer quantityOfStudents;
    private String captainName;
    private boolean isFull;
    private String track;
    private String technologies;

    private String name;
    private String description;
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_uuid")
    private User captain;

    private int year;

    public Project() {}

    public Project(String name, String description, String type, int year) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.year = year;
    }

    public Project(
            Integer externalId,
            Integer quantityOfStudents,
            String captainName,
            boolean isFull,
            String track,
            String technologies,
            String name,
            String description,
            String type,
            int year
    ) {
        this(name, description, type, year);
        this.externalId = externalId;
        this.quantityOfStudents = quantityOfStudents;
        this.captainName = captainName;
        this.isFull = isFull;
        this.track = track;
        this.technologies = technologies;
    }

}
