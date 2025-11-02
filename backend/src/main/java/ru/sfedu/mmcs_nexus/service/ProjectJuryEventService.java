package ru.sfedu.mmcs_nexus.service;

import jakarta.annotation.Nullable;
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
import ru.sfedu.mmcs_nexus.model.enums.controller.jury.GradeTableEnums;
import ru.sfedu.mmcs_nexus.model.payload.admin.AssignJuriesRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.ProjectJuryEventResponsePayload;
import ru.sfedu.mmcs_nexus.repository.*;

import java.util.List;
import java.util.Optional;
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

    //todo review rewrite everything below

    @Nullable
    public ProjectJuryEvent.RelationType getRelationType(UUID projectId, UUID eventId, UUID juryId) {
        Optional<ProjectJuryEvent> relation = projectJuryEventRepository.findByProjectIdAndEventIdAndJuryId(projectId,eventId,juryId);

        return relation.map(ProjectJuryEvent::getRelationType).orElse(null);
    }

    /*
    Находит ментора (первого) для проекта и события
     */
    @Nullable
    public UserDTO getMentor(UUID projectId, UUID eventId) {
        Optional<User> mentor = projectJuryEventRepository.findMentorsByProjectIdAndEventId(eventId, projectId).stream().findFirst();
        return mentor.map(UserDTO::new).orElse(null);
    }


    //Это для таблицы оценок, с фильтрацией по типу связи и дню защиты
    public List<Project> findProjectsForEvent(UUID eventId, GradeTableEnums.ShowFilter showFilter, UUID userId, Integer day) {
        return switch (showFilter) {
            case ALL -> projectEventRepository.findProjectsByEventId(eventId, day);
            case ASSIGNED -> projectJuryEventRepository.findProjectByEventAssignedToJury(eventId, userId, day);
            case MENTORED -> projectJuryEventRepository.findProjectByEventMentoredByJury(eventId, userId, day);
        };
    }

    //Это для таблицы оценок, с фильтрацией по типу связи и дню защиты
    public List<UserDTO> findJuriesForEvent(UUID eventId, GradeTableEnums.ShowFilter showFilter, UUID userId, Integer day) {
        return switch (showFilter) {
            case ALL -> projectJuryEventRepository.findJuriesByEventId(eventId, day).stream().map(UserDTO::new).toList();
            case ASSIGNED -> projectJuryEventRepository.findJuriesForProjectsAssignedToJuryByEvent(eventId, userId, day).stream().map(UserDTO::new).toList();
            case MENTORED -> projectJuryEventRepository.findJuriesForProjectsMentoredByJuryByEvent(eventId, userId, day).stream().map(UserDTO::new).toList();
        };

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
        saveJuriesToProjectEvent(project, event, mentors, ProjectJuryEvent.RelationType.MENTOR);
        saveJuriesToProjectEvent(project, event, obliged, ProjectJuryEvent.RelationType.OBLIGED);
        saveJuriesToProjectEvent(project, event, willing, ProjectJuryEvent.RelationType.WILLING);
    }

    private void clearProjectEventJuries(Project project, Event event) {
        projectJuryEventRepository.deleteByProjectAndEvent(project.getId(), event.getId());
    }

    //Для привязки нескольких жюри с указанным типом связи
    private void saveJuriesToProjectEvent(Project project, Event event, List<UUID> juries, ProjectJuryEvent.RelationType relationType) {

        //todo потом сделать не внутри цикла, а запросом saveAll
        for (UUID juryId: juries) {
            User jury = userRepository.findById(juryId).orElseThrow(() -> new EntityNotFoundException(STR."Jury with id \{juryId} not found"));

            ProjectJuryEventKey key = new ProjectJuryEventKey(project.getId(), jury.getId(), event.getId());
            ProjectJuryEvent projectJuryEvent = new ProjectJuryEvent(key, jury, project, event, relationType);
            projectJuryEventRepository.save(projectJuryEvent);
        }
    }

    //Для привязки одного жюри с указанным типом связи
    @Transactional
    public void addJuryToProjectEvent(UUID projectId, UUID eventId, UUID juryId, ProjectJuryEvent.RelationType relationType) {

        User jury = userRepository.findById(juryId).orElseThrow(() -> new EntityNotFoundException(STR."Jury with id \{juryId} not found"));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(STR."Project with id \{projectId} not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{eventId} not found"));

        ProjectJuryEventKey key = new ProjectJuryEventKey(projectId, jury.getId(), eventId);
        ProjectJuryEvent projectJuryEvent = new ProjectJuryEvent(key, jury, project, event, relationType);
        projectJuryEventRepository.save(projectJuryEvent);
    }
}
