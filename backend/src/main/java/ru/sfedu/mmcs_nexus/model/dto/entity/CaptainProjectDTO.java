package ru.sfedu.mmcs_nexus.model.dto.entity;

import lombok.Getter;
import ru.sfedu.mmcs_nexus.model.entity.Project;

import java.util.UUID;

@Getter
public class CaptainProjectDTO {

    private final UUID id;
    private final String name;

    public CaptainProjectDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
    }
}