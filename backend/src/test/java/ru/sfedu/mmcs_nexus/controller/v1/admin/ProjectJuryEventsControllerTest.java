package ru.sfedu.mmcs_nexus.controller.v1.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.payload.admin.AssignJuriesRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.ProjectJuryEventResponsePayload;
import ru.sfedu.mmcs_nexus.service.ImportService;
import ru.sfedu.mmcs_nexus.service.ProjectEventService;
import ru.sfedu.mmcs_nexus.service.ProjectJuryEventService;
import ru.sfedu.mmcs_nexus.service.ProjectService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectJuryEventsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectEventService projectEventService;

    @MockBean
    private ProjectJuryEventService projectJuryEventService;

    @MockBean
    private ImportService importService;

    @Test
    void shouldReturnJuriesByProjectAndEvent() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        User mentor = createUser("mentor", "Ментор", "Иванов", UserEnums.UserRole.ROLE_JURY);
        User obligedJury = createUser("obliged", "Обязанный", "Петров", UserEnums.UserRole.ROLE_JURY);
        User willingJury = createUser("willing", "Желающий", "Сидоров", UserEnums.UserRole.ROLE_JURY);

        ProjectJuryEventResponsePayload response = new ProjectJuryEventResponsePayload();
        response.getMentors().add(new UserDTO(mentor));
        response.getObligedJuries().add(new UserDTO(obligedJury));
        response.getWillingJuries().add(new UserDTO(willingJury));

        when(projectJuryEventService.getJuriesByProjectAndEvent(projectId.toString(), eventId.toString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/projects/{projectId}/juries/{eventId}", projectId, eventId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mentors", hasSize(1)))
                .andExpect(jsonPath("$.mentors[0].id").value(mentor.getId().toString()))
                .andExpect(jsonPath("$.mentors[0].login").value("mentor"))
                .andExpect(jsonPath("$.mentors[0].firstName").value("Ментор"))
                .andExpect(jsonPath("$.mentors[0].lastName").value("Иванов"))
                .andExpect(jsonPath("$.obligedJuries", hasSize(1)))
                .andExpect(jsonPath("$.obligedJuries[0].id").value(obligedJury.getId().toString()))
                .andExpect(jsonPath("$.obligedJuries[0].login").value("obliged"))
                .andExpect(jsonPath("$.willingJuries", hasSize(1)))
                .andExpect(jsonPath("$.willingJuries[0].id").value(willingJury.getId().toString()))
                .andExpect(jsonPath("$.willingJuries[0].login").value("willing"));

        verify(projectJuryEventService).getJuriesByProjectAndEvent(projectId.toString(), eventId.toString());
    }

    @Test
    void shouldReturnEmptyJuriesListsByProjectAndEvent() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(projectJuryEventService.getJuriesByProjectAndEvent(projectId.toString(), eventId.toString()))
                .thenReturn(new ProjectJuryEventResponsePayload());

        mockMvc.perform(get("/api/v1/admin/projects/{projectId}/juries/{eventId}", projectId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentors", hasSize(0)))
                .andExpect(jsonPath("$.obligedJuries", hasSize(0)))
                .andExpect(jsonPath("$.willingJuries", hasSize(0)));

        verify(projectJuryEventService).getJuriesByProjectAndEvent(projectId.toString(), eventId.toString());
    }

    @Test
    void shouldAssignJuriesToProjectAndEvent() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();
        UUID obligedJuryId = UUID.randomUUID();
        UUID willingJuryId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "projectId", projectId.toString(),
                "eventId", eventId.toString(),
                "mentors", List.of(mentorId.toString()),
                "obligedJuries", List.of(obligedJuryId.toString()),
                "willingJuries", List.of(willingJuryId.toString()),
                "applyToAllEvents", false
        );

        mockMvc.perform(post("/api/v1/admin/projects/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("saved successfully"));

        ArgumentCaptor<AssignJuriesRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(AssignJuriesRequestPayload.class);

        verify(projectJuryEventService).assignJuries(payloadCaptor.capture());

        AssignJuriesRequestPayload payload = payloadCaptor.getValue();

        assertEquals(projectId.toString(), payload.getProjectId());
        assertEquals(eventId.toString(), payload.getEventId());
        assertEquals(List.of(mentorId.toString()), payload.getMentors());
        assertEquals(List.of(obligedJuryId.toString()), payload.getObligedJuries());
        assertEquals(List.of(willingJuryId.toString()), payload.getWillingJuries());
        assertEquals(false, payload.isApplyToAllEvents());
    }

    @Test
    void shouldAssignJuriesToAllProjectEvents() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID mentorId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "projectId", projectId.toString(),
                "eventId", eventId.toString(),
                "mentors", List.of(mentorId.toString()),
                "obligedJuries", List.of(),
                "willingJuries", List.of(),
                "applyToAllEvents", true
        );

        mockMvc.perform(post("/api/v1/admin/projects/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("saved successfully"));

        ArgumentCaptor<AssignJuriesRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(AssignJuriesRequestPayload.class);

        verify(projectJuryEventService).assignJuries(payloadCaptor.capture());

        assertEquals(true, payloadCaptor.getValue().isApplyToAllEvents());
        assertEquals(List.of(mentorId.toString()), payloadCaptor.getValue().getMentors());
    }

    @Test
    void shouldReturnBadRequestWhenProjectIdIsInvalidInAssignRequest() throws Exception {
        UUID eventId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "projectId", "invalid-id",
                "eventId", eventId.toString(),
                "mentors", List.of(),
                "obligedJuries", List.of(),
                "willingJuries", List.of(),
                "applyToAllEvents", false
        );

        mockMvc.perform(post("/api/v1/admin/projects/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.projectId").exists());
    }

    @Test
    void shouldReturnBadRequestWhenEventIdIsInvalidInAssignRequest() throws Exception {
        UUID projectId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "projectId", projectId.toString(),
                "eventId", "invalid-id",
                "mentors", List.of(),
                "obligedJuries", List.of(),
                "willingJuries", List.of(),
                "applyToAllEvents", false
        );

        mockMvc.perform(post("/api/v1/admin/projects/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.eventId").exists());
    }

    @Test
    void shouldReturnBadRequestWhenJuryListsAreMissing() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Map<String, Object> requestBody = Map.of(
                "projectId", projectId.toString(),
                "eventId", eventId.toString(),
                "applyToAllEvents", false
        );

        mockMvc.perform(post("/api/v1/admin/projects/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.mentors").exists())
                .andExpect(jsonPath("$.errors.obligedJuries").exists())
                .andExpect(jsonPath("$.errors.willingJuries").exists());
    }

    @Test
    void shouldReturnNotFoundWhenProjectDoesNotExistOnGetJuries() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(projectJuryEventService.getJuriesByProjectAndEvent(projectId.toString(), eventId.toString()))
                .thenThrow(new EntityNotFoundException("Project with id " + projectId + " not found"));

        mockMvc.perform(get("/api/v1/admin/projects/{projectId}/juries/{eventId}", projectId, eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Project with id " + projectId + " not found"));
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExistOnGetJuries() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(projectJuryEventService.getJuriesByProjectAndEvent(projectId.toString(), eventId.toString()))
                .thenThrow(new EntityNotFoundException("Event with id " + eventId + " not found"));

        mockMvc.perform(get("/api/v1/admin/projects/{projectId}/juries/{eventId}", projectId, eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event with id " + eventId + " not found"));
    }

    @Test
    void shouldReturnBadRequestWhenPathProjectIdIsInvalid() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/admin/projects/{projectId}/juries/{eventId}", "invalid-id", eventId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPathEventIdIsInvalid() throws Exception {
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/admin/projects/{projectId}/juries/{eventId}", projectId, "invalid-id"))
                .andExpect(status().isBadRequest());
    }

    private User createUser(String login, String firstName, String lastName, UserEnums.UserRole role) {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setLogin(login);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(login + "@sfedu.ru");
        user.setRole(role);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }
}