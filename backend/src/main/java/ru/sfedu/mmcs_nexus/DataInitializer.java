package ru.sfedu.mmcs_nexus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJury;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryKey;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryRepository;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectRepository;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserRepository;
import ru.sfedu.mmcs_nexus.data.user.UserRole;
import ru.sfedu.mmcs_nexus.data.user.UserStatus;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectJuryRepository projectJuryRepository;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           ProjectJuryRepository projectJuryRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectJuryRepository = projectJuryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Проверяем, пусты ли таблицы, чтобы избежать дублирования
        if (userRepository.count() == 0 && projectRepository.count() == 0) {
            // Создаем пользователей
            User user1 = new User("John", "Doe", "johndoe", 1, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user2 = new User("Jane", "Smith", "janesmith", 2, UserStatus.NON_VERIFIED, UserRole.ROLE_ADMIN);
            User user3 = new User("Alice", "Johnson", "alicej", 3, UserStatus.BLOCKED, UserRole.ROLE_USER);
            User user4 = new User("Bob", "Brown", "bobbrown", 4, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user5 = new User("Charlie", "Davis", "charlied", 5, UserStatus.NON_VERIFIED, UserRole.ROLE_USER);
            User user6 = new User("Diana", "Evans", "dianaev", 6, UserStatus.VERIFIED, UserRole.ROLE_ADMIN);
            User user7 = new User("Ethan", "Foster", "ethanf", 7, UserStatus.BLOCKED, UserRole.ROLE_USER);
            User user8 = new User("Fiona", "Green", "fionag", 8, UserStatus.NON_VERIFIED, UserRole.ROLE_USER);
            User user9 = new User("George", "Harris", "georgeh", 9, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user10 = new User("Hannah", "Irvine", "hannahir", 10, UserStatus.BLOCKED, UserRole.ROLE_ADMIN);
            User user11 = new User("Isaac", "James", "isaacj", 11, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user12 = new User("Julia", "Kane", "juliak", 12, UserStatus.NON_VERIFIED, UserRole.ROLE_USER);
            User user13 = new User("Kevin", "Lewis", "kevinl", 13, UserStatus.BLOCKED, UserRole.ROLE_USER);
            User user14 = new User("Lily", "Moore", "lilym", 14, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user15 = new User("Michael", "Nolan", "michaeln", 15, UserStatus.NON_VERIFIED, UserRole.ROLE_USER);
            User user16 = new User("Nina", "Owens", "ninao", 16, UserStatus.BLOCKED, UserRole.ROLE_USER);

            // Сохраняем пользователей в базу данных
            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);
            userRepository.save(user4);
            userRepository.save(user5);
            userRepository.save(user6);
            userRepository.save(user7);
            userRepository.save(user8);
            userRepository.save(user9);
            userRepository.save(user10);
            userRepository.save(user11);
            userRepository.save(user12);
            userRepository.save(user13);
            userRepository.save(user14);
            userRepository.save(user15);
            userRepository.save(user16);

            // Создаем Projectы
            Project project1 = new Project("Project A", "Description Project A", "WEB_APP", 2024);
            Project project2 = new Project("Project B", "Description Project B", "WEB_APP", 2024);
            Project project3 = new Project("Project C", "Description Project C", "GAME", 2024);
            Project project4 = new Project("Project D", "Description Project D", "DESKTOP-APP", 2024);

            // Сохраняем Projectы в базу данных
            projectRepository.save(project1);
            projectRepository.save(project2);
            projectRepository.save(project3);
            projectRepository.save(project4);

            // Создаем связи между пользователями и Projectами (жюри)
            // Предполагаем, что user1 - ментор Projectа1
            ProjectJuryKey key1 = new ProjectJuryKey();
            key1.setProjectId(project1.getId());
            key1.setJuryId(user1.getId());

            ProjectJury projectJury1 = new ProjectJury();
            projectJury1.setId(key1);
            projectJury1.setProjects(project1);
            projectJury1.setJuries(user1);
            projectJury1.setRelationType(ProjectJury.RelationType.MENTOR);

            projectJuryRepository.save(projectJury1);

            // Добавим еще связи
            ProjectJuryKey key2 = new ProjectJuryKey();
            key2.setProjectId(project1.getId());
            key2.setJuryId(user2.getId());

            ProjectJury projectJury2 = new ProjectJury();
            projectJury2.setId(key2);
            projectJury2.setProjects(project1);
            projectJury2.setJuries(user2);
            projectJury2.setRelationType(ProjectJury.RelationType.WILLING);

            projectJuryRepository.save(projectJury2);

            // Аналогично добавляем другие связи
            ProjectJuryKey key3 = new ProjectJuryKey();
            key3.setProjectId(project2.getId());
            key3.setJuryId(user1.getId());

            ProjectJury projectJury3 = new ProjectJury();
            projectJury3.setId(key3);
            projectJury3.setProjects(project2);
            projectJury3.setJuries(user1);
            projectJury3.setRelationType(ProjectJury.RelationType.OBLIGED);

            projectJuryRepository.save(projectJury3);

            // Добавьте остальные связи по аналогии
        }
    }
}
