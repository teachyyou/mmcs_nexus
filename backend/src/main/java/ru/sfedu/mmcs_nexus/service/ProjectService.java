package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.model.dto.entity.CaptainProjectDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectUserDTO;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateProjectRequestPayload;
import ru.sfedu.mmcs_nexus.repository.ProjectRepository;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public Page<ProjectDTO> findAll(Integer year, PaginationPayload paginationPayload) {
        Pageable pageable = paginationPayload.getPageable();

        Page<Project> projects = year != null
                ? projectRepository.findAllByYear(year, pageable)
                : projectRepository.findAll(pageable);

        return projects.map(ProjectDTO::new);
    }

    public Project find(String projectId) {
        return getById(projectId);
    }

    @Transactional
    public void create(CreateProjectRequestPayload payload) {
        if (existsByName(payload.getName())) {
            throw new EntityExistsException("Project with name " + payload.getName() + " already exists");
        }

        Project project = new Project(
                payload.getExternalId(),
                payload.getQuantityOfStudents(),
                payload.getCaptainName(),
                payload.isFull(),
                payload.getTrack(),
                payload.getTechnologies(),
                payload.getName(),
                payload.getDescription(),
                payload.getType(),
                payload.getYear()
        );

        projectRepository.save(project);
    }

    @Transactional
    public Project edit(String projectId, CreateProjectRequestPayload payload) {
        Project project = getById(projectId);

        if (projectRepository.existsByNameAndIdNot(payload.getName(), project.getId())) {
            throw new EntityExistsException("Project with name " + payload.getName() + " already exists");
        }

        project.setName(payload.getName());
        project.setDescription(payload.getDescription());
        project.setType(payload.getType());
        project.setYear(payload.getYear());
        project.setExternalId(payload.getExternalId());
        project.setQuantityOfStudents(payload.getQuantityOfStudents());
        project.setCaptainName(payload.getCaptainName());
        project.setFull(payload.isFull());
        project.setTrack(payload.getTrack());
        project.setTechnologies(payload.getTechnologies());

        return project;
    }

    @Transactional
    public void delete(String projectId) {
        Project project = getById(projectId);
        projectRepository.delete(project);
    }

    public Page<ProjectUserDTO> findAllForUser(Integer year, PaginationPayload paginationPayload, String githubLogin) {
        User user = getVerifiedUserByLogin(githubLogin);

        int targetYear = year != null ? year : Year.now().getValue();

        return projectRepository.findAllByYear(targetYear, paginationPayload.getPageable())
                .map(project -> new ProjectUserDTO(project, user));
    }

    public ProjectUserDTO findForUser(String projectId, String githubLogin) {
        User user = getVerifiedUserByLogin(githubLogin);

        Project project = getById(projectId);

        return new ProjectUserDTO(project, user);
    }

    @Transactional
    public ProjectUserDTO assignCaptain(String projectId, String githubLogin) {
        User user = getVerifiedUserByLogin(githubLogin);

        Project project = getById(projectId);

        if (project.getCaptain() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project already has captain");
        }

        if (projectRepository.existsByCaptainId(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has captain project");
        }

        project.setCaptain(user);

        return new ProjectUserDTO(project, user);
    }

    public Optional<CaptainProjectDTO> findCaptainProject(String githubLogin) {
        User user = userService.findByGithubLogin(githubLogin)
                .orElseThrow(() -> new UsernameNotFoundException("User " + githubLogin + " is not found"));

        return projectRepository.findByCaptainId(user.getId())
                .map(CaptainProjectDTO::new);
    }

    private User getVerifiedUserByLogin(String githubLogin) {
        User user = userService.findByGithubLogin(githubLogin)
                .orElseThrow(() -> new UsernameNotFoundException("User " + githubLogin + " is not found"));

        if (user.getStatus() != UserEnums.UserStatus.VERIFIED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not verified");
        }

        return user;
    }

    private Project getById(String projectId) {
        return projectRepository.findById(UUID.fromString(projectId))
                .orElseThrow(() -> new EntityNotFoundException("Project with id " + projectId + " not found"));
    }

    public boolean existsByName(String name) {
        return projectRepository.existsByName(name);
    }
}