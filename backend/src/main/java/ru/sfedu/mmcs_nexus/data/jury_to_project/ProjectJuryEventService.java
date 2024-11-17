package ru.sfedu.mmcs_nexus.data.jury_to_project;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.data.dto.UserDTO;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.event.EventRepository;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectRepository;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventRepository;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserRepository;

import java.util.*;

@Service
public class ProjectJuryEventService {

    private final ProjectJuryEventRepository projectJuryEventRepository;

    private final ProjectRepository projectRepository;

    private final ProjectEventRepository projectEventRepository;

    private final EventRepository eventRepository;

    private final UserRepository userRepository;

    @Autowired
    public ProjectJuryEventService(ProjectJuryEventRepository projectJuryEventRepository, ProjectRepository projectRepository, ProjectEventRepository projectEventRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.projectJuryEventRepository = projectJuryEventRepository;
        this.projectRepository = projectRepository;
        this.projectEventRepository = projectEventRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Nullable
    public ProjectJuryEvent.RelationType getRelationType(UUID projectId, UUID eventId, UUID juryId) {
        Optional<ProjectJuryEvent> relation = projectJuryEventRepository.findByProjectIdAndEventIdAndJuryId(projectId,eventId,juryId);

        return relation.map(ProjectJuryEvent::getRelationType).orElse(null);
    }

    public Map<String, List<UserDTO>> getJuriesByProjectAndEvent(UUID projectId, UUID eventId) {

        // Verify that the project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        // Verify that the event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // Retrieve ProjectJuryEvent entries
        List<ProjectJuryEvent> projectJuryEvents = projectJuryEventRepository.findByProjectIdAndEventId(projectId, eventId);

        // Group juries by relation type
        Map<String, List<UserDTO>> juriesByRelationType = new HashMap<>();
        juriesByRelationType.put("willingJuries", new ArrayList<>());
        juriesByRelationType.put("obligedJuries", new ArrayList<>());
        juriesByRelationType.put("mentors", new ArrayList<>());

        for (ProjectJuryEvent pje : projectJuryEvents) {
            User jury = pje.getJury();
            UserDTO juryDTO = new UserDTO(jury);

            switch (pje.getRelationType()) {
                case WILLING:
                    juriesByRelationType.get("willingJuries").add(juryDTO);
                    break;
                case OBLIGED:
                    juriesByRelationType.get("obligedJuries").add(juryDTO);
                    break;
                case MENTOR:
                    juriesByRelationType.get("mentors").add(juryDTO);
                    break;
            }
        }

        return juriesByRelationType;
    }

    @Transactional
    public void clearProjectEventJuries(Project project, Event event) {
        projectJuryEventRepository.deleteByProjectAndEvent(project.getId(), event.getId());
    }

    @Transactional
    public void clearProjectEventsJuries(Project project) {
        List<Event> events = projectEventRepository.findByProjectId(project.getId());
        for (Event event : events) {
            projectJuryEventRepository.deleteByProjectAndEvent(project.getId(), event.getId());
        }
    }

    @Transactional
    public void saveJuriesToProjectEvent(Project project, Event event, List<UUID> juries, ProjectJuryEvent.RelationType relationType) {

        for (UUID juryId: juries) {
            User jury = userRepository.findById(juryId).orElseThrow(() -> new EntityNotFoundException(STR."Jury with id \{juryId} not found"));

            ProjectJuryEventKey key = new ProjectJuryEventKey(juryId, jury.getId(), event.getId());
            ProjectJuryEvent projectJuryEvent = new ProjectJuryEvent(key, jury, project, event, relationType);
            projectJuryEventRepository.save(projectJuryEvent);
        }
    }
}
