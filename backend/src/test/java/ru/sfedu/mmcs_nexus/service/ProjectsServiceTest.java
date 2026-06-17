package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectDTO;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreateProjectRequestPayload;
import ru.sfedu.mmcs_nexus.repository.ProjectRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldFindAllProjectsWithoutYear() {
        Project project = createProject();
        PaginationPayload paginationPayload = new PaginationPayload(10, 0, "id", "asc", EntitySort.PROJECT_SORT);

        when(projectRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(project)));

        Page<ProjectDTO> result = projectService.findAll(null, paginationPayload);

        assertEquals(1, result.getTotalElements());
        assertEquals(project.getId(), result.getContent().getFirst().getId());

        verify(projectRepository).findAll(any(org.springframework.data.domain.Pageable.class));
        verify(projectRepository, never()).findAllByYear(anyInt(), any());
    }

    @Test
    void shouldFindAllProjectsByYear() {
        Project project = createProject();
        PaginationPayload paginationPayload = new PaginationPayload(10, 0, "id", "asc", EntitySort.PROJECT_SORT);

        when(projectRepository.findAllByYear(eq(2026), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(project)));

        Page<ProjectDTO> result = projectService.findAll(2026, paginationPayload);

        assertEquals(1, result.getTotalElements());
        assertEquals(project.getId(), result.getContent().getFirst().getId());

        verify(projectRepository).findAllByYear(eq(2026), any(org.springframework.data.domain.Pageable.class));
        verify(projectRepository, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void shouldFindProjectById() {
        Project project = createProject();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        Project result = projectService.find(project.getId().toString());

        assertSame(project, result);

        verify(projectRepository).findById(project.getId());
    }

    @Test
    void shouldThrowWhenProjectNotFound() {
        UUID projectId = UUID.randomUUID();

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectService.find(projectId.toString())
        );

        assertEquals("Project with id " + projectId + " not found", exception.getMessage());

        verify(projectRepository).findById(projectId);
    }

    @Test
    void shouldCreateProject() {
        CreateProjectRequestPayload payload = createPayload("Новый проект", "Описание", 2026);

        when(projectRepository.existsByName("Новый проект")).thenReturn(false);

        projectService.create(payload);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

        verify(projectRepository).save(projectCaptor.capture());

        Project savedProject = projectCaptor.getValue();

        assertEquals(1001, savedProject.getExternalId());
        assertEquals(4, savedProject.getQuantityOfStudents());
        assertEquals("Иван Иванов", savedProject.getCaptainName());
        assertTrue(savedProject.isFull());
        assertEquals("Backend", savedProject.getTrack());
        assertEquals("Java, Spring", savedProject.getTechnologies());
        assertEquals("Новый проект", savedProject.getName());
        assertEquals("Описание", savedProject.getDescription());
        assertEquals("WEB_APP", savedProject.getType());
        assertEquals(2026, savedProject.getYear());
    }

    @Test
    void shouldThrowWhenCreatingProjectWithExistingName() {
        CreateProjectRequestPayload payload = createPayload("Duplicate", "Описание", 2026);

        when(projectRepository.existsByName("Duplicate")).thenReturn(true);

        EntityExistsException exception = assertThrows(
                EntityExistsException.class,
                () -> projectService.create(payload)
        );

        assertEquals("Project with name Duplicate already exists", exception.getMessage());

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void shouldEditProject() {
        Project project = createProject();
        CreateProjectRequestPayload payload = createPayload("Обновлённый проект", "Новое описание", 2027);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.existsByNameAndIdNot("Обновлённый проект", project.getId())).thenReturn(false);

        Project result = projectService.edit(project.getId().toString(), payload);

        assertSame(project, result);
        assertEquals(1001, project.getExternalId());
        assertEquals(4, project.getQuantityOfStudents());
        assertEquals("Иван Иванов", project.getCaptainName());
        assertTrue(project.isFull());
        assertEquals("Backend", project.getTrack());
        assertEquals("Java, Spring", project.getTechnologies());
        assertEquals("Обновлённый проект", project.getName());
        assertEquals("Новое описание", project.getDescription());
        assertEquals("WEB_APP", project.getType());
        assertEquals(2027, project.getYear());

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void shouldThrowWhenEditingUnknownProject() {
        UUID projectId = UUID.randomUUID();
        CreateProjectRequestPayload payload = createPayload("Проект", "Описание", 2026);

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectService.edit(projectId.toString(), payload)
        );

        assertEquals("Project with id " + projectId + " not found", exception.getMessage());

        verify(projectRepository, never()).existsByNameAndIdNot(any(), any());
    }

    @Test
    void shouldThrowWhenEditingProjectWithExistingName() {
        Project project = createProject();
        CreateProjectRequestPayload payload = createPayload("Duplicate", "Описание", 2026);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.existsByNameAndIdNot("Duplicate", project.getId())).thenReturn(true);

        EntityExistsException exception = assertThrows(
                EntityExistsException.class,
                () -> projectService.edit(project.getId().toString(), payload)
        );

        assertEquals("Project with name Duplicate already exists", exception.getMessage());
    }

    @Test
    void shouldDeleteProject() {
        Project project = createProject();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        projectService.delete(project.getId().toString());

        verify(projectRepository).delete(project);
    }

    @Test
    void shouldThrowWhenDeletingUnknownProject() {
        UUID projectId = UUID.randomUUID();

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> projectService.delete(projectId.toString())
        );

        assertEquals("Project with id " + projectId + " not found", exception.getMessage());

        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void shouldCheckProjectExistsByName() {
        when(projectRepository.existsByName("MMCS Nexus")).thenReturn(true);

        boolean result = projectService.existsByName("MMCS Nexus");

        assertTrue(result);

        verify(projectRepository).existsByName("MMCS Nexus");
    }

    @Test
    void shouldReturnFalseWhenProjectDoesNotExistByName() {
        when(projectRepository.existsByName("Unknown")).thenReturn(false);

        boolean result = projectService.existsByName("Unknown");

        assertFalse(result);

        verify(projectRepository).existsByName("Unknown");
    }

    private Project createProject() {
        Project project = new Project();

        project.setId(UUID.randomUUID());
        project.setExternalId(1000);
        project.setQuantityOfStudents(3);
        project.setCaptainName("Старый капитан");
        project.setFull(false);
        project.setTrack("Old track");
        project.setTechnologies("Old stack");
        project.setName("Старый проект");
        project.setDescription("Старое описание");
        project.setType("DESKTOP_APP");
        project.setYear(2025);

        return project;
    }

    private CreateProjectRequestPayload createPayload(String name, String description, int year) {
        CreateProjectRequestPayload payload = new CreateProjectRequestPayload();

        payload.setExternalId(1001);
        payload.setQuantityOfStudents(4);
        payload.setCaptainName("Иван Иванов");
        payload.setFull(true);
        payload.setTrack("Backend");
        payload.setTechnologies("Java, Spring");
        payload.setName(name);
        payload.setDescription(description);
        payload.setType("WEB_APP");
        payload.setYear(year);

        return payload;
    }
}