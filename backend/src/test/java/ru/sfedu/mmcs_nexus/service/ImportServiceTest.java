package ru.sfedu.mmcs_nexus.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartException;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.payload.admin.ImportResponsePayload;
import ru.sfedu.mmcs_nexus.repository.ProjectRepository;

import java.nio.charset.StandardCharsets;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ImportService importService;

    @Test
    void shouldImportValidCsvFile() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                id,quantityOfStudents,captainFio,track,technologies,name,projectDescription,projectType,isFull
                1001,4,Иван Иванов,Backend,Java Spring,MMCS Nexus,Описание проекта,web,true
                """
        );

        when(projectRepository.existsByName("MMCS Nexus")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 10);

        assertEquals(1, result.created().size());
        assertEquals(0, result.skipped().size());

        Project createdProject = result.created().getFirst();

        assertEquals(1001, createdProject.getExternalId());
        assertEquals(4, createdProject.getQuantityOfStudents());
        assertEquals("Иван Иванов", createdProject.getCaptainName());
        assertTrue(createdProject.isFull());
        assertEquals("Backend", createdProject.getTrack());
        assertEquals("Java Spring", createdProject.getTechnologies());
        assertEquals("MMCS Nexus", createdProject.getName());
        assertEquals("Описание проекта", createdProject.getDescription());
        assertEquals("WEB_APP", createdProject.getType());
        assertEquals(Year.now().getValue(), createdProject.getYear());

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

        verify(projectRepository).save(projectCaptor.capture());

        assertEquals("MMCS Nexus", projectCaptor.getValue().getName());
    }

    @Test
    void shouldImportCsvFileByCsvExtensionEvenWhenContentTypeIsNull() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projects.csv",
                null,
                """
                name,projectType,captainFio,track
                MMCS Nexus,web,Иван Иванов,Backend
                """.getBytes(StandardCharsets.UTF_8)
        );

        when(projectRepository.existsByName("MMCS Nexus")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 10);

        assertEquals(1, result.created().size());
        assertEquals("MMCS Nexus", result.created().getFirst().getName());
    }

    @Test
    void shouldRejectNonCsvFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projects.txt",
                "text/plain",
                "content".getBytes(StandardCharsets.UTF_8)
        );

        MultipartException exception = assertThrows(
                MultipartException.class,
                () -> importService.ImportProjectsFromCsv(file, 10)
        );

        assertEquals("Only CSV files are supported", exception.getMessage());

        verify(projectRepository, never()).save(any(Project.class));
    }
    @Test
    void shouldSkipBadRecordWhenRequiredColumnIsMissing() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                name,projectType,captainFio,track
                ,web,Иван Иванов,Backend
                """
        );

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 10);

        assertEquals(0, result.created().size());
        assertEquals(1, result.skipped().size());
        assertEquals("bad_record", result.skipped().getFirst().code());
        assertEquals("Required column 'name' is missing or empty", result.skipped().getFirst().message());

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void shouldSkipBadRecordWhenProjectTypeIsUnknown() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                name,projectType,captainFio,track
                Unknown Type Project,unknown,Иван Иванов,Backend
                """
        );

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 10);

        assertEquals(0, result.created().size());
        assertEquals(1, result.skipped().size());
        assertEquals("bad_record", result.skipped().getFirst().code());
        assertEquals("Unknown projectType value: unknown", result.skipped().getFirst().message());

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void shouldSkipBadRecordWhenIntegerIsInvalid() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                id,quantityOfStudents,captainFio,track,name,projectType
                not-number,4,Иван Иванов,Backend,MMCS Nexus,web
                """
        );

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 10);

        assertEquals(0, result.created().size());
        assertEquals(1, result.skipped().size());
        assertEquals("bad_record", result.skipped().getFirst().code());
        assertEquals("Invalid integer in 'externalId': not-number", result.skipped().getFirst().message());

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void shouldSkipBadRecordWhenBooleanIsInvalid() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                name,projectType,captainFio,track,isFull
                MMCS Nexus,web,Иван Иванов,Backend,maybe
                """
        );

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 10);

        assertEquals(0, result.created().size());
        assertEquals(1, result.skipped().size());
        assertEquals("bad_record", result.skipped().getFirst().code());
        assertEquals("Invalid boolean value: maybe", result.skipped().getFirst().message());

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void shouldRespectImportLimit() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                name,projectType,captainFio,track
                First,web,Иван Иванов,Backend
                Second,bot,Пётр Петров,Bots
                Third,mobile,Сергей Сергеев,Mobile
                """
        );

        when(projectRepository.existsByName(anyString())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 2);

        assertEquals(2, result.created().size());
        assertEquals("First", result.created().get(0).getName());
        assertEquals("Second", result.created().get(1).getName());

        verify(projectRepository, times(2)).save(any(Project.class));
    }

    @Test
    void shouldImportAllRowsWhenLimitIsZero() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                name,projectType,captainFio,track
                First,web,Иван Иванов,Backend
                Second,bot,Пётр Петров,Bots
                """
        );

        when(projectRepository.existsByName(anyString())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 0);

        assertEquals(2, result.created().size());

        verify(projectRepository, times(2)).save(any(Project.class));
    }

    @Test
    void shouldUseColumnAliases() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                name,projectType,captain_name,track,quantity_of_students,is_full
                MMCS Nexus,telegrambot,Иван Иванов,Bots,5,да
                """
        );

        when(projectRepository.existsByName("MMCS Nexus")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 10);

        Project project = result.created().getFirst();

        assertEquals("MMCS Nexus", project.getName());
        assertEquals("TELEGRAM_BOT", project.getType());
        assertEquals("Иван Иванов", project.getCaptainName());
        assertEquals(5, project.getQuantityOfStudents());
        assertTrue(project.isFull());
    }

    @Test
    void shouldSkipRowWhenDatabaseConstraintFails() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                name,projectType,captainFio,track
                MMCS Nexus,web,Иван Иванов,Backend
                """
        );

        when(projectRepository.existsByName("MMCS Nexus")).thenReturn(false);
        when(projectRepository.save(any(Project.class)))
                .thenThrow(new DataIntegrityViolationException("constraint error"));

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 10);

        assertEquals(0, result.created().size());
        assertEquals(1, result.skipped().size());
        assertEquals("db_constraint", result.skipped().getFirst().code());
        assertTrue(result.skipped().getFirst().message().contains("constraint error"));
    }

    @Test
    void shouldReturnReadErrorWhenCsvCannotBeParsed() {
        MockMultipartFile file = csvFile(
                "projects.csv",
                """
                name,projectType,captainFio,track
                "broken,web,Иван Иванов,Backend
                """
        );

        ImportResponsePayload<Project> result = importService.ImportProjectsFromCsv(file, 10);

        assertEquals(0, result.created().size());
        assertEquals(1, result.skipped().size());
        assertEquals(0, result.skipped().getFirst().rowNumber());
        assertEquals("read_error", result.skipped().getFirst().code());

        verify(projectRepository, never()).save(any(Project.class));
    }

    private MockMultipartFile csvFile(String filename, String content) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}