package ru.sfedu.mmcs_nexus.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.sfedu.mmcs_nexus.model.dto.internal.StoredFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadsRootPath;
    private final Path postsDirectoryPath;

    public FileStorageService(@Value("${app.uploads-dir}") String uploadsDir) {
        this.uploadsRootPath = Paths.get(uploadsDir).toAbsolutePath().normalize();
        this.postsDirectoryPath = this.uploadsRootPath.resolve("posts").normalize();
    }

    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(postsDirectoryPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize uploads directory", ex);
        }
    }

    public StoredFile save(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = extractFileExtension(originalFilename);
        String generatedFilename = UUID.randomUUID() + fileExtension;

        Path targetFilePath = postsDirectoryPath.resolve(generatedFilename).normalize();
        if (!targetFilePath.startsWith(postsDirectoryPath)) {
            throw new IllegalArgumentException("Invalid target file path");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to save file", ex);
        }

        String storagePath = "posts/" + generatedFilename;
        return new StoredFile(storagePath);
    }

    public Resource get(String storagePath) {
        Path filePath = resolveStoragePath(storagePath);

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("File not found or not readable");
            }

            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Invalid file path", ex);
        }
    }

    public void delete(String storagePath) {
        Path filePath = resolveStoragePath(storagePath);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete file", ex);
        }
    }

    private Path resolveStoragePath(String storagePath) {
        Path resolvedPath = uploadsRootPath.resolve(storagePath).normalize();
        if (!resolvedPath.startsWith(uploadsRootPath)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        return resolvedPath;
    }

    private String extractFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex);
    }
}