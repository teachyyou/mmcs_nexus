package ru.sfedu.mmcs_nexus.controller.v1.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreatePostRequestPayload;
import ru.sfedu.mmcs_nexus.service.PostService;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPostController.class)
@AutoConfigureMockMvc
class AdminPostsControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @Test
    void shouldCreatePostWithAuthenticatedOauthUser() throws Exception {
        UUID bannerFileId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "title", "Новая новость",
                "previewText", "Новый анонс",
                "contentHtml", "<p>Новый контент</p>",
                "bannerFileId", bannerFileId.toString(),
                "published", true
        );

        mockMvc.perform(post("/api/v1/admin/posts")
                        .with(csrf())
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("saved successfully"));

        ArgumentCaptor<CreatePostRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(CreatePostRequestPayload.class);

        ArgumentCaptor<String> loginCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(postService).create(payloadCaptor.capture(), loginCaptor.capture());

        CreatePostRequestPayload payload = payloadCaptor.getValue();

        assertEquals("teachyyou", loginCaptor.getValue());
        assertEquals("Новая новость", payload.getTitle());
        assertEquals("Новый анонс", payload.getPreviewText());
        assertEquals("<p>Новый контент</p>", payload.getContentHtml());
        assertEquals(bannerFileId, payload.getBannerFileId());
        assertEquals(true, payload.getPublished());
    }
}