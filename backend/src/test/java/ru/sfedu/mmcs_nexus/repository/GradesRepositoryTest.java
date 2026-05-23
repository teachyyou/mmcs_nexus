package ru.sfedu.mmcs_nexus.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Grade;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:grades_repository_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS \"text\" AS CLOB",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions=true"
})
class GradesRepositoryTest {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindGradeById() {
        Project project = projectRepository.save(createProject("MMCS Nexus"));
        Event event = eventRepository.save(createEvent("Идея"));
        User jury = userRepository.save(createUser("jury"));

        Grade grade = createGrade(project, event, jury, 15, 25, "Хорошая работа");

        gradeRepository.save(grade);

        Optional<Grade> result = gradeRepository.findById(new GradeKey(
                project.getId(),
                event.getId(),
                jury.getId()
        ));

        assertTrue(result.isPresent());
        assertEquals(project.getId(), result.get().getProject().getId());
        assertEquals(event.getId(), result.get().getEvent().getId());
        assertEquals(jury.getId(), result.get().getJury().getId());
        assertEquals(15, result.get().getPresPoints());
        assertEquals(25, result.get().getBuildPoints());
        assertEquals("Хорошая работа", result.get().getComment());
    }

    @Test
    void shouldFindGradesByEventAndProject() {
        Project project = projectRepository.save(createProject("MMCS Nexus"));
        Project otherProject = projectRepository.save(createProject("Other"));
        Event event = eventRepository.save(createEvent("Идея"));
        Event otherEvent = eventRepository.save(createEvent("Релиз"));

        User firstJury = userRepository.save(createUser("first"));
        User secondJury = userRepository.save(createUser("second"));
        User thirdJury = userRepository.save(createUser("third"));

        gradeRepository.save(createGrade(project, event, firstJury, 10, 20, "first"));
        gradeRepository.save(createGrade(project, event, secondJury, 11, 21, "second"));
        gradeRepository.save(createGrade(otherProject, event, thirdJury, 12, 22, "other project"));
        gradeRepository.save(createGrade(project, otherEvent, thirdJury, 13, 23, "other event"));

        List<Grade> result = gradeRepository.findByEventAndProject(event.getId(), project.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(grade -> grade.getEvent().getId().equals(event.getId())));
        assertTrue(result.stream().allMatch(grade -> grade.getProject().getId().equals(project.getId())));
        assertTrue(result.stream().anyMatch(grade -> grade.getJury().getLogin().equals("first")));
        assertTrue(result.stream().anyMatch(grade -> grade.getJury().getLogin().equals("second")));
    }

    @Test
    void shouldReturnEmptyListWhenNoGradesForEventAndProject() {
        Project project = projectRepository.save(createProject("MMCS Nexus"));
        Event event = eventRepository.save(createEvent("Идея"));

        List<Grade> result = gradeRepository.findByEventAndProject(event.getId(), project.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUpdateExistingGrade() {
        Project project = projectRepository.save(createProject("MMCS Nexus"));
        Event event = eventRepository.save(createEvent("Идея"));
        User jury = userRepository.save(createUser("jury"));

        Grade grade = gradeRepository.save(createGrade(project, event, jury, 10, 20, "old"));

        grade.setPresPoints(18);
        grade.setBuildPoints(28);
        grade.setComment("new");

        gradeRepository.save(grade);

        Grade result = gradeRepository.findById(new GradeKey(project.getId(), event.getId(), jury.getId()))
                .orElseThrow();

        assertEquals(18, result.getPresPoints());
        assertEquals(28, result.getBuildPoints());
        assertEquals("new", result.getComment());
    }

    private Grade createGrade(Project project, Event event, User jury, Integer presPoints, Integer buildPoints, String comment) {
        Grade grade = new Grade();

        grade.setId(new GradeKey(project.getId(), event.getId(), jury.getId()));
        grade.setProject(project);
        grade.setEvent(event);
        grade.setJury(jury);
        grade.setPresPoints(presPoints);
        grade.setBuildPoints(buildPoints);
        grade.setComment(comment);

        return grade;
    }

    private Project createProject(String name) {
        Project project = new Project();

        project.setExternalId(1001);
        project.setQuantityOfStudents(4);
        project.setCaptainName("Иван Иванов");
        project.setFull(true);
        project.setTrack("Backend");
        project.setTechnologies("Java, Spring");
        project.setName(name);
        project.setDescription("Описание проекта");
        project.setType("WEB_APP");
        project.setYear(2026);

        return project;
    }

    private Event createEvent(String name) {
        Event event = new Event();

        event.setName(name);
        event.setEventType(EventType.IDEA);
        event.setYear(2026);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);

        return event;
    }

    private User createUser(String login) {
        User user = new User();

        user.setLogin(login);
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail(login + "@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_JURY);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }
}