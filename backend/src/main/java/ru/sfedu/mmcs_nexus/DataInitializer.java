package ru.sfedu.mmcs_nexus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.repository.*;

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
         seedProjects2025();
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
