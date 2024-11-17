package ru.sfedu.mmcs_nexus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.sfedu.mmcs_nexus.data.event.Event;
import ru.sfedu.mmcs_nexus.data.event.EventRepository;
import ru.sfedu.mmcs_nexus.data.event.EventType;
import ru.sfedu.mmcs_nexus.data.grade.Grade;
import ru.sfedu.mmcs_nexus.data.grade.GradeKey;
import ru.sfedu.mmcs_nexus.data.grade.GradeRepository;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEventKey;
import ru.sfedu.mmcs_nexus.data.jury_to_project.ProjectJuryEventRepository;
import ru.sfedu.mmcs_nexus.data.project.Project;
import ru.sfedu.mmcs_nexus.data.project.ProjectRepository;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEvent;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventKey;
import ru.sfedu.mmcs_nexus.data.project_to_event.ProjectEventRepository;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserRepository;
import ru.sfedu.mmcs_nexus.data.user.UserRole;
import ru.sfedu.mmcs_nexus.data.user.UserStatus;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectJuryEventRepository projectJuryEventRepository;

    private final ProjectEventRepository projectEventRepository;

    private final EventRepository eventRepository;
    private final GradeRepository gradeRepository;
    @Autowired
    public DataInitializer(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           ProjectJuryEventRepository projectJuryEventRepository, ProjectEventRepository projectEventRepository,
                           EventRepository eventRepository, GradeRepository gradeRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectJuryEventRepository = projectJuryEventRepository;
        this.projectEventRepository = projectEventRepository;
        this.eventRepository = eventRepository;
        this.gradeRepository = gradeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Проверяем, пусты ли таблицы, чтобы избежать дублирования
        if (userRepository.count() == 0 && projectRepository.count() == 0) {
            // Создаем пользователей
            User user1 = new User("John", "Doe", "johndoe", 1, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user2 = new User("Jane", "Smith", "janesmith", 2, UserStatus.NON_VERIFIED, UserRole.ROLE_ADMIN);

// Создаем и добавляем еще подтвержденных пользователей
            User user3 = new User("Alice", "Brown", "alicebrown", 3, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user4 = new User("Bob", "White", "bobwhite", 4, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user5 = new User("Carol", "Black", "carolblack", 5, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user6 = new User("David", "Green", "davidgreen", 6, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user7 = new User("Emma", "Blue", "emmablue", 7, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user8 = new User("Frank", "Red", "frankred", 8, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user9 = new User("Grace", "Yellow", "graceyellow", 9, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user10 = new User("Hank", "Purple", "hankpurple", 10, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user11 = new User("Ivy", "Gray", "ivygray", 11, UserStatus.VERIFIED, UserRole.ROLE_USER);
            User user12 = new User("Jack", "Orange", "jackorange", 12, UserStatus.VERIFIED, UserRole.ROLE_USER);

// Сохраняем всех пользователей в базу данных
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

            // Сохраняем остальных пользователей

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



            // Добавляем остальные связи жюри

            // Создаем события для 2024 года
            Event ideaEvent = new Event("Idea", EventType.IDEA, 2024);
            Event zeroVersionEvent = new Event("Zero Version", EventType.ZERO_VERSION, 2024);
            Event preReleaseEvent = new Event("Pre-Release", EventType.PRE_RELEASE, 2024);
            Event releaseEvent = new Event("Release", EventType.RELEASE, 2024);

            // Сохраняем события в базу данных
            eventRepository.save(ideaEvent);
            eventRepository.save(zeroVersionEvent);
            eventRepository.save(preReleaseEvent);
            eventRepository.save(releaseEvent);

            // Создаем связи между пользователями и Projectами (жюри)
            ProjectJuryEventKey key1 = new ProjectJuryEventKey(project1.getId(), user1.getId(), ideaEvent.getId());
            ProjectJuryEvent projectJuryEvent1 = new ProjectJuryEvent(key1, user1, project1, ideaEvent, ProjectJuryEvent.RelationType.MENTOR);
            projectJuryEventRepository.save(projectJuryEvent1);

            // Связываем проекты и события через ProjectEvent

            // Проект 1 связан с 1 событием
            ProjectEventKey projectEventKey1 = new ProjectEventKey(project1.getId(), ideaEvent.getId());
            ProjectEvent projectEvent1 = new ProjectEvent(projectEventKey1, ideaEvent, project1);
            projectEventRepository.save(projectEvent1);

            // Проект 2 связан с 2 событиями
            ProjectEventKey projectEventKey2a = new ProjectEventKey(project2.getId(), ideaEvent.getId());
            ProjectEvent projectEvent2a = new ProjectEvent(projectEventKey2a, ideaEvent, project2);
            projectEventRepository.save(projectEvent2a);

            ProjectEventKey projectEventKey2b = new ProjectEventKey(project2.getId(), zeroVersionEvent.getId());
            ProjectEvent projectEvent2b = new ProjectEvent(projectEventKey2b, zeroVersionEvent, project2);
            projectEventRepository.save(projectEvent2b);

            // Проект 3 связан с 3 событиями
            ProjectEventKey projectEventKey3a = new ProjectEventKey(project3.getId(), ideaEvent.getId());
            ProjectEvent projectEvent3a = new ProjectEvent(projectEventKey3a, ideaEvent, project3);
            projectEventRepository.save(projectEvent3a);

            ProjectEventKey projectEventKey3b = new ProjectEventKey(project3.getId(), zeroVersionEvent.getId());
            ProjectEvent projectEvent3b = new ProjectEvent(projectEventKey3b, zeroVersionEvent, project3);
            projectEventRepository.save(projectEvent3b);

            ProjectEventKey projectEventKey3c = new ProjectEventKey(project3.getId(), preReleaseEvent.getId());
            ProjectEvent projectEvent3c = new ProjectEvent(projectEventKey3c, preReleaseEvent, project3);
            projectEventRepository.save(projectEvent3c);

            // Проект 4 связан с 4 событиями
            ProjectEventKey projectEventKey4a = new ProjectEventKey(project4.getId(), ideaEvent.getId());
            ProjectEvent projectEvent4a = new ProjectEvent(projectEventKey4a, ideaEvent, project4);
            projectEventRepository.save(projectEvent4a);

            ProjectEventKey projectEventKey4b = new ProjectEventKey(project4.getId(), zeroVersionEvent.getId());
            ProjectEvent projectEvent4b = new ProjectEvent(projectEventKey4b, zeroVersionEvent, project4);
            projectEventRepository.save(projectEvent4b);

            ProjectEventKey projectEventKey4c = new ProjectEventKey(project4.getId(), preReleaseEvent.getId());
            ProjectEvent projectEvent4c = new ProjectEvent(projectEventKey4c, preReleaseEvent, project4);
            projectEventRepository.save(projectEvent4c);

            ProjectEventKey projectEventKey4d = new ProjectEventKey(project4.getId(), releaseEvent.getId());
            ProjectEvent projectEvent4d = new ProjectEvent(projectEventKey4d, releaseEvent, project4);
            projectEventRepository.save(projectEvent4d);

            Grade grade1 = new Grade();
            grade1.setId(new GradeKey(project1.getId(), user1.getId(), ideaEvent.getId()));
            grade1.setProject(project1);
            grade1.setJury(user1);
            grade1.setEvent(ideaEvent);
            grade1.setComment("Отличная презентация");
            grade1.setPresPoints(8);
            grade1.setBuildPoints(9);
            gradeRepository.save(grade1);

            Grade grade2 = new Grade();
            grade2.setId(new GradeKey(project2.getId(), user2.getId(), zeroVersionEvent.getId()));
            grade2.setProject(project2);
            grade2.setJury(user2);
            grade2.setEvent(zeroVersionEvent);
            grade2.setComment("Хорошее развитие проекта");
            grade2.setPresPoints(7);
            grade2.setBuildPoints(8);
            gradeRepository.save(grade2);

            Grade grade3 = new Grade();
            grade3.setId(new GradeKey(project3.getId(), user1.getId(), preReleaseEvent.getId()));
            grade3.setProject(project3);
            grade3.setJury(user1);
            grade3.setEvent(preReleaseEvent);
            grade3.setComment("Проект близок к завершению");
            grade3.setPresPoints(9);
            grade3.setBuildPoints(9);
            gradeRepository.save(grade3);

            Grade grade4 = new Grade();
            grade4.setId(new GradeKey(project4.getId(), user2.getId(), releaseEvent.getId()));
            grade4.setProject(project4);
            grade4.setJury(user2);
            grade4.setEvent(releaseEvent);
            grade4.setComment("Полностью готовый продукт");
            grade4.setPresPoints(10);
            grade4.setBuildPoints(10);
            gradeRepository.save(grade4);
        }
    }

}
