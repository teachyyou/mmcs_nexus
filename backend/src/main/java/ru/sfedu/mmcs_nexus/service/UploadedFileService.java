package ru.sfedu.mmcs_nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.sfedu.mmcs_nexus.model.dto.internal.StoredFile;
import ru.sfedu.mmcs_nexus.model.entity.UploadedFile;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileResponsePayload;
import ru.sfedu.mmcs_nexus.repository.UploadedFileRepository;

@Service
public class UploadedFileService {

    private final FileStorageService fileStorageService;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserService userService;

    @Autowired
    public UploadedFileService(FileStorageService fileStorageService, UploadedFileRepository uploadedFileRepository, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.userService = userService;
    }

    public UploadFileResponsePayload upload(UploadFileRequestPayload payload, String userLogin) {
        MultipartFile file = payload.getFile();

        validateFile(file);

        StoredFile storedFile = fileStorageService.save(file);

        try {
            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));
            uploadedFile.setStoragePath(storedFile.getStoragePath());
            uploadedFile.setContentType(resolveContentType(file));
            uploadedFile.setUploadedBy(userService.findByGithubLogin(userLogin).orElseThrow(() -> new UsernameNotFoundException("User not found")));
            uploadedFile.setSizeBytes(file.getSize());
            uploadedFile.setAttached(false);

            UploadedFile savedUploadedFile = uploadedFileRepository.save(uploadedFile);

            String publicUrl = fileStorageService.toPublicUrl(savedUploadedFile.getStoragePath());

            return new UploadFileResponsePayload(
                    savedUploadedFile.getId(),
                    publicUrl
            );
        } catch (RuntimeException ex) {
            fileStorageService.delete(storedFile.getStoragePath());
            throw ex;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private String resolveContentType(MultipartFile file) {
        return file.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.getContentType();
    }
}