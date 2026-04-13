package ru.sfedu.mmcs_nexus.model.payload.admin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UploadFileRequestPayload {
    private MultipartFile file;
}
