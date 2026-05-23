package ru.sfedu.mmcs_nexus.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.model.entity.Post;
import ru.sfedu.mmcs_nexus.model.entity.UploadedFile;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.repository.PostRepository;
import ru.sfedu.mmcs_nexus.repository.UploadedFileRepository;
import ru.sfedu.mmcs_nexus.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PostsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    void shouldCreatePostThroughAdminApiAndReturnItInAdminList() throws Exception {
        User author = userRepository.save(createAdminUser("teachyyou"));
        UploadedFile bannerFile = uploadedFileRepository.save(createUploadedImage(author));

        Map<String, Object> requestBody = Map.of(
                "title", "Интеграционная новость",
                "previewText", "Краткое описание интеграционной новости",
                "contentHtml", "<p>Полный текст интеграционной новости</p>",
                "bannerFileId", bannerFile.getId().toString(),
                "published", false
        );

        mockMvc.perform(post("/api/v1/admin/posts")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("saved successfully"));

        assertEquals(1, postRepository.count());

        Post savedPost = postRepository.findAll().getFirst();

        assertEquals("Интеграционная новость", savedPost.getTitle());
        assertEquals("Краткое описание интеграционной новости", savedPost.getPreviewText());
        assertEquals("<p>Полный текст интеграционной новости</p>", savedPost.getContentHtml());
        assertEquals(bannerFile.getId(), savedPost.getBannerFile().getId());
        assertEquals(author.getId(), savedPost.getAuthor().getId());
        assertTrue(uploadedFileRepository.findById(bannerFile.getId()).orElseThrow().isAttached());

        mockMvc.perform(get("/api/v1/admin/posts")
                        .with(oauth2Login()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(savedPost.getId().toString()))
                .andExpect(jsonPath("$.content[0].title").value("Интеграционная новость"))
                .andExpect(jsonPath("$.content[0].previewText").value("Краткое описание интеграционной новости"))
                .andExpect(jsonPath("$.content[0].contentHtml").value("<p>Полный текст интеграционной новости</p>"))
                .andExpect(jsonPath("$.content[0].published").value(false))
                .andExpect(jsonPath("$.content[0].author").value("teachyyou"))
                .andExpect(jsonPath("$.content[0].bannerFileId").value(bannerFile.getId().toString()))
                .andExpect(jsonPath("$.content[0].bannerUrl").value("/api/v1/media/image/" + bannerFile.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldPublishPostThroughAdminApiAndShowItInPublicList() throws Exception {
        User author = userRepository.save(createAdminUser("teachyyou"));
        UploadedFile bannerFile = uploadedFileRepository.save(createUploadedImage(author));
        Post post = postRepository.save(createPost(author, bannerFile, false));

        mockMvc.perform(get("/api/v1/public/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        Map<String, Object> requestBody = Map.of(
                "published", true
        );

        mockMvc.perform(patch("/api/v1/admin/posts/{id}", post.getId())
                        .with(oauth2Login()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("published successfully"));

        Post publishedPost = postRepository.findById(post.getId()).orElseThrow();

        assertTrue(publishedPost.isPublished());

        mockMvc.perform(get("/api/v1/public/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(post.getId().toString()))
                .andExpect(jsonPath("$.content[0].title").value("Тестовая новость"))
                .andExpect(jsonPath("$.content[0].published").value(true))
                .andExpect(jsonPath("$.content[0].author").value("teachyyou"))
                .andExpect(jsonPath("$.content[0].bannerUrl").value("/api/v1/media/image/" + bannerFile.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/public/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId().toString()))
                .andExpect(jsonPath("$.title").value("Тестовая новость"))
                .andExpect(jsonPath("$.contentHtml").value("<p>Содержимое новости</p>"))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    void shouldNotAllowAnonymousUserToCreatePost() throws Exception {
        UUID bannerFileId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "title", "Новость",
                "previewText", "Анонс",
                "contentHtml", "<p>Контент</p>",
                "bannerFileId", bannerFileId.toString(),
                "published", false
        );

        mockMvc.perform(post("/api/v1/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Пользователь не авторизован."));

        assertEquals(0, postRepository.count());
    }

    private User createAdminUser(String login) {
        User user = new User();

        user.setLogin(login);
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail(login + "@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_ADMIN);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }

    private UploadedFile createUploadedImage(User user) {
        UploadedFile uploadedFile = new UploadedFile();

        uploadedFile.setOriginalFilename("banner.png");
        uploadedFile.setStoragePath("posts/" + UUID.randomUUID() + ".png");
        uploadedFile.setContentType(MediaType.IMAGE_PNG_VALUE);
        uploadedFile.setSizeBytes(1024L);
        uploadedFile.setAttached(false);
        uploadedFile.setUploadedBy(user);

        return uploadedFile;
    }

    private Post createPost(User author, UploadedFile bannerFile, boolean published) {
        Post post = new Post();

        post.setTitle("Тестовая новость");
        post.setPreviewText("Краткое описание");
        post.setContentHtml("<p>Содержимое новости</p>");
        post.setPublished(published);
        post.setPublishedAt(published ? LocalDateTime.of(2026, 5, 20, 12, 0) : null);
        post.setCreatedAt(LocalDateTime.of(2026, 5, 20, 10, 0));
        post.setUpdatedAt(LocalDateTime.of(2026, 5, 20, 11, 0));
        post.setAuthor(author);
        post.setBannerFile(bannerFile);

        return post;
    }
}