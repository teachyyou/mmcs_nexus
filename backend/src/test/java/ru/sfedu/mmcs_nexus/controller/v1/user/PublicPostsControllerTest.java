package ru.sfedu.mmcs_nexus.controller.v1.user;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.model.dto.entity.PostDTO;
import ru.sfedu.mmcs_nexus.model.entity.Post;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.service.PostService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicPostsController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicPostsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    void shouldReturnPublishedPostsList() throws Exception {
        Post post = createPost();
        PostDTO postDTO = new PostDTO(post);

        when(postService.findAllPublished(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(postDTO)));

        mockMvc.perform(get("/api/v1/public/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(post.getId().toString()))
                .andExpect(jsonPath("$.content[0].title").value("Тестовая новость"))
                .andExpect(jsonPath("$.content[0].previewText").value("Краткое описание"))
                .andExpect(jsonPath("$.content[0].contentHtml").value("<p>Содержимое новости</p>"))
                .andExpect(jsonPath("$.content[0].published").value(true))
                .andExpect(jsonPath("$.content[0].author").value("teachyyou"))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(postService).findAllPublished(isNull(), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(10, paginationPayload.getLimit());
        assertEquals(0, paginationPayload.getOffset());
        assertEquals("publishedAt", paginationPayload.getSort());
        assertEquals("desc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnEmptyPublishedPostsList() throws Exception {
        when(postService.findAllPublished(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/public/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(postService).findAllPublished(isNull(), any(PaginationPayload.class));
    }

    @Test
    void shouldReturnSeveralPublishedPosts() throws Exception {
        Post firstPost = createPost("Первая новость", "Первый анонс", "firstauthor");
        Post secondPost = createPost("Вторая новость", "Второй анонс", "secondauthor");

        when(postService.findAllPublished(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(new PostDTO(firstPost), new PostDTO(secondPost))));

        mockMvc.perform(get("/api/v1/public/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title").value("Первая новость"))
                .andExpect(jsonPath("$.content[0].previewText").value("Первый анонс"))
                .andExpect(jsonPath("$.content[0].author").value("firstauthor"))
                .andExpect(jsonPath("$.content[1].title").value("Вторая новость"))
                .andExpect(jsonPath("$.content[1].previewText").value("Второй анонс"))
                .andExpect(jsonPath("$.content[1].author").value("secondauthor"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldPassQueryParamsToService() throws Exception {
        when(postService.findAllPublished(any(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/public/posts")
                        .param("limit", "5")
                        .param("offset", "10")
                        .param("sort", "createdAt")
                        .param("order", "asc")
                        .param("year", "2026"))
                .andExpect(status().isOk());

        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(postService).findAllPublished(yearCaptor.capture(), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(2026, yearCaptor.getValue());
        assertEquals(5, paginationPayload.getLimit());
        assertEquals(10, paginationPayload.getOffset());
        assertEquals("createdAt", paginationPayload.getSort());
        assertEquals("asc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/posts")
                        .param("limit", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOffsetIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/posts")
                        .param("offset", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenYearIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/posts")
                        .param("year", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnPublishedPostById() throws Exception {
        Post post = createPost();

        when(postService.findPublished(post.getId().toString()))
                .thenReturn(post);

        mockMvc.perform(get("/api/v1/public/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(post.getId().toString()))
                .andExpect(jsonPath("$.title").value("Тестовая новость"))
                .andExpect(jsonPath("$.previewText").value("Краткое описание"))
                .andExpect(jsonPath("$.contentHtml").value("<p>Содержимое новости</p>"))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.author").value("teachyyou"));

        verify(postService).findPublished(post.getId().toString());
    }

    @Test
    void shouldReturnBadRequestWhenPostIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/public/posts/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenPostDoesNotExist() throws Exception {
        String postId = UUID.randomUUID().toString();

        when(postService.findPublished(postId))
                .thenThrow(new EntityNotFoundException("Post with id " + postId + " not found"));

        mockMvc.perform(get("/api/v1/public/posts/{id}", postId))
                .andExpect(status().isNotFound());
    }

    private Post createPost() {
        return createPost("Тестовая новость", "Краткое описание", "teachyyou");
    }

    private Post createPost(String title, String previewText, String authorLogin) {
        User author = new User();
        author.setLogin(authorLogin);

        Post post = new Post();

        post.setId(UUID.randomUUID());
        post.setTitle(title);
        post.setPreviewText(previewText);
        post.setContentHtml("<p>Содержимое новости</p>");
        post.setPublished(true);
        post.setPublishedAt(LocalDateTime.of(2026, 5, 20, 12, 0));
        post.setCreatedAt(LocalDateTime.of(2026, 5, 20, 10, 0));
        post.setUpdatedAt(LocalDateTime.of(2026, 5, 20, 11, 0));
        post.setAuthor(author);

        return post;
    }
}