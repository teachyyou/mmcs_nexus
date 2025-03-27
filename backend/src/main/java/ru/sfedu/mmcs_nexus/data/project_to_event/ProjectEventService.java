package ru.sfedu.mmcs_nexus.data.project_to_event;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.project.Project;

import java.util.List;
import java.util.Optional;
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

    public Optional<ProjectEvent> findById(ProjectEventKey id) {
        return projectEventRepository.findById(id);
    }

    @Transactional
    public void deleteLinksByEventId(UUID eventId) {
        projectEventRepository.deleteByEventId(eventId);
    }

    @Transactional
    public void setProjectsForEvent(Event event, List<Project> projects) {

        deleteLinksByEventId(event.getId());

        for (Project project : projects ) {
            ProjectEventKey key = new ProjectEventKey(project.getId(), event.getId());
            ProjectEvent projectEvent = new ProjectEvent(key, event, project);
            projectEventRepository.save(projectEvent);
        }
    }
}
