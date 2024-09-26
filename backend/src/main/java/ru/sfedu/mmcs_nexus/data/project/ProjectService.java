package ru.sfedu.mmcs_nexus.data.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJury;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryRepository;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserRepository;

import java.util.List;

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
}
