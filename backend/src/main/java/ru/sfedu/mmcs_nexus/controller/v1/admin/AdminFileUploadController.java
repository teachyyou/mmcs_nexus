package ru.sfedu.mmcs_nexus.controller.v1.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.payload.admin.ImportResponsePayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileResponsePayload;
import ru.sfedu.mmcs_nexus.service.UploadedFileService;

@RestController
public class AdminFileUploadController {

    private UploadedFileService uploadedFileService;

    @Autowired
    public AdminFileUploadController(UploadedFileService uploadedFileService) {
        this.uploadedFileService = uploadedFileService;
    }

    @PostMapping(value="api/v1/admin/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json")
    public ResponseEntity<?> importProjectsFromCsv(
            @ModelAttribute UploadFileRequestPayload payload
    ) {
        UploadFileResponsePayload result = uploadedFileService.upload(payload);

        return ResponseEntity.ok(result);
    }

}
