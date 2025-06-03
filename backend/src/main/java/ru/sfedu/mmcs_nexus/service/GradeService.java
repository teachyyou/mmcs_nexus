package ru.sfedu.mmcs_nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.repository.EventRepository;
import ru.sfedu.mmcs_nexus.model.entity.Grade;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;
import ru.sfedu.mmcs_nexus.repository.GradeRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectJuryEventRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectRepository;
import ru.sfedu.mmcs_nexus.repository.ProjectEventRepository;
import ru.sfedu.mmcs_nexus.repository.UserRepository;

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

    public void save(Grade grade) {
        gradeRepository.save(grade);
    }

    public void deleteById(GradeKey id) {
         gradeRepository.deleteById(id);
    }

    public Optional<Grade> findById(GradeKey id) {
        return gradeRepository.findById(id);
    }

    public List<Grade> findByYear(int year) {
        return gradeRepository.findByYear(year);
    }

    public List<Grade> findByJury(UUID juryId) {
        return gradeRepository.findByJury(juryId);
    }

    public List<Grade> findByEvent(UUID eventId) {
        return gradeRepository.findByEvent(eventId);
    }

    public List<Grade> findByProject(UUID projectId) {
        return gradeRepository.findByProject(projectId);
    }

    public List<Grade> findByJuryAndEvent(UUID juryId, UUID eventId) {
        return gradeRepository.findByJuryAndEvent(juryId, eventId);
    }

    public List<Grade> findByJuryAndProject(UUID juryId, UUID projectId) {
        return gradeRepository.findByJuryAndProject(juryId, projectId);
    }

    public List<Grade> findByEventAndProject(UUID eventId, UUID projectId) {
        return gradeRepository.findByEventAndProject(eventId, projectId);
    }

    // Методы с фильтрацией по году
    public List<Grade> findByJuryForYear(UUID juryId, int year) {
        return gradeRepository.findByJuryForYear(juryId, year);
    }


    public List<Grade> findByProjectForYear(UUID projectId, int year) {
        return gradeRepository.findByProjectForYear(projectId, year);
    }


    //idk why I wrote these tbh, event has a year in it already
    public List<Grade> findByEventForYear(UUID eventId, int year) {
        return gradeRepository.findByEventForYear(eventId, year);
    }
    public List<Grade> findByJuryAndEventForYear(UUID juryId, UUID eventId, int year) {
        return gradeRepository.findByJuryAndEventForYear(juryId, eventId, year);
    }

    public List<Grade> findByJuryAndProjectForYear(UUID juryId, UUID projectId, int year) {
        return gradeRepository.findByJuryAndProjectForYear(juryId, projectId, year);
    }

    public List<Grade> findByEventAndProjectForYear(UUID eventId, UUID projectId, int year) {
        return gradeRepository.findByEventAndProjectForYear(eventId, projectId, year);
    }
}
