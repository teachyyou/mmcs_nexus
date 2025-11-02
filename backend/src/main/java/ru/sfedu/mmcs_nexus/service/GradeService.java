package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.exceptions.WrongGradePointsException;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;
import ru.sfedu.mmcs_nexus.model.entity.*;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectJuryEventKey;
import ru.sfedu.mmcs_nexus.model.payload.jury.CreateGradeRequestPayload;
import ru.sfedu.mmcs_nexus.repository.*;

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


        if (payload.getPresPoints() != null
                && payload.getPresPoints() > event.getMaxPresPoints()) {
            throw new WrongGradePointsException(STR."Maximum presentation score for \{event.getName()} is \{event.getMaxPresPoints()}");

        } else if (payload.getBuildPoints() != null
                && payload.getBuildPoints() > event.getMaxBuildPoints()) {
            throw new WrongGradePointsException(STR."Maximum build score for \{event.getName()} is \{event.getMaxBuildPoints()}");

        }

        ProjectJuryEvent.RelationType relationType;
        Optional<ProjectJuryEvent> optionalPje = projectJuryEventRepository.findByProjectIdAndEventIdAndJuryId(project.getId(),event.getId(),user.getId());

        if (optionalPje.isPresent()) {
            relationType = optionalPje.get().getRelationType();
            if (relationType == ProjectJuryEvent.RelationType.MENTOR) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mentor is not allowed to grade a project assigned to them");
            }
        } else {
            relationType = ProjectJuryEvent.RelationType.WILLING;
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

    public GradeDTO edit(String githubLogin, CreateGradeRequestPayload payload) {
        User user = userRepository.findByLogin(githubLogin).orElseThrow(() -> new UsernameNotFoundException(STR."User \{githubLogin} is not found"));

        GradeKey key = new GradeKey(
                payload.getProjectId(),
                payload.getEventId(),
                user.getId()
        );

        // Получаем пользователя (проверяющего) из аутентификации
        if (userService.findByGithubLogin(authentication).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (projectEventService.findById(new ProjectEventKey(gradeDTO.getProjectId(), gradeDTO.getEventId())).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project and event are not linked");
        }

        Grade existingGrade = gradeService.find(key);
        Event event = eventService.find(gradeDTO.getEventId().toString());

        if (gradeDTO.getPresPoints() != null && gradeDTO.getPresPoints() > event.getMaxPresPoints()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(STR."Maximum presentation score for \{event.getName()} is \{event.getMaxPresPoints()}");
        } else if (gradeDTO.getBuildPoints() != null && gradeDTO.getBuildPoints() > event.getMaxBuildPoints()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(STR."Maximum build score for \{event.getName()} is \{event.getMaxBuildPoints()}");
        }

        existingGrade.setPresPoints(gradeDTO.getPresPoints());
        existingGrade.setBuildPoints(gradeDTO.getBuildPoints());
        existingGrade.setComment(gradeDTO.getComment());

        gradeService.save(existingGrade);

        return ResponseEntity.ok().body(new GradeDTO(existingGrade));
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

    //todo clear this

    public List<Grade> findByEventAndProject(UUID eventId, UUID projectId) {
        return gradeRepository.findByEventAndProject(eventId, projectId);
    }
}
