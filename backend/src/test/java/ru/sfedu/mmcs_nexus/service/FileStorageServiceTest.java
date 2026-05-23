package ru.sfedu.mmcs_nexus.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void shouldInitializePostsDirectory() {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());

        fileStorageService.initialize();

        assertTrue(Files.exists(tempDir.resolve("posts")));
        assertTrue(Files.isDirectory(tempDir.resolve("posts")));
    }

    @Test
    void shouldSaveFileToPostsDirectory() throws Exception {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initialize();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "banner.png",
                "image/png",
                "image-content".getBytes()
        );

        ru.sfedu.mmcs_nexus.model.dto.internal.StoredFile storedFile = fileStorageService.save(file);

        assertNotNull(storedFile);
        assertTrue(storedFile.getStoragePath().startsWith("posts/"));
        assertTrue(storedFile.getStoragePath().endsWith(".png"));

        Path savedPath = tempDir.resolve(storedFile.getStoragePath());

        assertTrue(Files.exists(savedPath));
        assertEquals("image-content", Files.readString(savedPath));
    }

    @Test
    void shouldSaveFileWithoutExtension() throws Exception {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initialize();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "banner",
                "image/png",
                "image-content".getBytes()
        );

        ru.sfedu.mmcs_nexus.model.dto.internal.StoredFile storedFile = fileStorageService.save(file);

        assertNotNull(storedFile);
        assertTrue(storedFile.getStoragePath().startsWith("posts/"));

        String filename = storedFile.getStoragePath().substring("posts/".length());

        assertFalse(filename.contains("."));

        Path savedPath = tempDir.resolve(storedFile.getStoragePath());

        assertTrue(Files.exists(savedPath));
    }

    @Test
    void shouldGetSavedFileAsResource() throws Exception {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initialize();

        Path postsDir = tempDir.resolve("posts");
        Path savedFile = postsDir.resolve("banner.png");
        Files.writeString(savedFile, "image-content");

        Resource resource = fileStorageService.get("posts/banner.png");

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        try (InputStream inputStream = resource.getInputStream()) {
            assertEquals("image-content", new String(inputStream.readAllBytes()));
        }
    }

    @Test
    void shouldThrowWhenGettingMissingFile() {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initialize();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> fileStorageService.get("posts/missing.png")
        );

        assertEquals("File not found or not readable", exception.getMessage());
    }

    @Test
    void shouldDeleteExistingFile() throws Exception {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initialize();

        Path postsDir = tempDir.resolve("posts");
        Path savedFile = postsDir.resolve("banner.png");
        Files.writeString(savedFile, "image-content");

        assertTrue(Files.exists(savedFile));

        fileStorageService.delete("posts/banner.png");

        assertFalse(Files.exists(savedFile));
    }

    @Test
    void shouldIgnoreDeletingMissingFile() {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initialize();

        assertDoesNotThrow(() -> fileStorageService.delete("posts/missing.png"));
    }

    @Test
    void shouldRejectPathTraversalWhenGettingFile() {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initialize();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.get("../secret.txt")
        );

        assertEquals("Invalid storage path", exception.getMessage());
    }

    @Test
    void shouldRejectPathTraversalWhenDeletingFile() {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initialize();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.delete("../secret.txt")
        );

        assertEquals("Invalid storage path", exception.getMessage());
    }

    @Test
    void shouldGenerateDifferentNamesForFilesWithSameOriginalFilename() {
        FileStorageService fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initialize();

        MockMultipartFile firstFile = new MockMultipartFile(
                "file",
                "banner.png",
                "image/png",
                "first-content".getBytes()
        );

        MockMultipartFile secondFile = new MockMultipartFile(
                "file",
                "banner.png",
                "image/png",
                "second-content".getBytes()
        );

        ru.sfedu.mmcs_nexus.model.dto.internal.StoredFile firstStoredFile = fileStorageService.save(firstFile);
        ru.sfedu.mmcs_nexus.model.dto.internal.StoredFile secondStoredFile = fileStorageService.save(secondFile);

        assertNotEquals(firstStoredFile.getStoragePath(), secondStoredFile.getStoragePath());

        assertTrue(Files.exists(tempDir.resolve(firstStoredFile.getStoragePath())));
        assertTrue(Files.exists(tempDir.resolve(secondStoredFile.getStoragePath())));
    }
}