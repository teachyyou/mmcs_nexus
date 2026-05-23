package ru.sfedu.mmcs_nexus.controller.v1.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.model.entity.UploadedFile;
import ru.sfedu.mmcs_nexus.service.UploadedFileService;

import java.util.UUID;

@RestController
public class MediaController {

    private final UploadedFileService uploadedFileService;

    @Autowired
    public MediaController(UploadedFileService uploadedFileService) {
        this.uploadedFileService = uploadedFileService;
    }

    @GetMapping("/api/v1/media/image/{id}")
    public ResponseEntity<Resource> getImage(@PathVariable("id") UUID imageId) {
        Resource image = uploadedFileService.getResource(imageId);
        UploadedFile uploadedFile = uploadedFileService.find(imageId.toString());

        if (uploadedFile.getContentType() == null || !uploadedFile.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("File is not an image");
        }

        MediaType mediaType = MediaType.parseMediaType(uploadedFile.getContentType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(image);
    }
}
