package ru.sfedu.mmcs_nexus.data.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEventRepository;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectJuryEventRepository projectJuryEventRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          ProjectJuryEventRepository projectJuryEventRepository)
    {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectJuryEventRepository = projectJuryEventRepository;
    }

    public List<Project> findByFirstname(String firstname) {
        User user = userRepository.findByFirstName(firstname);
        List<ProjectJuryEvent> projectJuryEventList = projectJuryEventRepository.findByJury(user);

        return projectJuryEventList.stream().map(ProjectJuryEvent::getProject).toList();
    }

    public Optional<Project> findById(UUID id) {
        return projectRepository.findById(id);
    }

    public List<Project> findByIds(List<UUID> ids) {
        return projectRepository.findAllById(ids);
    }

    public List<Project> findByYear(int year) {
        return projectRepository.findByYear(year);
    }

    public Optional<Project> findByName(String name) {
        return projectRepository.findByName(name).stream().findFirst();
    }

    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    public boolean existsByName(String name) {
        return projectRepository.existsByName(name);
    }

    public void saveProject(Project project) {
        projectRepository.saveAndFlush(project);
    }

    public List<Project> getProjects(String sort, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return projectRepository.findAll(Sort.by(direction, sort));
    }

    public List<Project> getProjects(String sort, String order, String year) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return projectRepository.findByYear(Integer.parseInt(year), Sort.by(direction, sort));
    }

    public boolean existsById(UUID id) {
        return projectRepository.existsById(id);
    }

    public void deleteById(UUID id) {
        projectRepository.deleteById(id);
    }
}
