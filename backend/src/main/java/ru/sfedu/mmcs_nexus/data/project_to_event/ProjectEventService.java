package ru.sfedu.mmcs_nexus.data.project_to_event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.project.Project;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectEventService {

    private final ProjectEventRepository projectEventRepository;

    @Autowired
    public ProjectEventService(ProjectEventRepository projectEventRepository) {
        this.projectEventRepository = projectEventRepository;
    }


    public List<Project> findByEventId(UUID eventId) {
        return projectEventRepository.findByEventId(eventId);
    }

    public List<Event> findByProjectId(UUID projectId) {
        return projectEventRepository.findByProjectId(projectId);
    }
}
