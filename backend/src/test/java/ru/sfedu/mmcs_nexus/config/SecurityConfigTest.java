package ru.sfedu.mmcs_nexus.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.controller.publicapi.PublicEventController;
import ru.sfedu.mmcs_nexus.controller.v1.admin.AdminPostController;
import ru.sfedu.mmcs_nexus.controller.v1.auth.AuthController;
import ru.sfedu.mmcs_nexus.controller.v1.jury.JuryGradeController;
import ru.sfedu.mmcs_nexus.controller.v1.media.MediaController;
import ru.sfedu.mmcs_nexus.model.dto.entity.PostDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.service.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest({
        AuthController.class,
        AdminPostController.class,
        JuryGradeController.class,
        PublicEventController.class,
        MediaController.class
})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "BASE_URL=http://localhost:3000"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleHierarchy roleHierarchy;

    @MockBean
    private UserService userService;

    @MockBean
    private PostService postService;

    @MockBean
    private GradeService gradeService;

    @MockBean
    private EventService eventService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UploadedFileService uploadedFileService;

    @Test
    void shouldAllowAnonymousUserToAccessAuthStatus() throws Exception {
        mockMvc.perform(get("/api/v1/auth/status"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAnonymousUserToAccessPublicApi() throws Exception {
        when(eventService.findAll(any(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<Event>(List.of()));

        mockMvc.perform(get("/api/v1/public/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldAllowAnonymousUserToAccessMediaApiButReturnBadRequestForInvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/media/image/invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedForAnonymousUserOnAdminApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Пользователь не авторизован."));
    }

    @Test
    void shouldReturnUnauthorizedForAnonymousUserOnJuryApi() throws Exception {
        mockMvc.perform(get("/api/v1/jury/grades/table/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Пользователь не авторизован."));
    }

    @Test
    void shouldForbidUserRoleFromAdminApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts")
                        .with(oauth2Login()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Недостаточно прав"));
    }

    @Test
    void shouldForbidJuryRoleFromAdminApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/posts")
                        .with(oauth2Login()
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Недостаточно прав"));
    }

    @Test
    void shouldAllowAdminRoleToAdminApi() throws Exception {
        when(postService.findAll(isNull(), any(PaginationPayload.class)))
                .thenReturn(new PageImpl<PostDTO>(List.of()));

        mockMvc.perform(get("/api/v1/admin/posts")
                        .with(oauth2Login()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldForbidUserRoleFromJuryApi() throws Exception {
        mockMvc.perform(get("/api/v1/jury/grades/table/00000000-0000-0000-0000-000000000001")
                        .with(oauth2Login()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Недостаточно прав"));
    }

    @Test
    void shouldAllowJuryRoleToJuryApiButReturnBadRequestForInvalidShowFilter() throws Exception {
        mockMvc.perform(get("/api/v1/jury/grades/table/00000000-0000-0000-0000-000000000001")
                        .param("show", "wrong")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "jury"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failure"));
    }

    @Test
    void shouldAllowAdminRoleToJuryApiBecauseOfRoleHierarchy() throws Exception {
        mockMvc.perform(get("/api/v1/jury/grades/table/00000000-0000-0000-0000-000000000001")
                        .param("show", "wrong")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "admin"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failure"));
    }

    @Test
    void shouldConfigureRoleHierarchy() {
        var reachableAuthorities = roleHierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertTrue(reachableAuthorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(reachableAuthorities.contains(new SimpleGrantedAuthority("ROLE_JURY")));
        assertTrue(reachableAuthorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(reachableAuthorities.contains(new SimpleGrantedAuthority("ROLE_GUEST")));
    }

    @Test
    void shouldNotCallUserServiceForAnonymousAuthStatus() throws Exception {
        mockMvc.perform(get("/api/v1/auth/status"))
                .andExpect(status().isOk());

        verify(userService, never()).findByGithubLogin(any(org.springframework.security.core.Authentication.class));
    }
}