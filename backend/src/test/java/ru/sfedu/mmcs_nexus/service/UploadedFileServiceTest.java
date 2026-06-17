package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.sfedu.mmcs_nexus.model.dto.internal.StoredFile;
import ru.sfedu.mmcs_nexus.model.entity.UploadedFile;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileResponsePayload;
import ru.sfedu.mmcs_nexus.repository.UploadedFileRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadedFileServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private UploadedFileService uploadedFileService;

    @Test
    void shouldFindUploadedFileById() {
        UploadedFile file = createUploadedFile();

        when(uploadedFileRepository.findById(file.getId())).thenReturn(Optional.of(file));

        UploadedFile result = uploadedFileService.find(file.getId().toString());

        assertEquals(file, result);

        verify(uploadedFileRepository).findById(file.getId());
    }

    @Test
    void shouldThrowWhenUploadedFileNotFound() {
        UUID fileId = UUID.randomUUID();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> uploadedFileService.find(fileId.toString())
        );

        assertEquals("File with id " + fileId + " not found", exception.getMessage());

        verify(uploadedFileRepository).findById(fileId);
    }

    @Test
    void shouldReturnResourceByFileId() {
        UploadedFile file = createUploadedFile();
        Resource resource = new ByteArrayResource("image-content".getBytes());

        when(uploadedFileRepository.findById(file.getId())).thenReturn(Optional.of(file));
        when(fileStorageService.get(file.getStoragePath())).thenReturn(resource);

        Resource result = uploadedFileService.getResource(file.getId());

        assertEquals(resource, result);

        verify(uploadedFileRepository).findById(file.getId());
        verify(fileStorageService).get(file.getStoragePath());
    }

    @Test
    void shouldUploadImageFile() {
        UUID savedFileId = UUID.randomUUID();
        User user = createUser();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "banner.png",
                MediaType.IMAGE_PNG_VALUE,
                "image-content".getBytes()
        );

        UploadFileRequestPayload payload = new UploadFileRequestPayload();
        payload.setFile(multipartFile);

        when(fileStorageService.save(multipartFile)).thenReturn(new StoredFile("posts/generated.png"));
        when(userService.findByGithubLogin("teachyyou")).thenReturn(Optional.of(user));
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenAnswer(invocation -> {
            UploadedFile uploadedFile = invocation.getArgument(0);
            uploadedFile.setId(savedFileId);
            return uploadedFile;
        });

        UploadFileResponsePayload result = uploadedFileService.upload(payload, "teachyyou");

        assertEquals(savedFileId, result.getUploadId());

        ArgumentCaptor<UploadedFile> uploadedFileCaptor = ArgumentCaptor.forClass(UploadedFile.class);

        verify(uploadedFileRepository).save(uploadedFileCaptor.capture());

        UploadedFile uploadedFile = uploadedFileCaptor.getValue();

        assertEquals("banner.png", uploadedFile.getOriginalFilename());
        assertEquals("posts/generated.png", uploadedFile.getStoragePath());
        assertEquals(MediaType.IMAGE_PNG_VALUE, uploadedFile.getContentType());
        assertEquals(multipartFile.getSize(), uploadedFile.getSizeBytes());
        assertEquals(user, uploadedFile.getUploadedBy());
        assertFalse(uploadedFile.isAttached());

        verify(fileStorageService, never()).delete(anyString());
    }

    @Test
    void shouldCleanOriginalFilenameWhenUploadingFile() {
        UUID savedFileId = UUID.randomUUID();
        User user = createUser();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "../banner.png",
                MediaType.IMAGE_PNG_VALUE,
                "image-content".getBytes()
        );

        UploadFileRequestPayload payload = new UploadFileRequestPayload();
        payload.setFile(multipartFile);

        when(fileStorageService.save(multipartFile)).thenReturn(new StoredFile("posts/generated.png"));
        when(userService.findByGithubLogin("teachyyou")).thenReturn(Optional.of(user));
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenAnswer(invocation -> {
            UploadedFile uploadedFile = invocation.getArgument(0);
            uploadedFile.setId(savedFileId);
            return uploadedFile;
        });

        uploadedFileService.upload(payload, "teachyyou");

        ArgumentCaptor<UploadedFile> uploadedFileCaptor = ArgumentCaptor.forClass(UploadedFile.class);

        verify(uploadedFileRepository).save(uploadedFileCaptor.capture());

        assertEquals("banner.png", uploadedFileCaptor.getValue().getOriginalFilename());
    }

    @Test
    void shouldDeleteStoredFileWhenDatabaseSaveFails() {
        User user = createUser();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "banner.png",
                MediaType.IMAGE_PNG_VALUE,
                "image-content".getBytes()
        );

        UploadFileRequestPayload payload = new UploadFileRequestPayload();
        payload.setFile(multipartFile);

        when(fileStorageService.save(multipartFile)).thenReturn(new StoredFile("posts/generated.png"));
        when(userService.findByGithubLogin("teachyyou")).thenReturn(Optional.of(user));
        when(uploadedFileRepository.save(any(UploadedFile.class)))
                .thenThrow(new RuntimeException("database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> uploadedFileService.upload(payload, "teachyyou")
        );

        assertEquals("database error", exception.getMessage());

        verify(fileStorageService).delete("posts/generated.png");
    }

    @Test
    void shouldDeleteStoredFileWhenUserNotFound() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "banner.png",
                MediaType.IMAGE_PNG_VALUE,
                "image-content".getBytes()
        );

        UploadFileRequestPayload payload = new UploadFileRequestPayload();
        payload.setFile(multipartFile);

        when(fileStorageService.save(multipartFile)).thenReturn(new StoredFile("posts/generated.png"));
        when(userService.findByGithubLogin("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> uploadedFileService.upload(payload, "unknown")
        );

        assertEquals("User not found", exception.getMessage());

        verify(fileStorageService).delete("posts/generated.png");
        verify(uploadedFileRepository, never()).save(any(UploadedFile.class));
    }

    @Test
    void shouldRejectMissingFile() {
        UploadFileRequestPayload payload = new UploadFileRequestPayload();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> uploadedFileService.upload(payload, "teachyyou")
        );

        assertEquals("File is required", exception.getMessage());

        verify(fileStorageService, never()).save(any());
        verify(uploadedFileRepository, never()).save(any());
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "empty.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[0]
        );

        UploadFileRequestPayload payload = new UploadFileRequestPayload();
        payload.setFile(multipartFile);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> uploadedFileService.upload(payload, "teachyyou")
        );

        assertEquals("File is empty", exception.getMessage());

        verify(fileStorageService, never()).save(any());
        verify(uploadedFileRepository, never()).save(any());
    }

    @Test
    void shouldRejectFileWithoutContentType() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "banner",
                null,
                "image-content".getBytes()
        );

        UploadFileRequestPayload payload = new UploadFileRequestPayload();
        payload.setFile(multipartFile);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> uploadedFileService.upload(payload, "teachyyou")
        );

        assertEquals("Only image files are allowed", exception.getMessage());

        verify(fileStorageService, never()).save(any());
        verify(uploadedFileRepository, never()).save(any());
    }

    @Test
    void shouldRejectNonImageFile() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "pdf-content".getBytes()
        );

        UploadFileRequestPayload payload = new UploadFileRequestPayload();
        payload.setFile(multipartFile);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> uploadedFileService.upload(payload, "teachyyou")
        );

        assertEquals("Only image files are allowed", exception.getMessage());

        verify(fileStorageService, never()).save(any());
        verify(uploadedFileRepository, never()).save(any());
    }

    @Test
    void shouldSetFileAttached() {
        UploadedFile file = createUploadedFile();
        file.setAttached(false);

        when(uploadedFileRepository.findById(file.getId())).thenReturn(Optional.of(file));

        uploadedFileService.setAttached(file.getId());

        assertTrue(file.isAttached());

        verify(uploadedFileRepository).save(file);
    }

    @Test
    void shouldThrowWhenSettingAttachedForUnknownFile() {
        UUID fileId = UUID.randomUUID();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> uploadedFileService.setAttached(fileId)
        );

        assertEquals("File with id " + fileId + " not found", exception.getMessage());

        verify(uploadedFileRepository, never()).save(any(UploadedFile.class));
    }

    private UploadedFile createUploadedFile() {
        UploadedFile file = new UploadedFile();

        file.setId(UUID.randomUUID());
        file.setOriginalFilename("banner.png");
        file.setStoragePath("posts/generated.png");
        file.setContentType(MediaType.IMAGE_PNG_VALUE);
        file.setSizeBytes(1024L);
        file.setAttached(false);
        file.setUploadedBy(createUser());

        return file;
    }

    private User createUser() {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setLogin("teachyyou");

        return user;
    }
}