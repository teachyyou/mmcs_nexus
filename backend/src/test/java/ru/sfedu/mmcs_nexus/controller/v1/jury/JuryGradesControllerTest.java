package ru.sfedu.mmcs_nexus.controller.v1.jury;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.sfedu.mmcs_nexus.config.SecurityConfig;
import ru.sfedu.mmcs_nexus.exceptions.WrongGradePointsException;
import ru.sfedu.mmcs_nexus.model.dto.entity.GradeDTO;
import ru.sfedu.mmcs_nexus.model.dto.entity.ProjectDTO;
import ru.sfedu.mmcs_nexus.model.entity.Event;
import ru.sfedu.mmcs_nexus.model.entity.Grade;
import ru.sfedu.mmcs_nexus.model.entity.Project;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.entity.keys.GradeKey;
import ru.sfedu.mmcs_nexus.model.enums.controller.jury.GradeTableEnums;
import ru.sfedu.mmcs_nexus.model.enums.entity.EventType;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.internal.GradeTableRow;
import ru.sfedu.mmcs_nexus.model.payload.jury.CreateGradeRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.jury.GetGradeTableResponsePayload;
import ru.sfedu.mmcs_nexus.service.GradeService;
import ru.sfedu.mmcs_nexus.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JuryGradeController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "BASE_URL=http://localhost:3000"
})
class JuryGradesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GradeService gradeService;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnUnauthorizedWhenUserIsAnonymous() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/jury/grades/table/{eventId}", eventId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Пользователь не авторизован."));

        verify(gradeService, never()).getTable(any(), any(), any(), any());
    }

    @Test
    void shouldReturnForbiddenWhenUserHasOnlyUserRole() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/jury/grades/table/{eventId}", eventId)
                        .with(oauth2Login()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Недостаточно прав"));

        verify(gradeService, never()).getTable(any(), any(), any(), any());
    }

    @Test
    void shouldReturnGradesTable() throws Exception {
        Event event = createEvent();
        Project project = createProject("MMCS Nexus");
        User jury = createUser("jury");
        Grade grade = createGrade(project, event, jury, 15, 25, "Хорошая работа");

        GetGradeTableResponsePayload table = new GetGradeTableResponsePayload();
        table.setEvent(event);
        table.setProjects(List.of(project).stream().map(ProjectDTO::new).toList());
        table.setJuries(List.of(new ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO(jury)));

        GradeTableRow row = new GradeTableRow(project.getId(), null, project.getName());
        row.setTableRow(List.of(new GradeDTO(grade)));
        table.addGradeRow(row);

        when(gradeService.getTable("teachyyou", event.getId().toString(), GradeTableEnums.ShowFilter.ALL, null))
                .thenReturn(table);

        mockMvc.perform(get("/api/v1/jury/grades/table/{eventId}", event.getId())
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.event.id").value(event.getId().toString()))
                .andExpect(jsonPath("$.content.event.name").value("Идея"))
                .andExpect(jsonPath("$.content.projects", hasSize(1)))
                .andExpect(jsonPath("$.content.projects[0].id").value(project.getId().toString()))
                .andExpect(jsonPath("$.content.projects[0].name").value("MMCS Nexus"))
                .andExpect(jsonPath("$.content.juries", hasSize(1)))
                .andExpect(jsonPath("$.content.juries[0].id").value(jury.getId().toString()))
                .andExpect(jsonPath("$.content.rows", hasSize(1)))
                .andExpect(jsonPath("$.content.rows[0].projectId").value(project.getId().toString()))
                .andExpect(jsonPath("$.content.rows[0].projectDisplayName").value("MMCS Nexus"))
                .andExpect(jsonPath("$.content.rows[0].tableRow", hasSize(1)))
                .andExpect(jsonPath("$.content.rows[0].tableRow[0].presPoints").value(15))
                .andExpect(jsonPath("$.content.rows[0].tableRow[0].buildPoints").value(25))
                .andExpect(jsonPath("$.content.rows[0].tableRow[0].comment").value("Хорошая работа"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(gradeService).getTable("teachyyou", event.getId().toString(), GradeTableEnums.ShowFilter.ALL, null);
    }

    @Test
    void shouldPassShowAndDayParamsToService() throws Exception {
        UUID eventId = UUID.randomUUID();
        GetGradeTableResponsePayload table = new GetGradeTableResponsePayload();
        table.setProjects(List.of());
        table.setJuries(List.of());

        when(gradeService.getTable("teachyyou", eventId.toString(), GradeTableEnums.ShowFilter.ASSIGNED, 2))
                .thenReturn(table);

        mockMvc.perform(get("/api/v1/jury/grades/table/{eventId}", eventId)
                        .param("show", "assigned")
                        .param("day", "2")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(gradeService).getTable("teachyyou", eventId.toString(), GradeTableEnums.ShowFilter.ASSIGNED, 2);
    }

    @Test
    void shouldReturnBadRequestWhenShowParamIsInvalid() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/jury/grades/table/{eventId}", eventId)
                        .param("show", "wrong")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect filter parameter"))
                .andExpect(jsonPath("$.value").value("wrong"));

        verify(gradeService, never()).getTable(any(), any(), any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenEventIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/jury/grades/table/{eventId}", "invalid-id")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateGrade() throws Exception {
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        User jury = createUser("jury");
        Grade grade = createGrade(project, event, jury, 18, 27, "Отлично");

        Map<String, Object> requestBody = createRequestBody(project.getId(), event.getId(), 18, 27, "Отлично");

        when(gradeService.create(eq("teachyyou"), any(CreateGradeRequestPayload.class)))
                .thenReturn(new GradeDTO(grade));

        mockMvc.perform(post("/api/v1/jury/grades")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(project.getId().toString()))
                .andExpect(jsonPath("$.eventId").value(event.getId().toString()))
                .andExpect(jsonPath("$.juryId").value(jury.getId().toString()))
                .andExpect(jsonPath("$.presPoints").value(18))
                .andExpect(jsonPath("$.buildPoints").value(27))
                .andExpect(jsonPath("$.comment").value("Отлично"));

        ArgumentCaptor<CreateGradeRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(CreateGradeRequestPayload.class);

        verify(gradeService).create(eq("teachyyou"), payloadCaptor.capture());

        CreateGradeRequestPayload payload = payloadCaptor.getValue();

        assertEquals(project.getId(), payload.getProjectId());
        assertEquals(event.getId(), payload.getEventId());
        assertEquals(18, payload.getPresPoints());
        assertEquals(27, payload.getBuildPoints());
        assertEquals("Отлично", payload.getComment());
    }

    @Test
    void shouldUpdateGrade() throws Exception {
        Project project = createProject("MMCS Nexus");
        Event event = createEvent();
        User jury = createUser("jury");
        Grade grade = createGrade(project, event, jury, 19, 28, "Исправлено");

        Map<String, Object> requestBody = createRequestBody(project.getId(), event.getId(), 19, 28, "Исправлено");

        when(gradeService.edit(eq("teachyyou"), any(CreateGradeRequestPayload.class)))
                .thenReturn(new GradeDTO(grade));

        mockMvc.perform(put("/api/v1/jury/grades")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(project.getId().toString()))
                .andExpect(jsonPath("$.eventId").value(event.getId().toString()))
                .andExpect(jsonPath("$.juryId").value(jury.getId().toString()))
                .andExpect(jsonPath("$.presPoints").value(19))
                .andExpect(jsonPath("$.buildPoints").value(28))
                .andExpect(jsonPath("$.comment").value("Исправлено"));

        verify(gradeService).edit(eq("teachyyou"), any(CreateGradeRequestPayload.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateBodyIsInvalid() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "presPoints", -1,
                "buildPoints", 10,
                "comment", "Комментарий"
        );

        mockMvc.perform(post("/api/v1/jury/grades")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.projectId").exists())
                .andExpect(jsonPath("$.errors.eventId").exists())
                .andExpect(jsonPath("$.errors.presPoints").exists());

        verify(gradeService, never()).create(any(), any());
    }

    @Test
    void shouldReturnConflictWhenGradeAlreadyExists() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(gradeService.create(eq("teachyyou"), any(CreateGradeRequestPayload.class)))
                .thenThrow(new EntityExistsException("Grade already exists"));

        mockMvc.perform(post("/api/v1/jury/grades")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestBody(projectId, eventId, 10, 20, "ok"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Grade already exists"));
    }

    @Test
    void shouldReturnBadRequestWhenGradePointsAreTooHigh() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(gradeService.create(eq("teachyyou"), any(CreateGradeRequestPayload.class)))
                .thenThrow(new WrongGradePointsException("Maximum presentation score for Идея is 20"));

        mockMvc.perform(post("/api/v1/jury/grades")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestBody(projectId, eventId, 25, 20, "too high"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Maximum presentation score for Идея is 20"));
    }

    @Test
    void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(gradeService.create(eq("teachyyou"), any(CreateGradeRequestPayload.class)))
                .thenThrow(new EntityNotFoundException("Project with id " + projectId + " not found"));

        mockMvc.perform(post("/api/v1/jury/grades")
                        .with(oauth2Login()
                                .attributes(attributes -> attributes.put("login", "teachyyou"))
                                .authorities(new SimpleGrantedAuthority("ROLE_JURY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestBody(projectId, eventId, 10, 20, "ok"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Project with id " + projectId + " not found"));
    }

    private Map<String, Object> createRequestBody(
            UUID projectId,
            UUID eventId,
            Integer presPoints,
            Integer buildPoints,
            String comment
    ) {
        return Map.of(
                "projectId", projectId.toString(),
                "eventId", eventId.toString(),
                "presPoints", presPoints,
                "buildPoints", buildPoints,
                "comment", comment
        );
    }

    private Grade createGrade(Project project, Event event, User jury, Integer presPoints, Integer buildPoints, String comment) {
        Grade grade = new Grade();

        grade.setId(new GradeKey(project.getId(), event.getId(), jury.getId()));
        grade.setProject(project);
        grade.setEvent(event);
        grade.setJury(jury);
        grade.setPresPoints(presPoints);
        grade.setBuildPoints(buildPoints);
        grade.setComment(comment);

        return grade;
    }

    private Project createProject(String name) {
        Project project = new Project();

        project.setId(UUID.randomUUID());
        project.setExternalId(1001);
        project.setQuantityOfStudents(4);
        project.setCaptainName("Иван Иванов");
        project.setFull(true);
        project.setTrack("Backend");
        project.setTechnologies("Java, Spring");
        project.setName(name);
        project.setDescription("Описание проекта");
        project.setType("WEB_APP");
        project.setYear(2026);

        return project;
    }

    private Event createEvent() {
        Event event = new Event();

        event.setId(UUID.randomUUID());
        event.setName("Идея");
        event.setEventType(EventType.IDEA);
        event.setYear(2026);
        event.setMaxPresPoints(20);
        event.setMaxBuildPoints(30);

        return event;
    }

    private User createUser(String login) {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setLogin(login);
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail(login + "@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_JURY);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }
}