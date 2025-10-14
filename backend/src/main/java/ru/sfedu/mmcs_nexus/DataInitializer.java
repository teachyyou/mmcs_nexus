package ru.sfedu.mmcs_nexus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.repository.EventRepository;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.entity.Grade;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;
import ru.sfedu.mmcs_nexus.repository.GradeRepository;
import ru.sfedu.mmcs_nexus.model.entity.ProjectJuryEvent;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectJuryEventKey;
import ru.sfedu.mmcs_nexus.repository.ProjectJuryEventRepository;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.repository.ProjectRepository;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEvent;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventKey;
import ru.sfedu.mmcs_nexus.repository.ProjectEventRepository;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.repository.UserRepository;

import java.util.List;

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
                           ProjectJuryEventRepository projectJuryEventRepository,
                           ProjectEventRepository projectEventRepository,
                           EventRepository eventRepository,
                           GradeRepository gradeRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectJuryEventRepository = projectJuryEventRepository;
        this.projectEventRepository = projectEventRepository;
        this.eventRepository = eventRepository;
        this.gradeRepository = gradeRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // seedInitialUsersBatch();
        // seedFullDemo2024IfEmpty();
         seedProjects2025();
    }

    /**
     * Первая «куча»: 8 пользователей с русскими именами.
     */
    private void seedInitialUsersBatch() {
        List<String> logins = List.of(
                "alexsidr", "ivanzoor", "dmsmir", "sergorlov",
                "nastiakuz", "katiasorok", "mariapop", "dasha_roma"
        );
        boolean anyExists = userRepository.findAll().stream()
                .anyMatch(u -> logins.contains(u.getLogin()));

        if (anyExists) return;

        User user1 = new User("Алексей", "Сидоренко", "alexsidr", 2, 4, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user2 = new User("Иван", "Журавлёв", "ivanzoor", 3, 2, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user3 = new User("Дмитрий", "Смирнов", "dmsmir", 1, 1, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user4 = new User("Сергей", "Орлов", "sergorlov", 4, 3, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);

        User user5 = new User("Анастасия", "Кузнецова", "nastiakuz", 1, 1, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user6 = new User("Екатерина", "Сорокина", "katiasorok", 2, 2, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user7 = new User("Мария", "Попова", "mariapop", 3, 3, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user8 = new User("Дарья", "Романова", "dasha_roma", 4, 4, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5, user6, user7, user8));
    }


    private void seedFullDemo2024IfEmpty() {
        if (userRepository.count() != 0 || projectRepository.count() != 0) return;

        User user1 = new User("Алексей", "Сидоренко", "alexsidr", 2, 4, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user2 = new User("Jane", "Smith", "janesmith", 2, 3, UserEnums.UserStatus.NON_VERIFIED, UserEnums.UserRole.ROLE_ADMIN);

        User user3 = new User("Alice", "Brown", "alicebrown", 3, 5, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user4 = new User("Bob", "White", "bobwhite", 4, 1, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user5 = new User("Carol", "Black", "carolblack", 5, 13, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user6 = new User("David", "Green", "davidgreen", 6, 4, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user7 = new User("Emma", "Blue", "emmablue", 7, 1, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user8 = new User("Frank", "Red", "frankred", 8, 2, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user9 = new User("Grace", "Yellow", "graceyellow", 9, 3, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user10 = new User("Hank", "Purple", "hankpurple", 10, 4, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user11 = new User("Ivy", "Gray", "ivygray", 11, 5, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);
        User user12 = new User("Jack", "Orange", "jackorange", 12, 6, UserEnums.UserStatus.VERIFIED, UserEnums.UserRole.ROLE_USER);

        userRepository.saveAll(List.of(
                user1, user2, user3, user4, user5, user6, user7, user8, user9, user10, user11, user12
        ));

        Project project1 = new Project("Project A", "Description Project A", "WEB_APP", 2024);
        Project project2 = new Project("Project B", "Description Project B", "WEB_APP", 2024);
        Project project3 = new Project("Project C", "Description Project C", "GAME", 2024);
        Project project4 = new Project("Project D", "Description Project D", "DESKTOP-APP", 2024);

        projectRepository.saveAll(List.of(project1, project2, project3, project4));

        Event ideaEvent = new Event("Idea", EventType.IDEA, 2024, 0, 15);
        Event zeroVersionEvent = new Event("Zero Version", EventType.ZERO_VERSION, 2024, 10, 15);
        Event preReleaseEvent = new Event("Pre-Release", EventType.PRE_RELEASE, 2024, 10, 15);
        Event releaseEvent = new Event("Release", EventType.RELEASE, 2024, 10, 15);

        eventRepository.saveAll(List.of(ideaEvent, zeroVersionEvent, preReleaseEvent, releaseEvent));

        ProjectJuryEventKey key1 = new ProjectJuryEventKey(project1.getId(), user1.getId(), ideaEvent.getId());
        ProjectJuryEvent projectJuryEvent1 = new ProjectJuryEvent(key1, user1, project1, ideaEvent, ProjectJuryEvent.RelationType.MENTOR);
        projectJuryEventRepository.save(projectJuryEvent1);


        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project1.getId(), ideaEvent.getId()), ideaEvent, project1));

        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project2.getId(), ideaEvent.getId()), ideaEvent, project2));
        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project2.getId(), zeroVersionEvent.getId()), zeroVersionEvent, project2));

        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project3.getId(), ideaEvent.getId()), ideaEvent, project3));
        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project3.getId(), zeroVersionEvent.getId()), zeroVersionEvent, project3));
        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project3.getId(), preReleaseEvent.getId()), preReleaseEvent, project3));

        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project4.getId(), ideaEvent.getId()), ideaEvent, project4));
        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project4.getId(), zeroVersionEvent.getId()), zeroVersionEvent, project4));
        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project4.getId(), preReleaseEvent.getId()), preReleaseEvent, project4));
        projectEventRepository.save(new ProjectEvent(new ProjectEventKey(project4.getId(), releaseEvent.getId()), releaseEvent, project4));

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


    private void seedProjects2025() {
        record Draft(String name, String desc, String type) {}

        List<Draft> drafts = List.of(
                new Draft("Campus Navigator", "Приложение для навигации по кампусу с расписанием аудиторий", "MOBILE_APP"),
                new Draft("Nexus Portal 2.0", "Веб-портал для проектов с личными кабинетами и оценками", "WEB_APP"),
                new Draft("Mod: Quantum Rails", "Мод к игре с новыми механиками транспорта", "GAME_MOD"),
                new Draft("DeskTrack", "Десктоп-утилита для трекинга задач и времени", "DESKTOP_APP"),
                new Draft("Chess Arena", "Онлайн-аркада-шахматы с матчмейкингом", "GAME"),
                new Draft("Schedule Bot", "Телеграм-бот для расписания и уведомлений о дедлайнах", "TELEGRAM_BOT")
        );

        for (Draft d : drafts) {
            boolean exists = projectRepository.findAll().stream()
                    .anyMatch(p -> p.getName().equalsIgnoreCase(d.name) && p.getYear() == 2025);
            if (exists) continue;

            Project p = new Project(d.name, d.desc, d.type, 2025);
            projectRepository.save(p);
        }
    }
}
