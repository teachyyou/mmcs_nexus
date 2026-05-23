package ru.sfedu.mmcs_nexus.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import ru.sfedu.mmcs_nexus.model.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:posts_repository_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS \"text\" AS CLOB",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions=true"
})
class PostsRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void shouldFindAllPostsByYear() {
        Post post2025 = createPost(
                "Новость 2025",
                true,
                LocalDateTime.of(2025, 5, 20, 10, 0),
                LocalDateTime.of(2025, 5, 20, 12, 0)
        );

        Post post2026 = createPost(
                "Новость 2026",
                true,
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 20, 12, 0)
        );

        postRepository.saveAll(List.of(post2025, post2026));

        Page<Post> result = postRepository.findAllByYear(
                2026,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"))
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Новость 2026", result.getContent().getFirst().getTitle());
    }

    @Test
    void shouldReturnEmptyPageWhenYearDoesNotMatch() {
        Post post = createPost(
                "Новость 2026",
                true,
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 20, 12, 0)
        );

        postRepository.save(post);

        Page<Post> result = postRepository.findAllByYear(
                2024,
                PageRequest.of(0, 10)
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void shouldFindOnlyPublishedPosts() {
        Post publishedPost = createPost(
                "Опубликованная новость",
                true,
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 20, 12, 0)
        );

        Post draftPost = createPost(
                "Черновик",
                false,
                LocalDateTime.of(2026, 5, 21, 10, 0),
                null
        );

        postRepository.saveAll(List.of(publishedPost, draftPost));

        Page<Post> result = postRepository.findAllPublished(
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Опубликованная новость", result.getContent().getFirst().getTitle());
        assertTrue(result.getContent().getFirst().isPublished());
    }

    @Test
    void shouldFindOnlyPublishedPostsByYear() {
        Post publishedPost2025 = createPost(
                "Опубликованная новость 2025",
                true,
                LocalDateTime.of(2025, 5, 20, 10, 0),
                LocalDateTime.of(2025, 5, 20, 12, 0)
        );

        Post publishedPost2026 = createPost(
                "Опубликованная новость 2026",
                true,
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 20, 12, 0)
        );

        Post draftPost2026 = createPost(
                "Черновик 2026",
                false,
                LocalDateTime.of(2026, 5, 21, 10, 0),
                null
        );

        postRepository.saveAll(List.of(publishedPost2025, publishedPost2026, draftPost2026));

        Page<Post> result = postRepository.findAllPublishedByYear(
                2026,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Опубликованная новость 2026", result.getContent().getFirst().getTitle());
        assertTrue(result.getContent().getFirst().isPublished());
    }

    @Test
    void shouldApplyPageableSortingForPublishedPosts() {
        Post olderPost = createPost(
                "Старая новость",
                true,
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 20, 12, 0)
        );

        Post newerPost = createPost(
                "Новая новость",
                true,
                LocalDateTime.of(2026, 5, 21, 10, 0),
                LocalDateTime.of(2026, 5, 21, 12, 0)
        );

        postRepository.saveAll(List.of(olderPost, newerPost));

        Page<Post> result = postRepository.findAllPublished(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishedAt"))
        );

        assertEquals(2, result.getTotalElements());
        assertEquals("Новая новость", result.getContent().get(0).getTitle());
        assertEquals("Старая новость", result.getContent().get(1).getTitle());
    }

    @Test
    void shouldApplyPaginationForPublishedPosts() {
        Post firstPost = createPost(
                "Первая новость",
                true,
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 20, 12, 0)
        );

        Post secondPost = createPost(
                "Вторая новость",
                true,
                LocalDateTime.of(2026, 5, 21, 10, 0),
                LocalDateTime.of(2026, 5, 21, 12, 0)
        );

        Post thirdPost = createPost(
                "Третья новость",
                true,
                LocalDateTime.of(2026, 5, 22, 10, 0),
                LocalDateTime.of(2026, 5, 22, 12, 0)
        );

        postRepository.saveAll(List.of(firstPost, secondPost, thirdPost));

        Page<Post> result = postRepository.findAllPublished(
                PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "publishedAt"))
        );

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Вторая новость", result.getContent().getFirst().getTitle());
    }

    private Post createPost(
            String title,
            boolean published,
            LocalDateTime createdAt,
            LocalDateTime publishedAt
    ) {
        Post post = new Post();

        post.setTitle(title);
        post.setPreviewText("Краткое описание");
        post.setContentHtml("<p>Содержимое новости</p>");
        post.setPublished(published);
        post.setPublishedAt(publishedAt);
        post.setCreatedAt(createdAt);
        post.setUpdatedAt(createdAt.plusHours(1));

        return post;
    }
}