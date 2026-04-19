package ru.sfedu.mmcs_nexus.controller.v1.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.UploadFileResponsePayload;
import ru.sfedu.mmcs_nexus.service.UploadedFileService;

@RestController
public class AdminFileUploadController {

    private final UploadedFileService uploadedFileService;

    @Autowired
    public AdminFileUploadController(UploadedFileService uploadedFileService) {
        this.uploadedFileService = uploadedFileService;
    }

    @PostMapping(value="api/v1/admin/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json")
    public ResponseEntity<?> uploadFile(
            @ModelAttribute UploadFileRequestPayload payload,
            @AuthenticationPrincipal OAuth2User user
    ) {
        String userLogin = user.getAttribute("login");
        UploadFileResponsePayload result = uploadedFileService.upload(payload, userLogin);

        return ResponseEntity.ok(result);
    }

}
