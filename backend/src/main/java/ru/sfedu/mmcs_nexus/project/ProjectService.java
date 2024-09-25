package ru.sfedu.mmcs_nexus.project;

import org.springframework.beans.factory.annotation.Autowired;

public class ProjectService {

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
}
