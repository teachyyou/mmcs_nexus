package ru.sfedu.mmcs_nexus.controller.v1.media;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.model.entity.UploadedFile;
import ru.sfedu.mmcs_nexus.service.UploadedFileService;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(MediaController.class)
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UploadedFileService uploadedFileService;

    @Test
    void shouldReturnImageById() throws Exception {
        UUID imageId = UUID.randomUUID();

        Resource resource = new ByteArrayResource("image-content".getBytes());

        UploadedFile uploadedFile = createUploadedFile(
                imageId,
                "banner.png",
                "posts/banner.png",
                MediaType.IMAGE_PNG_VALUE
        );

        when(uploadedFileService.getResource(imageId)).thenReturn(resource);
        when(uploadedFileService.find(imageId.toString())).thenReturn(uploadedFile);

        mockMvc.perform(get("/api/v1/media/image/{id}", imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline"))
                .andExpect(content().bytes("image-content".getBytes()));

        verify(uploadedFileService).getResource(imageId);
        verify(uploadedFileService).find(imageId.toString());
    }

    @Test
    void shouldReturnJpegImageById() throws Exception {
        UUID imageId = UUID.randomUUID();

        Resource resource = new ByteArrayResource("jpeg-content".getBytes());

        UploadedFile uploadedFile = createUploadedFile(
                imageId,
                "photo.jpg",
                "posts/photo.jpg",
                MediaType.IMAGE_JPEG_VALUE
        );

        when(uploadedFileService.getResource(imageId)).thenReturn(resource);
        when(uploadedFileService.find(imageId.toString())).thenReturn(uploadedFile);

        mockMvc.perform(get("/api/v1/media/image/{id}", imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline"))
                .andExpect(content().bytes("jpeg-content".getBytes()));

        verify(uploadedFileService).getResource(imageId);
        verify(uploadedFileService).find(imageId.toString());
    }

    @Test
    void shouldReturnBadRequestWhenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/media/image/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenImageDoesNotExist() throws Exception {
        UUID imageId = UUID.randomUUID();

        when(uploadedFileService.getResource(imageId))
                .thenThrow(new EntityNotFoundException("File with id " + imageId + " not found"));

        mockMvc.perform(get("/api/v1/media/image/{id}", imageId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("File with id " + imageId + " not found"));

        verify(uploadedFileService).getResource(imageId);
    }

    @Test
    void shouldReturnBadRequestWhenFileIsNotImage() throws Exception {
        UUID fileId = UUID.randomUUID();

        Resource resource = new ByteArrayResource("pdf-content".getBytes());

        UploadedFile uploadedFile = createUploadedFile(
                fileId,
                "document.pdf",
                "posts/document.pdf",
                MediaType.APPLICATION_PDF_VALUE
        );

        when(uploadedFileService.getResource(fileId)).thenReturn(resource);
        when(uploadedFileService.find(fileId.toString())).thenReturn(uploadedFile);

        mockMvc.perform(get("/api/v1/media/image/{id}", fileId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File is not an image"));

        verify(uploadedFileService).getResource(fileId);
        verify(uploadedFileService).find(fileId.toString());
    }

    @Test
    void shouldReturnBadRequestWhenFileContentTypeIsNull() throws Exception {
        UUID fileId = UUID.randomUUID();

        Resource resource = new ByteArrayResource("file-content".getBytes());

        UploadedFile uploadedFile = createUploadedFile(
                fileId,
                "unknown",
                "posts/unknown",
                null
        );

        when(uploadedFileService.getResource(fileId)).thenReturn(resource);
        when(uploadedFileService.find(fileId.toString())).thenReturn(uploadedFile);

        mockMvc.perform(get("/api/v1/media/image/{id}", fileId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File is not an image"));

        verify(uploadedFileService).getResource(fileId);
        verify(uploadedFileService).find(fileId.toString());
    }

    private UploadedFile createUploadedFile(
            UUID id,
            String originalFilename,
            String storagePath,
            String contentType
    ) {
        UploadedFile uploadedFile = new UploadedFile();

        uploadedFile.setId(id);
        uploadedFile.setOriginalFilename(originalFilename);
        uploadedFile.setStoragePath(storagePath);
        uploadedFile.setContentType(contentType);
        uploadedFile.setSizeBytes(1024L);
        uploadedFile.setAttached(true);

        return uploadedFile;
    }
}