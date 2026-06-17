package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.sfedu.mmcs_nexus.exceptions.EmailAlreadyTakenException;
import ru.sfedu.mmcs_nexus.model.dto.entity.UserDTO;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.EditUserRequestPayload;
import ru.sfedu.mmcs_nexus.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldFindAllUsers() {
        User user = createUser();
        PaginationPayload paginationPayload = new PaginationPayload(10, 0, "id", "asc", EntitySort.USER_SORT);

        when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        Page<User> result = userService.findAll(paginationPayload);

        assertEquals(1, result.getTotalElements());
        assertEquals(user.getId(), result.getContent().getFirst().getId());

        verify(userRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void shouldFindUserById() {
        User user = createUser();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = userService.find(user.getId().toString());

        assertSame(user, result);

        verify(userRepository).findById(user.getId());
    }

    @Test
    void shouldThrowWhenUserNotFoundById() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.find(userId.toString())
        );

        assertEquals("User with id " + userId + " not found", exception.getMessage());

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldCreateUserWhenGithubLoginDoesNotExist() {
        when(userRepository.findByLogin("teachyyou")).thenReturn(Optional.empty());

        userService.create("teachyyou");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldNotCreateUserWhenGithubLoginAlreadyExists() {
        User user = createUser();

        when(userRepository.findByLogin("teachyyou")).thenReturn(Optional.of(user));

        userService.create("teachyyou");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFindByGithubLogin() {
        User user = createUser();

        when(userRepository.findByLogin("teachyyou")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByGithubLogin("teachyyou");

        assertTrue(result.isPresent());
        assertSame(user, result.get());

        verify(userRepository).findByLogin("teachyyou");
    }

    @Test
    void shouldFindByGithubLoginFromAuthentication() {
        User user = createUser();

        DefaultOAuth2User oauthUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("login", "teachyyou"),
                "login"
        );

        TestingAuthenticationToken authentication = new TestingAuthenticationToken(oauthUser, null);

        when(userRepository.findByLogin("teachyyou")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByGithubLogin(authentication);

        assertTrue(result.isPresent());
        assertSame(user, result.get());

        verify(userRepository).findByLogin("teachyyou");
    }

    @Test
    void shouldReturnTrueWhenUserIsNotFoundOrVerifiedAndUserDoesNotExist() {
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        boolean result = userService.isNotFoundOrVerified("unknown");

        assertTrue(result);

        verify(userRepository).findByLogin("unknown");
    }

    @Test
    void shouldReturnTrueWhenUserIsNotFoundOrVerifiedAndUserIsNonVerified() {
        User user = createUser();
        user.setStatus(UserEnums.UserStatus.NON_VERIFIED);

        when(userRepository.findByLogin("teachyyou")).thenReturn(Optional.of(user));

        boolean result = userService.isNotFoundOrVerified("teachyyou");

        assertTrue(result);

        verify(userRepository).findByLogin("teachyyou");
    }

    @Test
    void shouldReturnFalseWhenUserIsNotFoundOrVerifiedAndUserIsVerified() {
        User user = createUser();
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        when(userRepository.findByLogin("teachyyou")).thenReturn(Optional.of(user));

        boolean result = userService.isNotFoundOrVerified("teachyyou");

        assertFalse(result);

        verify(userRepository).findByLogin("teachyyou");
    }

    @Test
    void shouldEditUser() {
        User user = createUser();
        EditUserRequestPayload payload = createEditPayload(
                "Пётр",
                "Петров",
                "petr@sfedu.ru",
                UserEnums.UserRole.ROLE_JURY,
                UserEnums.UserStatus.VERIFIED
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("petr@sfedu.ru", user.getId())).thenReturn(false);

        User result = userService.edit(user.getId().toString(), payload);

        assertSame(user, result);
        assertEquals("Пётр", user.getFirstName());
        assertEquals("Петров", user.getLastName());
        assertEquals("petr@sfedu.ru", user.getEmail());
        assertEquals(UserEnums.UserRole.ROLE_JURY, user.getRole());
        assertEquals(UserEnums.UserStatus.VERIFIED, user.getStatus());

        verify(userRepository).existsByEmailAndIdNot("petr@sfedu.ru", user.getId());
    }

    @Test
    void shouldThrowWhenEditingUserWithTakenEmail() {
        User user = createUser();
        EditUserRequestPayload payload = createEditPayload(
                "Пётр",
                "Петров",
                "petr@sfedu.ru",
                UserEnums.UserRole.ROLE_JURY,
                UserEnums.UserStatus.VERIFIED
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("petr@sfedu.ru", user.getId())).thenReturn(true);

        EmailAlreadyTakenException exception = assertThrows(
                EmailAlreadyTakenException.class,
                () -> userService.edit(user.getId().toString(), payload)
        );

        assertEquals("Email is already taken: petr@sfedu.ru", exception.getMessage());
    }

    @Test
    void shouldUpdateUserInfoAndVerifyNonVerifiedUser() {
        User user = createUser();
        user.setStatus(UserEnums.UserStatus.NON_VERIFIED);

        when(userRepository.findByLogin("teachyyou")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("petr@sfedu.ru", user.getId())).thenReturn(false);

        UserDTO result = userService.updateUserInfo("teachyyou", "petr@sfedu.ru", "Пётр", "Петров");

        assertEquals(user.getId(), result.getId());
        assertEquals("teachyyou", result.getLogin());
        assertEquals("Пётр", result.getFirstName());
        assertEquals("Петров", result.getLastName());
        assertEquals("petr@sfedu.ru", result.getEmail());
        assertEquals(UserEnums.UserStatus.VERIFIED, user.getStatus());
        assertEquals(UserEnums.UserRole.ROLE_ADMIN, user.getRole());
    }

    @Test
    void shouldUpdateUserInfoWithoutChangingVerifiedStatus() {
        User user = createUser();
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        when(userRepository.findByLogin("teachyyou")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("petr@sfedu.ru", user.getId())).thenReturn(false);

        userService.updateUserInfo("teachyyou", "petr@sfedu.ru", "Пётр", "Петров");

        assertEquals(UserEnums.UserStatus.VERIFIED, user.getStatus());
    }

    @Test
    void shouldThrowWhenUpdatingUnknownUserInfo() {
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.updateUserInfo("unknown", "petr@sfedu.ru", "Пётр", "Петров")
        );

        assertEquals("User unknown is not found", exception.getMessage());
    }

    @Test
    void shouldBlockUser() {
        User user = createUser();
        user.setRole(UserEnums.UserRole.ROLE_ADMIN);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.block(user.getId().toString());

        assertEquals(UserEnums.UserStatus.BLOCKED, user.getStatus());
        assertEquals(UserEnums.UserRole.ROLE_USER, user.getRole());
    }

    @Test
    void shouldThrowWhenBlockingUnknownUser() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.block(userId.toString())
        );

        assertEquals("User with id " + userId + " not found", exception.getMessage());
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

    private EditUserRequestPayload createEditPayload(
            String firstName,
            String lastName,
            String email,
            UserEnums.UserRole role,
            UserEnums.UserStatus status
    ) {
        EditUserRequestPayload payload = new EditUserRequestPayload();

        payload.setFirstName(firstName);
        payload.setLastName(lastName);
        payload.setEmail(email);
        payload.setRole(role);
        payload.setStatus(status);

        return payload;
    }
}