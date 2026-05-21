package ru.sfedu.mmcs_nexus.controller.v1.admin;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileResponsePayload;
import ru.sfedu.mmcs_nexus.service.UploadedFileService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminFileUploadController.class)
@AutoConfigureMockMvc
class AdminFileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UploadedFileService uploadedFileService;

    @Test
    void shouldUploadFile() throws Exception {
        UUID uploadId = UUID.randomUUID();

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile(
                        "file",
                        "banner.png",
                        MediaType.IMAGE_PNG_VALUE,
                        "image-content".getBytes()
                );

        when(uploadedFileService.upload(
                org.mockito.ArgumentMatchers.any(UploadFileRequestPayload.class),
                org.mockito.ArgumentMatchers.eq("teachyyou")
        )).thenReturn(new UploadFileResponsePayload(uploadId));

        mockMvc.perform(multipart("/api/v1/admin/upload")
                        .file(file)
                        .with(csrf())
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uploadId").value(uploadId.toString()));

        ArgumentCaptor<UploadFileRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(UploadFileRequestPayload.class);

        verify(uploadedFileService).upload(payloadCaptor.capture(), org.mockito.ArgumentMatchers.eq("teachyyou"));

        MultipartFile capturedFile = payloadCaptor.getValue().getFile();

        assertNotNull(capturedFile);
        assertEquals("banner.png", capturedFile.getOriginalFilename());
        assertEquals(MediaType.IMAGE_PNG_VALUE, capturedFile.getContentType());
    }

    @Test
    void shouldReturnBadRequestWhenServiceRejectsEmptyFile() throws Exception {
        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile(
                        "file",
                        "empty.png",
                        MediaType.IMAGE_PNG_VALUE,
                        new byte[0]
                );

        when(uploadedFileService.upload(
                org.mockito.ArgumentMatchers.any(UploadFileRequestPayload.class),
                org.mockito.ArgumentMatchers.eq("teachyyou")
        )).thenThrow(new IllegalArgumentException("File is empty"));

        mockMvc.perform(multipart("/api/v1/admin/upload")
                        .file(file)
                        .with(csrf())
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File is empty"));
    }

    @Test
    void shouldReturnBadRequestWhenServiceRejectsNonImageFile() throws Exception {
        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile(
                        "file",
                        "document.pdf",
                        MediaType.APPLICATION_PDF_VALUE,
                        "pdf-content".getBytes()
                );

        when(uploadedFileService.upload(
                org.mockito.ArgumentMatchers.any(UploadFileRequestPayload.class),
                org.mockito.ArgumentMatchers.eq("teachyyou")
        )).thenThrow(new IllegalArgumentException("Only image files are allowed"));

        mockMvc.perform(multipart("/api/v1/admin/upload")
                        .file(file)
                        .with(csrf())
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only image files are allowed"));
    }
}