package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.exceptions.WrongGradePointsException;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;
import ru.sfedu.mmcs_nexus.model.entity.*;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectJuryEventKey;
import ru.sfedu.mmcs_nexus.model.enums.controller.jury.GradeTableEnums;
import ru.sfedu.mmcs_nexus.model.enums.entity.JuryRelationType;
import ru.sfedu.mmcs_nexus.model.internal.GradeTableRow;
import ru.sfedu.mmcs_nexus.model.payload.jury.CreateGradeRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.jury.GetGradeTableResponsePayload;
import ru.sfedu.mmcs_nexus.repository.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GradeService {

    private final ProjectJuryEventRepository projectJuryEventRepository;
    private final ProjectRepository projectRepository;
    private final ProjectEventRepository projectEventRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;

    @Autowired
    public GradeService(ProjectJuryEventRepository projectJuryEventRepository, ProjectRepository projectRepository,
                        ProjectEventRepository projectEventRepository, EventRepository eventRepository,
                        UserRepository userRepository, GradeRepository gradeRepository) {
        this.projectJuryEventRepository = projectJuryEventRepository;
        this.projectRepository = projectRepository;
        this.projectEventRepository = projectEventRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
    }

    @Transactional
    public GradeDTO create(String githubLogin, CreateGradeRequestPayload payload) {
        User user = userRepository.findByLogin(githubLogin).orElseThrow(() -> new UsernameNotFoundException(STR."User \{githubLogin} is not found"));

        Project project = projectRepository.findById(payload.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException(STR."Project with id \{payload.getProjectId()} not found"));

        Event event = eventRepository.findById(payload.getEventId())
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{payload.getEventId()} not found"));

        if (!projectEventRepository.existsById(new ProjectEventKey(project.getId(), event.getId()))) {
            throw new EntityNotFoundException("Given project and Event are not linked");
        }

        if (payload.getPresPoints() != null
                && payload.getPresPoints() > event.getMaxPresPoints()) {
            throw new WrongGradePointsException(STR."Maximum presentation score for \{event.getName()} is \{event.getMaxPresPoints()}");

        } else if (payload.getBuildPoints() != null
                && payload.getBuildPoints() > event.getMaxBuildPoints()) {
            throw new WrongGradePointsException(STR."Maximum build score for \{event.getName()} is \{event.getMaxBuildPoints()}");

        }

        JuryRelationType relationType;
        Optional<ProjectJuryEvent> optionalPje = projectJuryEventRepository.findByProjectIdAndEventIdAndJuryId(project.getId(),event.getId(),user.getId());

        if (optionalPje.isPresent()) {
            relationType = optionalPje.get().getRelationType();
            if (relationType == JuryRelationType.MENTOR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mentor is not allowed to grade a project assigned to them");
            }
        } else {
            relationType = JuryRelationType.WILLING;
            ProjectJuryEventKey key = new ProjectJuryEventKey(project.getId(), user.getId(), event.getId());
            ProjectJuryEvent projectJuryEvent = new ProjectJuryEvent(key, user, project, event, relationType);
            projectJuryEventRepository.save(projectJuryEvent);
        }

        GradeKey key = new GradeKey(
                project.getId(),
                event.getId(),
                user.getId()
        );

        if (existsById(key)) {
            throw new EntityExistsException("Grade already exists");
        }

        Grade grade = new Grade();

        grade.setId(key);
        grade.setEvent(event);
        grade.setProject(project);
        grade.setJury(user);

        if (payload.getPresPoints() != null) {
            grade.setPresPoints(payload.getPresPoints());
        }
        if (payload.getBuildPoints() != null) {
            grade.setBuildPoints(payload.getBuildPoints());
        }
        if (payload.getComment() != null) {
            grade.setComment(payload.getComment());
        }

        save(grade);

        return new GradeDTO(grade);
    }

    @Transactional
    public GradeDTO edit(String githubLogin, CreateGradeRequestPayload payload) {
        User user = userRepository.findByLogin(githubLogin).orElseThrow(
                () -> new UsernameNotFoundException(STR."User \{githubLogin} is not found"));

        GradeKey key = new GradeKey(
                payload.getProjectId(),
                payload.getEventId(),
                user.getId()
        );

        Grade existingGrade = find(key);
        Event event = eventRepository.findById(payload.getEventId())
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{payload.getEventId()} not found"));

        if (payload.getPresPoints() != null
                && payload.getPresPoints() > event.getMaxPresPoints()) {
            throw new WrongGradePointsException(STR."Maximum presentation score for \{event.getName()} is \{event.getMaxPresPoints()}");

        } else if (payload.getBuildPoints() != null
                && payload.getBuildPoints() > event.getMaxBuildPoints()) {
            throw new WrongGradePointsException(STR."Maximum build score for \{event.getName()} is \{event.getMaxBuildPoints()}");

        }

        existingGrade.setPresPoints(payload.getPresPoints());
        existingGrade.setBuildPoints(payload.getBuildPoints());
        existingGrade.setComment(payload.getComment());

        return new GradeDTO(existingGrade);
    }

    public GetGradeTableResponsePayload getTable(String githubLogin, String eventId, GradeTableEnums.ShowFilter showFilter, Integer day) {

        User user = userRepository.findByLogin(githubLogin).orElseThrow(
                () -> new UsernameNotFoundException(STR."User \{githubLogin} is not found"));

        Event event = eventRepository.findById(UUID.fromString(eventId))
                .orElseThrow(() -> new EntityNotFoundException(STR."Event with id \{eventId} not found"));

        //Находим в зависимости от параметра show, в случае all - все проекты привязанные к событию, иначе - только те, с которыми есть связь у отправителя запроса
        List<Project> eventProjects = findProjectsForEvent(event.getId(), showFilter, user.getId(), day).stream().sorted(Comparator.comparing(Project::getName)).toList();
        List<UserDTO> eventJuries = findJuriesForEvent(event.getId(), showFilter, user.getId(), day).stream().sorted(Comparator.comparing(UserDTO::getLastName)).toList();

        GetGradeTableResponsePayload table = new GetGradeTableResponsePayload();
        table.setEvent(event);
        table.setJuries(eventJuries);
        table.setProjects(eventProjects);

        //Создаем строки для объекта таблицы - каждому проекту ставим в соответствие несколько gradeDTO в формате Map
        for (Project project : eventProjects) {
            UUID mentorId = Optional.ofNullable(getMentor(project.getId(), event.getId())).map(UserDTO::getId).orElse(null);
            GradeTableRow row = new GradeTableRow(project.getId(), mentorId, project.getName());
            List<GradeDTO> grades = findByEventAndProject(event.getId(), project.getId())
                    .stream().map(GradeDTO::new).toList();
            row.setTableRow(grades);
            table.addGradeRow(row);
        }

        return table;
    }

    public Grade find(GradeKey key) {
        return getById(key);
    }

    private void save(Grade grade) {
        gradeRepository.save(grade);
    }

    private Grade getById(GradeKey key) {
        return gradeRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(STR."Grade not found"));
    }

    private boolean existsById(GradeKey key) {
        return gradeRepository.findById(key).isPresent();
    }

    private List<Grade> findByEventAndProject(UUID eventId, UUID projectId) {
        return gradeRepository.findByEventAndProject(eventId, projectId);
    }

    private UserDTO getMentor(UUID projectId, UUID eventId) {
        Optional<User> mentor = projectJuryEventRepository.findMentorsByProjectIdAndEventId(eventId, projectId).stream().findFirst();
        return mentor.map(UserDTO::new).orElse(null);
    }


    //Это для таблицы оценок, с фильтрацией по типу связи и дню защиты
    private List<Project> findProjectsForEvent(UUID eventId, GradeTableEnums.ShowFilter showFilter, UUID userId, Integer day) {
        return switch (showFilter) {
            case ALL -> projectEventRepository.findProjectsByEventId(eventId, day);
            case ASSIGNED -> projectJuryEventRepository.findProjectByEventAssignedToJury(eventId, userId, day);
            case MENTORED -> projectJuryEventRepository.findProjectByEventMentoredByJury(eventId, userId, day);
        };
    }

    //Это для таблицы оценок, с фильтрацией по типу связи и дню защиты
    private List<UserDTO> findJuriesForEvent(UUID eventId, GradeTableEnums.ShowFilter showFilter, UUID userId, Integer day) {
        return switch (showFilter) {
            case ALL -> projectJuryEventRepository.findJuriesByEventId(eventId, day).stream().map(UserDTO::new).toList();
            case ASSIGNED -> projectJuryEventRepository.findJuriesForProjectsAssignedToJuryByEvent(eventId, userId, day).stream().map(UserDTO::new).toList();
            case MENTORED -> projectJuryEventRepository.findJuriesForProjectsMentoredByJuryByEvent(eventId, userId, day).stream().map(UserDTO::new).toList();
        };

    }
}
