package ru.sfedu.mmcs_nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileResponsePayload;
import ru.sfedu.mmcs_nexus.repository.UploadedFileRepository;

@Service
public class UploadedFileService {

    private final FileStorageService fileStorageService;
    private final UploadedFileRepository uploadedFileRepository;

    @Autowired
    public UploadedFileService(FileStorageService fileStorageService, UploadedFileRepository uploadedFileRepository) {
        this.fileStorageService = fileStorageService;
        this.uploadedFileRepository = uploadedFileRepository;
    }


    public UploadFileResponsePayload upload(UploadFileRequestPayload payload) {

    }
}
