package ru.sfedu.mmcs_nexus.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:users_repository_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS \"text\" AS CLOB",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions=true"
})
class UsersRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByLogin() {
        User user = createUser("teachyyou", "ivan@sfedu.ru");

        userRepository.save(user);

        Optional<User> result = userRepository.findByLogin("teachyyou");

        assertTrue(result.isPresent());
        assertEquals("teachyyou", result.get().getLogin());
        assertEquals("ivan@sfedu.ru", result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenUserLoginDoesNotExist() {
        Optional<User> result = userRepository.findByLogin("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckEmailExistsForAnotherUser() {
        User firstUser = userRepository.save(createUser("first", "first@sfedu.ru"));
        User secondUser = userRepository.save(createUser("second", "second@sfedu.ru"));

        assertTrue(userRepository.existsByEmailAndIdNot("first@sfedu.ru", secondUser.getId()));
        assertFalse(userRepository.existsByEmailAndIdNot("first@sfedu.ru", firstUser.getId()));
        assertFalse(userRepository.existsByEmailAndIdNot("unknown@sfedu.ru", secondUser.getId()));
    }

    @Test
    void shouldPersistDefaultUserStatusWhenUsingDefaultConstructor() {
        User user = new User();

        user.setLogin("new-user");
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail("new-user@sfedu.ru");
        user.setRole(UserEnums.UserRole.ROLE_USER);

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals(UserEnums.UserStatus.NON_VERIFIED, savedUser.getStatus());
    }

    @Test
    void shouldPersistUserCreatedByGithubLoginConstructor() {
        User user = new User("teachyyou");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("teachyyou", savedUser.getLogin());
        assertEquals(UserEnums.UserStatus.NON_VERIFIED, savedUser.getStatus());
        assertEquals(UserEnums.UserRole.ROLE_USER, savedUser.getRole());
    }

    private User createUser(String login, String email) {
        User user = new User();

        user.setLogin(login);
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setEmail(email);
        user.setRole(UserEnums.UserRole.ROLE_USER);
        user.setStatus(UserEnums.UserStatus.VERIFIED);

        return user;
    }
}