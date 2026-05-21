package ru.sfedu.mmcs_nexus.controller.v1.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.config.SecurityConfig;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.service.UserService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "BASE_URL=http://localhost:3000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnGuestStatusWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isAuthenticated").value(false))
                .andExpect(jsonPath("$.userRole").value("ROLE_GUEST"))
                .andExpect(jsonPath("$.userStatus").isEmpty())
                .andExpect(jsonPath("$.user").isEmpty());

        verify(userService, never()).findByGithubLogin(any(org.springframework.security.core.Authentication.class));
    }

    @Test
    void shouldReturnAuthenticatedUserStatus() throws Exception {
        User user = createUser();

        when(userService.findByGithubLogin(any(org.springframework.security.core.Authentication.class)))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/auth/status")
                        .with(oauth2Login()
                                .attributes(attributes -> {
                                    attributes.put("id", "12345");
                                    attributes.put("login", "teachyyou");
                                    attributes.put("name", "Teach You");
                                    attributes.put("avatar_url", "https://example.com/avatar.png");
                                })))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isAuthenticated").value(true))
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.userStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.userRole").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.user.login").value("teachyyou"))
                .andExpect(jsonPath("$.user.githubName").value("Teach You"))
                .andExpect(jsonPath("$.user.firstName").value("Иван"))
                .andExpect(jsonPath("$.user.lastName").value("Иванов"))
                .andExpect(jsonPath("$.user.email").value("ivan@sfedu.ru"))
                .andExpect(jsonPath("$.user.avatarUrl").value("https://example.com/avatar.png"));

        verify(userService).findByGithubLogin(any(org.springframework.security.core.Authentication.class));
    }

    @Test
    void shouldReturnNotFoundWhenAuthenticatedUserIsNotInDatabase() throws Exception {
        when(userService.findByGithubLogin(any(org.springframework.security.core.Authentication.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/auth/status")
                        .with(oauth2Login()
                                .attributes(attributes -> {
                                    attributes.put("id", "12345");
                                    attributes.put("login", "unknown");
                                    attributes.put("name", "Unknown User");
                                    attributes.put("avatar_url", "https://example.com/avatar.png");
                                })))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isAuthenticated").value(true))
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService).findByGithubLogin(any(org.springframework.security.core.Authentication.class));
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        User updatedUser = createUser();
        updatedUser.setFirstName("Пётр");
        updatedUser.setLastName("Петров");
        updatedUser.setEmail("petr@sfedu.ru");

        Map<String, Object> requestBody = Map.of(
                "firstName", "Пётр",
                "lastName", "Петров",
                "email", "petr@sfedu.ru"
        );

        when(userService.updateUserInfo("teachyyou", "petr@sfedu.ru", "Пётр", "Петров"))
                .thenReturn(new UserDTO(updatedUser));

        mockMvc.perform(put("/api/v1/auth/update_profile")
                        .with(oauth2Login()
                                .attributes(attributes -> {
                                    attributes.put("id", "12345");
                                    attributes.put("login", "teachyyou");
                                    attributes.put("name", "Teach You");
                                    attributes.put("avatar_url", "https://example.com/avatar.png");
                                }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(updatedUser.getId().toString()))
                .andExpect(jsonPath("$.login").value("teachyyou"))
                .andExpect(jsonPath("$.firstName").value("Пётр"))
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andExpect(jsonPath("$.email").value("petr@sfedu.ru"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        ArgumentCaptor<String> loginCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> firstNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> lastNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(userService).updateUserInfo(
                loginCaptor.capture(),
                emailCaptor.capture(),
                firstNameCaptor.capture(),
                lastNameCaptor.capture()
        );

        assertEquals("teachyyou", loginCaptor.getValue());
        assertEquals("petr@sfedu.ru", emailCaptor.getValue());
        assertEquals("Пётр", firstNameCaptor.getValue());
        assertEquals("Петров", lastNameCaptor.getValue());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingProfileWithInvalidEmail() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "firstName", "Пётр",
                "lastName", "Петров",
                "email", "petr@gmail.com"
        );

        mockMvc.perform(put("/api/v1/auth/update_profile")
                        .with(oauth2Login()
                                .attributes(attributes -> {
                                    attributes.put("id", "12345");
                                    attributes.put("login", "teachyyou");
                                }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(userService, never()).updateUserInfo(any(), any(), any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingProfileWithInvalidFirstName() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "firstName", "Peter",
                "lastName", "Петров",
                "email", "petr@sfedu.ru"
        );

        mockMvc.perform(put("/api/v1/auth/update_profile")
                        .with(oauth2Login()
                                .attributes(attributes -> {
                                    attributes.put("id", "12345");
                                    attributes.put("login", "teachyyou");
                                }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists());

        verify(userService, never()).updateUserInfo(any(), any(), any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingProfileWithInvalidLastName() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "firstName", "Пётр",
                "lastName", "Petrov",
                "email", "petr@sfedu.ru"
        );

        mockMvc.perform(put("/api/v1/auth/update_profile")
                        .with(oauth2Login()
                                .attributes(attributes -> {
                                    attributes.put("id", "12345");
                                    attributes.put("login", "teachyyou");
                                }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lastName").exists());

        verify(userService, never()).updateUserInfo(any(), any(), any(), any());
    }

    private User createUser() {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setLogin("teachyyou");
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail("ivan@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_ADMIN);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }
}