package ru.sfedu.mmcs_nexus.controller.v1.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectEventSubmissionDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectEventSubmissionItemDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.ProjectEventSubmission;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.entity.keys.ProjectEventSubmissionKey;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.SubmissionAvailabilityStatus;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.payload.user.GetProjectEventSubmissionsResponsePayload;
import ru.sfedu.mmcs_nexus.model.payload.user.SaveProjectEventSubmissionRequestPayload;
import ru.sfedu.mmcs_nexus.service.ProjectEventSubmissionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProjectSubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserProjectSubmissionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectEventSubmissionService projectEventSubmissionService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnProjectEventSubmissions() throws Exception {
        setAuthentication("teachyyou");

        User user = createUser("teachyyou");
        Project project = createProject(user);
        Event event = createOpenEvent();
        ProjectEventSubmission submission = createSubmission(project, event, user);

        ProjectEventSubmissionItemDTO item = new ProjectEventSubmissionItemDTO(
                event,
                SubmissionAvailabilityStatus.OPEN,
                true,
                null,
                submission
        );

        GetProjectEventSubmissionsResponsePayload response =
                new GetProjectEventSubmissionsResponsePayload(event.getId(), List.of(item));

        when(projectEventSubmissionService.getSubmissions(project.getId().toString(), "teachyyou"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/user/projects/{projectId}/submissions", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultEventId").value(event.getId().toString()))
                .andExpect(jsonPath("$.items[0].eventId").value(event.getId().toString()))
                .andExpect(jsonPath("$.items[0].eventName").value("Промежуточная защита"))
                .andExpect(jsonPath("$.items[0].eventType").value("IDEA"))
                .andExpect(jsonPath("$.items[0].submissionStatus").value("OPEN"))
                .andExpect(jsonPath("$.items[0].editable").value(true))
                .andExpect(jsonPath("$.items[0].submission.presentationUrl").value("https://docs.google.com/presentation/d/example"))
                .andExpect(jsonPath("$.items[0].submission.repositoryUrl").value("https://github.com/teachyyou/mmcs_nexus"))
                .andExpect(jsonPath("$.items[0].submission.releaseUrl").value("https://github.com/teachyyou/mmcs_nexus/releases/tag/v1"))
                .andExpect(jsonPath("$.items[0].submission.comment").value("Комментарий"));

        verify(projectEventSubmissionService).getSubmissions(project.getId().toString(), "teachyyou");
    }

    @Test
    void shouldSaveProjectEventSubmission() throws Exception {
        setAuthentication("teachyyou");

        User user = createUser("teachyyou");
        Project project = createProject(user);
        Event event = createOpenEvent();
        ProjectEventSubmission submission = createSubmission(project, event, user);

        Map<String, Object> requestBody = Map.of(
                "presentationUrl", "https://docs.google.com/presentation/d/example",
                "repositoryUrl", "https://github.com/teachyyou/mmcs_nexus",
                "releaseUrl", "https://github.com/teachyyou/mmcs_nexus/releases/tag/v1",
                "comment", "Комментарий"
        );

        when(projectEventSubmissionService.saveSubmission(
                eq(project.getId().toString()),
                eq(event.getId().toString()),
                eq("teachyyou"),
                any(SaveProjectEventSubmissionRequestPayload.class)
        )).thenReturn(new ProjectEventSubmissionDTO(submission));

        mockMvc.perform(put("/api/v1/user/projects/{projectId}/events/{eventId}/submission", project.getId(), event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(project.getId().toString()))
                .andExpect(jsonPath("$.eventId").value(event.getId().toString()))
                .andExpect(jsonPath("$.presentationUrl").value("https://docs.google.com/presentation/d/example"))
                .andExpect(jsonPath("$.repositoryUrl").value("https://github.com/teachyyou/mmcs_nexus"))
                .andExpect(jsonPath("$.releaseUrl").value("https://github.com/teachyyou/mmcs_nexus/releases/tag/v1"))
                .andExpect(jsonPath("$.comment").value("Комментарий"))
                .andExpect(jsonPath("$.submittedByLogin").value("teachyyou"));

        ArgumentCaptor<SaveProjectEventSubmissionRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(SaveProjectEventSubmissionRequestPayload.class);

        verify(projectEventSubmissionService).saveSubmission(
                eq(project.getId().toString()),
                eq(event.getId().toString()),
                eq("teachyyou"),
                payloadCaptor.capture()
        );

        SaveProjectEventSubmissionRequestPayload payload = payloadCaptor.getValue();

        assertEquals("https://docs.google.com/presentation/d/example", payload.getPresentationUrl());
        assertEquals("https://github.com/teachyyou/mmcs_nexus", payload.getRepositoryUrl());
        assertEquals("https://github.com/teachyyou/mmcs_nexus/releases/tag/v1", payload.getReleaseUrl());
        assertEquals("Комментарий", payload.getComment());
    }

    @Test
    void shouldReturnBadRequestWhenProjectIdIsInvalid() throws Exception {
        setAuthentication("teachyyou");

        mockMvc.perform(get("/api/v1/user/projects/{projectId}/submissions", "invalid-id"))
                .andExpect(status().isBadRequest());

        verify(projectEventSubmissionService, never()).getSubmissions(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenEventIdIsInvalid() throws Exception {
        setAuthentication("teachyyou");

        UUID projectId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/user/projects/{projectId}/events/{eventId}/submission", projectId, "invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "presentationUrl", "https://example.com"
                        ))))
                .andExpect(status().isBadRequest());

        verify(projectEventSubmissionService, never()).saveSubmission(any(), any(), any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenUrlIsInvalid() throws Exception {
        setAuthentication("teachyyou");

        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/user/projects/{projectId}/events/{eventId}/submission", projectId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "presentationUrl", "not-url"
                        ))))
                .andExpect(status().isBadRequest());

        verify(projectEventSubmissionService, never()).saveSubmission(any(), any(), any(), any());
    }

    @Test
    void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
        setAuthentication("teachyyou");

        UUID projectId = UUID.randomUUID();

        when(projectEventSubmissionService.getSubmissions(projectId.toString(), "teachyyou"))
                .thenThrow(new EntityNotFoundException("Project with id " + projectId + " not found"));

        mockMvc.perform(get("/api/v1/user/projects/{projectId}/submissions", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Project with id " + projectId + " not found"));
    }

    @Test
    void shouldReturnForbiddenWhenUserIsNotProjectCaptain() throws Exception {
        setAuthentication("teachyyou");

        UUID projectId = UUID.randomUUID();

        when(projectEventSubmissionService.getSubmissions(projectId.toString(), "teachyyou"))
                .thenThrow(new ResponseStatusException(FORBIDDEN, "Only project captain can manage submissions"));

        mockMvc.perform(get("/api/v1/user/projects/{projectId}/submissions", projectId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only project captain can manage submissions"));
    }

    private void setAuthentication(String login) {
        SecurityContextHolder.getContext().setAuthentication(createOAuth2Authentication(login));
    }

    private Authentication createOAuth2Authentication(String login) {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put("sub", login);
        attributes.put("login", login);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "login"
        );

        return new OAuth2AuthenticationToken(
                oauth2User,
                oauth2User.getAuthorities(),
                "github"
        );
    }

    private ProjectEventSubmission createSubmission(Project project, Event event, User user) {
        ProjectEventSubmission submission = new ProjectEventSubmission();

        submission.setId(new ProjectEventSubmissionKey(project.getId(), event.getId()));
        submission.setProject(project);
        submission.setEvent(event);
        submission.setSubmittedBy(user);
        submission.setPresentationUrl("https://docs.google.com/presentation/d/example");
        submission.setRepositoryUrl("https://github.com/teachyyou/mmcs_nexus");
        submission.setReleaseUrl("https://github.com/teachyyou/mmcs_nexus/releases/tag/v1");
        submission.setComment("Комментарий");
        submission.setCreatedAt(LocalDateTime.of(2026, 5, 24, 12, 0));
        submission.setUpdatedAt(LocalDateTime.of(2026, 5, 24, 13, 0));

        return submission;
    }

    private Project createProject(User captain) {
        Project project = new Project();

        project.setId(UUID.randomUUID());
        project.setExternalId(1001);
        project.setQuantityOfStudents(4);
        project.setCaptainName("Иван Иванов");
        project.setFull(true);
        project.setTrack("Backend");
        project.setTechnologies("Java, Spring");
        project.setName("MMCS Nexus");
        project.setDescription("Описание проекта");
        project.setType("WEB_APP");
        project.setYear(2026);
        project.setCaptain(captain);

        return project;
    }

    private Event createOpenEvent() {
        Event event = new Event();

        event.setId(UUID.randomUUID());
        event.setName("Промежуточная защита");
        event.setEventType(EventType.IDEA);
        event.setYear(2026);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);
        event.setSubmissionStartDate(LocalDate.now().minusDays(1));
        event.setSubmissionDeadlineDate(LocalDate.now().plusDays(1));

        return event;
    }

    private User createUser(String login) {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setLogin(login);
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail(login + "@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_USER);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }
}