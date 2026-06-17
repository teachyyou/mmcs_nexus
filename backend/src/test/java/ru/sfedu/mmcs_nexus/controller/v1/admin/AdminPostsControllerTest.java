package ru.sfedu.mmcs_nexus.controller.v1.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.model.dto.entity.PostDTO;
import ru.sfedu.mmcs_nexus.model.entity.Post;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreatePostRequestPayload;
import ru.sfedu.mmcs_nexus.service.PostService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPostController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminPostsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @Test
    void shouldReturnPostsList() throws Exception {
        Post post = createPost();

        when(postService.findAll(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(new PostDTO(post))));

        mockMvc.perform(get("/api/v1/admin/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(post.getId().toString()))
                .andExpect(jsonPath("$.content[0].title").value("Тестовая новость"))
                .andExpect(jsonPath("$.content[0].previewText").value("Краткое описание"))
                .andExpect(jsonPath("$.content[0].contentHtml").value("<p>Содержимое новости</p>"))
                .andExpect(jsonPath("$.content[0].published").value(true))
                .andExpect(jsonPath("$.content[0].author").value("teachyyou"))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(postService).findAll(isNull(), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(10, paginationPayload.getLimit());
        assertEquals(0, paginationPayload.getOffset());
        assertEquals("id", paginationPayload.getSort());
        assertEquals("asc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnEmptyPostsList() throws Exception {
        when(postService.findAll(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(postService).findAll(isNull(), any(PaginationPayload.class));
    }

    @Test
    void shouldPassQueryParamsToService() throws Exception {
        when(postService.findAll(any(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/posts")
                        .param("limit", "5")
                        .param("offset", "10")
                        .param("sort", "createdAt")
                        .param("order", "desc")
                        .param("year", "2026"))
                .andExpect(status().isOk());

        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(postService).findAll(yearCaptor.capture(), paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(2026, yearCaptor.getValue());
        assertEquals(5, paginationPayload.getLimit());
        assertEquals(10, paginationPayload.getOffset());
        assertEquals("createdAt", paginationPayload.getSort());
        assertEquals("desc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnPostById() throws Exception {
        Post post = createPost();

        when(postService.find(post.getId().toString()))
                .thenReturn(post);

        mockMvc.perform(get("/api/v1/admin/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(post.getId().toString()))
                .andExpect(jsonPath("$.title").value("Тестовая новость"))
                .andExpect(jsonPath("$.previewText").value("Краткое описание"))
                .andExpect(jsonPath("$.contentHtml").value("<p>Содержимое новости</p>"))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.author").value("teachyyou"));

        verify(postService).find(post.getId().toString());
    }

    @Test
    void shouldEditPost() throws Exception {
        Post post = createPost();
        UUID bannerFileId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "title", "Обновлённая новость",
                "previewText", "Обновлённый анонс",
                "contentHtml", "<p>Обновлённый контент</p>",
                "bannerFileId", bannerFileId.toString(),
                "published", true
        );

        when(postService.edit(any(CreatePostRequestPayload.class), any(String.class)))
                .thenReturn(post);

        mockMvc.perform(put("/api/v1/admin/posts/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(post.getId().toString()))
                .andExpect(jsonPath("$.title").value("Тестовая новость"))
                .andExpect(jsonPath("$.published").value(true));

        ArgumentCaptor<CreatePostRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(CreatePostRequestPayload.class);

        ArgumentCaptor<String> postIdCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(postService).edit(payloadCaptor.capture(), postIdCaptor.capture());

        CreatePostRequestPayload payload = payloadCaptor.getValue();

        assertEquals(post.getId().toString(), postIdCaptor.getValue());
        assertEquals("Обновлённая новость", payload.getTitle());
        assertEquals("Обновлённый анонс", payload.getPreviewText());
        assertEquals("<p>Обновлённый контент</p>", payload.getContentHtml());
        assertEquals(bannerFileId, payload.getBannerFileId());
        assertEquals(true, payload.getPublished());
    }

    @Test
    void shouldPublishPost() throws Exception {
        UUID postId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "published", true
        );

        mockMvc.perform(patch("/api/v1/admin/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("published successfully"));

        verify(postService).changePublished(postId.toString(), true);
    }

    @Test
    void shouldUnpublishPost() throws Exception {
        UUID postId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "published", false
        );

        mockMvc.perform(patch("/api/v1/admin/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("unpublished successfully"));

        verify(postService).changePublished(postId.toString(), false);
    }

    @Test
    void shouldReturnBadRequestWhenPostIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenPostDoesNotExist() throws Exception {
        String postId = UUID.randomUUID().toString();

        when(postService.find(postId))
                .thenThrow(new EntityNotFoundException("Post with id " + postId + " not found"));

        mockMvc.perform(get("/api/v1/admin/posts/{id}", postId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenCreateBodyIsInvalid() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "title", "",
                "previewText", "Анонс"
        );

        mockMvc.perform(post("/api/v1/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenEditBodyIsInvalid() throws Exception {
        UUID postId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "title", "",
                "previewText", "Анонс"
        );

        mockMvc.perform(put("/api/v1/admin/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPatchBodyIsInvalid() throws Exception {
        UUID postId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts")
                        .param("limit", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOffsetIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts")
                        .param("offset", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenYearIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts")
                        .param("year", "invalid"))
                .andExpect(status().isBadRequest());
    }

    private Post createPost() {
        User author = new User();
        author.setLogin("teachyyou");

        Post post = new Post();

        post.setId(UUID.randomUUID());
        post.setTitle("Тестовая новость");
        post.setPreviewText("Краткое описание");
        post.setContentHtml("<p>Содержимое новости</p>");
        post.setPublished(true);
        post.setPublishedAt(LocalDateTime.of(2026, 5, 20, 12, 0));
        post.setCreatedAt(LocalDateTime.of(2026, 5, 20, 10, 0));
        post.setUpdatedAt(LocalDateTime.of(2026, 5, 20, 11, 0));
        post.setAuthor(author);

        return post;
    }
}