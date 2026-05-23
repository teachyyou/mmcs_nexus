package ru.sfedu.mmcs_nexus.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_uuid")
    private User author;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "preview_text", columnDefinition = "text")
    private String previewText;

    @Column(name = "content_html", nullable = false, columnDefinition = "text")
    private String contentHtml;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banner_file_id")
    private UploadedFile bannerFile;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public Post() {
    }

    public Post(
            String title,
            String previewText,
            String contentHtml,
            UploadedFile bannerFile,
            boolean isPublished,
            User author,
            LocalDateTime publishedAt
    ) {
        this.title = title;
        this.previewText = previewText;
        this.contentHtml = contentHtml;
        this.bannerFile = bannerFile;
        this.isPublished = isPublished;
        this.author = author;
        this.publishedAt = publishedAt;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}