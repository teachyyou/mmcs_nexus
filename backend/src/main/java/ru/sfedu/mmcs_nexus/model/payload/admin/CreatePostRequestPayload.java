package ru.sfedu.mmcs_nexus.model.payload.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class CreatePostRequestPayload {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255)
    private String title;

    private String previewText;

    @NotBlank(message = "Content HTML must not be blank")
    private String contentHtml;

    @NotNull(message = "Banner file id is required")
    private UUID bannerFileId;

    private Boolean published;

}