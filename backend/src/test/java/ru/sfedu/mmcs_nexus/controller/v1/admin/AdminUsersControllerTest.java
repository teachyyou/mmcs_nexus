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
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.EditUserRequestPayload;
import ru.sfedu.mmcs_nexus.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnUsersList() throws Exception {
        User user = createUser();

        when(userService.findAll(any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(user.getId().toString()))
                .andExpect(jsonPath("$.content[0].login").value("teachyyou"))
                .andExpect(jsonPath("$.content[0].firstName").value("Иван"))
                .andExpect(jsonPath("$.content[0].lastName").value("Иванов"))
                .andExpect(jsonPath("$.content[0].email").value("ivan@sfedu.ru"))
                .andExpect(jsonPath("$.content[0].role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.content[0].status").value("VERIFIED"))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(userService).findAll(paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(10, paginationPayload.getLimit());
        assertEquals(0, paginationPayload.getOffset());
        assertEquals("id", paginationPayload.getSort());
        assertEquals("asc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnEmptyUsersList() throws Exception {
        when(userService.findAll(any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(userService).findAll(any(PaginationPayload.class));
    }

    @Test
    void shouldPassQueryParamsToService() throws Exception {
        when(userService.findAll(any(PaginationPayload.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("limit", "5")
                        .param("offset", "10")
                        .param("sort", "login")
                        .param("order", "desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<PaginationPayload> paginationCaptor = ArgumentCaptor.forClass(PaginationPayload.class);

        verify(userService).findAll(paginationCaptor.capture());

        PaginationPayload paginationPayload = paginationCaptor.getValue();

        assertEquals(5, paginationPayload.getLimit());
        assertEquals(10, paginationPayload.getOffset());
        assertEquals("login", paginationPayload.getSort());
        assertEquals("desc", paginationPayload.getOrder());
    }

    @Test
    void shouldReturnUserById() throws Exception {
        User user = createUser();

        when(userService.find(user.getId().toString())).thenReturn(user);

        mockMvc.perform(get("/api/v1/admin/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.login").value("teachyyou"))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@sfedu.ru"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.status").value("VERIFIED"));

        verify(userService).find(user.getId().toString());
    }

    @Test
    void shouldEditUser() throws Exception {
        User user = createUser();
        user.setFirstName("Пётр");
        user.setLastName("Петров");
        user.setEmail("petr@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_JURY);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        Map<String, Object> requestBody = Map.of(
                "firstName", "Пётр",
                "lastName", "Петров",
                "email", "petr@sfedu.ru",
                "role", "ROLE_JURY",
                "status", "VERIFIED"
        );

        when(userService.edit(any(String.class), any(EditUserRequestPayload.class)))
                .thenReturn(user);

        mockMvc.perform(put("/api/v1/admin/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.firstName").value("Пётр"))
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andExpect(jsonPath("$.email").value("petr@sfedu.ru"))
                .andExpect(jsonPath("$.role").value("ROLE_JURY"))
                .andExpect(jsonPath("$.status").value("VERIFIED"));

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EditUserRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(EditUserRequestPayload.class);

        verify(userService).edit(userIdCaptor.capture(), payloadCaptor.capture());

        EditUserRequestPayload payload = payloadCaptor.getValue();

        assertEquals(user.getId().toString(), userIdCaptor.getValue());
        assertEquals("Пётр", payload.getFirstName());
        assertEquals("Петров", payload.getLastName());
        assertEquals("petr@sfedu.ru", payload.getEmail());
        assertEquals(UserEnums.UserRole.ROLE_JURY, payload.getRole());
        assertEquals(UserEnums.UserStatus.VERIFIED, payload.getStatus());
    }

    @Test
    void shouldBlockUser() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).block(userId.toString());
    }

    @Test
    void shouldReturnBadRequestWhenUserIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        String userId = UUID.randomUUID().toString();

        when(userService.find(userId))
                .thenThrow(new EntityNotFoundException("User with id " + userId + " not found"));

        mockMvc.perform(get("/api/v1/admin/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with id " + userId + " not found"));
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("limit", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenOffsetIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("offset", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSortParamIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("sort", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting param: unknown"));
    }

    @Test
    void shouldReturnBadRequestWhenSortOrderIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("order", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect sorting order: wrong"));
    }

    @Test
    void shouldReturnBadRequestWhenEditingUserWithInvalidEmail() throws Exception {
        UUID userId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "firstName", "Пётр",
                "lastName", "Петров",
                "email", "petr@gmail.com",
                "role", "ROLE_JURY",
                "status", "VERIFIED"
        );

        mockMvc.perform(put("/api/v1/admin/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void shouldReturnBadRequestWhenEditingUserWithInvalidFirstName() throws Exception {
        UUID userId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "firstName", "Peter",
                "lastName", "Петров",
                "email", "petr@sfedu.ru",
                "role", "ROLE_JURY",
                "status", "VERIFIED"
        );

        mockMvc.perform(put("/api/v1/admin/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists());
    }

    @Test
    void shouldReturnBadRequestWhenEditingUserWithInvalidLastName() throws Exception {
        UUID userId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "firstName", "Пётр",
                "lastName", "Petrov",
                "email", "petr@sfedu.ru",
                "role", "ROLE_JURY",
                "status", "VERIFIED"
        );

        mockMvc.perform(put("/api/v1/admin/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lastName").exists());
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