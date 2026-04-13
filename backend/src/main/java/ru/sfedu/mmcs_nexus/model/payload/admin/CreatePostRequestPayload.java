package ru.sfedu.mmcs_nexus.model.payload.admin;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

public class CreatePostRequestPayload {

    private String title;
    private String previewText;
    private String contentHtml;
    private String bannerPath;
    private boolean isPublished;
    private LocalDateTime publishedAt;

}
