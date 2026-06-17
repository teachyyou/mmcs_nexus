package ru.sfedu.mmcs_nexus.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import ru.sfedu.mmcs_nexus.model.entity.Project;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:projects_repository_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS \"text\" AS CLOB",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions=true"
})
class ProjectsRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldFindAllProjectsByYearWithPageable() {
        Project project2025 = createProject("Проект 2025", 2025);
        Project project2026 = createProject("Проект 2026", 2026);

        projectRepository.saveAll(List.of(project2025, project2026));

        Page<Project> result = projectRepository.findAllByYear(
                2026,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Проект 2026", result.getContent().getFirst().getName());
        assertEquals(2026, result.getContent().getFirst().getYear());
    }

    @Test
    void shouldFindAllProjectsByYearAsList() {
        Project firstProject = createProject("Первый проект", 2026);
        Project secondProject = createProject("Второй проект", 2026);
        Project project2025 = createProject("Проект 2025", 2025);

        projectRepository.saveAll(List.of(firstProject, secondProject, project2025));

        List<Project> result = projectRepository.findAllByYear(2026);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(project -> project.getYear() == 2026));
    }

    @Test
    void shouldReturnEmptyPageWhenYearDoesNotMatch() {
        Project project = createProject("Проект 2026", 2026);

        projectRepository.save(project);

        Page<Project> result = projectRepository.findAllByYear(
                2024,
                PageRequest.of(0, 10)
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void shouldCheckExistsByName() {
        Project project = createProject("MMCS Nexus", 2026);

        projectRepository.save(project);

        assertTrue(projectRepository.existsByName("MMCS Nexus"));
        assertFalse(projectRepository.existsByName("Unknown"));
    }

    @Test
    void shouldCheckExistsByNameAndIdNot() {
        Project firstProject = projectRepository.save(createProject("MMCS Nexus", 2026));
        Project secondProject = projectRepository.save(createProject("Other Project", 2026));

        assertFalse(projectRepository.existsByNameAndIdNot("MMCS Nexus", firstProject.getId()));
        assertTrue(projectRepository.existsByNameAndIdNot("MMCS Nexus", secondProject.getId()));
    }

    @Test
    void shouldApplySortingForFindAllByYear() {
        Project betaProject = createProject("Beta", 2026);
        Project alphaProject = createProject("Alpha", 2026);
        Project gammaProject = createProject("Gamma", 2026);

        projectRepository.saveAll(List.of(betaProject, alphaProject, gammaProject));

        Page<Project> result = projectRepository.findAllByYear(
                2026,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        );

        assertEquals(3, result.getTotalElements());
        assertEquals("Alpha", result.getContent().get(0).getName());
        assertEquals("Beta", result.getContent().get(1).getName());
        assertEquals("Gamma", result.getContent().get(2).getName());
    }

    @Test
    void shouldApplyPaginationForFindAllByYear() {
        Project firstProject = createProject("First", 2026);
        Project secondProject = createProject("Second", 2026);
        Project thirdProject = createProject("Third", 2026);

        projectRepository.saveAll(List.of(firstProject, secondProject, thirdProject));

        Page<Project> result = projectRepository.findAllByYear(
                2026,
                PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "name"))
        );

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Second", result.getContent().getFirst().getName());
    }

    private Project createProject(String name, int year) {
        Project project = new Project();

        project.setExternalId(year);
        project.setQuantityOfStudents(4);
        project.setCaptainName("Иван Иванов");
        project.setFull(true);
        project.setTrack("Backend");
        project.setTechnologies("Java, Spring");
        project.setName(name);
        project.setDescription("Описание проекта");
        project.setType("WEB_APP");
        project.setYear(year);

        return project;
    }
}