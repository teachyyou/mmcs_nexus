package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectJuryEventKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.JuryRelationType;
import ru.sfedu.mmcs_nexus.model.payload.admin.AssignJuriesRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.ProjectJuryEventResponsePayload;
import ru.sfedu.mmcs_nexus.repository.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

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

    @Transactional
    public void assignJuries(AssignJuriesRequestPayload payload) {

        UUID projectId = UUID.fromString(payload.getProjectId());
        UUID eventId = UUID.fromString(payload.getEventId());

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(STR."Project with id \{projectId} not found"));

        if (payload.isApplyToAllEvents()) {
            List<Event> events = projectEventRepository.findEventsByProjectId(projectId, null);
            for (Event event : events) {
                assignJuriesForEvent(
                        project,
                        event,
                        payload.getMentors().stream().map(UUID::fromString).toList(),
                        payload.getObligedJuries().stream().map(UUID::fromString).toList(),
                        payload.getWillingJuries().stream().map(UUID::fromString).toList()
                );
            }

        } else {
            Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{eventId} not found"));

            assignJuriesForEvent(
                    project,
                    event,
                    payload.getMentors().stream().map(UUID::fromString).toList(),
                    payload.getObligedJuries().stream().map(UUID::fromString).toList(),
                    payload.getWillingJuries().stream().map(UUID::fromString).toList()
            );
        }
    }

    public ProjectJuryEventResponsePayload getJuriesByProjectAndEvent(String projectId, String eventId) {

        if (!projectRepository.existsById(UUID.fromString(projectId))) {
            throw new EntityNotFoundException(STR."Project with id \{projectId} not found");
        }

        if (!eventRepository.existsById(UUID.fromString(eventId))) {
            throw new EntityNotFoundException(STR."Event with id \{eventId} not found");
        }

        List<ProjectJuryEvent> projectJuryEvents = projectJuryEventRepository.findByProjectIdAndEventId(UUID.fromString(projectId), UUID.fromString(eventId));

        ProjectJuryEventResponsePayload payload = new ProjectJuryEventResponsePayload();

        for (ProjectJuryEvent pje : projectJuryEvents) {
            User jury = pje.getJury();
            UserDTO juryDTO = new UserDTO(jury);

            switch (pje.getRelationType()) {
                case WILLING:
                    payload.getWillingJuries().add(juryDTO);
                    break;
                case OBLIGED:
                    payload.getObligedJuries().add(juryDTO);
                    break;
                case MENTOR:
                    payload.getMentors().add(juryDTO);
                    break;
            }
        }

        return payload;
    }

    private void assignJuriesForEvent(Project project, Event event, List<UUID> mentors, List<UUID> obliged,List<UUID> willing) {
        clearProjectEventJuries(project, event);
        saveJuriesToProjectEvent(project, event, mentors, JuryRelationType.MENTOR);
        saveJuriesToProjectEvent(project, event, obliged, JuryRelationType.OBLIGED);
        saveJuriesToProjectEvent(project, event, willing, JuryRelationType.WILLING);
    }

    private void clearProjectEventJuries(Project project, Event event) {
        projectJuryEventRepository.deleteByProjectAndEvent(project.getId(), event.getId());
    }

    //Для привязки нескольких жюри с указанным типом связи
    private void saveJuriesToProjectEvent(Project project, Event event, List<UUID> juries, JuryRelationType relationType) {

        if (juries == null || juries.isEmpty()) return;

        List<User> users = userRepository.findAllById(juries);

        if (users.size() != juries.size()) {
            LinkedHashSet<UUID> requested = new java.util.LinkedHashSet<>(juries);
            for (User u : users)
                requested.remove(u.getId());
            throw new EntityNotFoundException(STR."Jury with ids \{requested} not found");
        }

        ArrayList<ProjectJuryEvent> links = new java.util.ArrayList<>(users.size());

        for (User jury : users) {
            ProjectJuryEventKey key = new ProjectJuryEventKey(project.getId(), jury.getId(), event.getId());
            links.add(new ProjectJuryEvent(key, jury, project, event, relationType));
        }
        projectJuryEventRepository.saveAll(links);
    }
}
