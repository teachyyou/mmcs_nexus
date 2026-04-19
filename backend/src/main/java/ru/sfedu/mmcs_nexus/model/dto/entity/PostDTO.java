package ru.sfedu.mmcs_nexus.model.dto.entity;

import lombok.Getter;
import lombok.Setter;
import ru.sfedu.mmcs_nexus.model.entity.Post;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class PostDTO {

    private UUID id;
    private String title;
    private String previewText;
    private String contentHtml;
    private boolean published;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String bannerUrl;
    private String author;

    public PostDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.previewText = post.getPreviewText();
        this.contentHtml = post.getContentHtml();
        this.published = post.isPublished();
        this.publishedAt = post.getPublishedAt();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        this.bannerUrl = post.getBannerFile() != null
                ? "/api/v1/media/image/" + post.getBannerFile().getId()
                : null;

        this.author = post.getAuthor() != null
                ? post.getAuthor().getLogin()
                : null;
    }
}