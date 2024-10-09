package ru.sfedu.mmcs_nexus.data.jury_to_project;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.data.dto.UserDTO;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.event.EventRepository;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectRepository;
import ru.sfedu.mmcs_nexus.data.user.User;

import java.util.*;

@Service
public class ProjectJuryEventService {

    private final ProjectJuryEventRepository projectJuryEventRepository;

    private final ProjectRepository projectRepository;

    private final EventRepository eventRepository;

    @Autowired
    public ProjectJuryEventService(ProjectJuryEventRepository projectJuryEventRepository, ProjectRepository projectRepository, EventRepository eventRepository) {
        this.projectJuryEventRepository = projectJuryEventRepository;
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
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
}
