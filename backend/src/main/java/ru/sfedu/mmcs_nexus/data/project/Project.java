package ru.sfedu.mmcs_nexus.data.project;


import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
public class Project {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    private String name;
    private String description;
    private String type;
    private int year;

    public Project() {}

    public Project(String name, String description, String type, int year) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.year = year;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void editExistingProject(Project project) {
        setName(project.getName());
        setDescription(project.getDescription());
        setType(project.getType());
        setYear(project.getYear());
    }



}
