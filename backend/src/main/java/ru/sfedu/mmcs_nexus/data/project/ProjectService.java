package ru.sfedu.mmcs_nexus.data.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJury;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryRepository;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectJuryRepository projectJuryRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          ProjectJuryRepository projectJuryRepository)
    {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectJuryRepository = projectJuryRepository;
    }

    public List<Project> findByFirstname(String firstname) {
        User user = userRepository.findByFirstName(firstname);
        List<ProjectJury> projectJuryList = projectJuryRepository.findByJuries(user);

        return projectJuryList.stream().map(ProjectJury::getProjects).toList();
    }

    public Optional<Project> findById(UUID id) {
        return projectRepository.findById(id);
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

    public boolean existsById(UUID id) {
        return projectRepository.existsById(id);
    }

    public void deleteProjectById(UUID id) {
        projectRepository.deleteById(id);
    }
}
