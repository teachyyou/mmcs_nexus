package ru.sfedu.mmcs_nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.exceptions.EmailAlreadyTakenException;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.EditUserRequestPayload;
import ru.sfedu.mmcs_nexus.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> getUsers(PaginationPayload paginationPayload) {
        Pageable pageable = paginationPayload.getPageable();

        return userRepository.findAll(pageable);
    }

    //Для первого сохранения в БД чисто по логину
    public void saveUser(String githubLogin) {
        if (findByGithubLogin(githubLogin).isEmpty()) {
            saveUser(new User(githubLogin));
        }
    }

    public void updateUserInfo(String login, String email, String firstName, String lastName) {
        User user = findByGithubLogin(login).orElseThrow(
                () -> new UsernameNotFoundException(STR."User \{login} is not found")
        );

        partialUserUpdate(user, firstName, lastName,email, null, null);

        if (user.getStatus() == UserEnums.UserStatus.NON_VERIFIED) {
            user.setStatus(UserEnums.UserStatus.VERIFIED);
        }

        saveUser(user);
    }

    public User editUser(User user, EditUserRequestPayload payload) {
        partialUserUpdate(user, payload.getFirstName(), payload.getLastName(), payload.getEmail(), payload.getRole(), payload.getStatus());

        saveUser(user);

        return user;
    }

    public void blockUser(User user) {
        user.setStatus(UserEnums.UserStatus.BLOCKED);
        user.setRole(UserEnums.UserRole.ROLE_USER);

        saveUser(user);
    }

    public Optional<User> findByGithubLogin(String githubLogin) {
        return userRepository.findByLogin(githubLogin).stream().findFirst();
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(UUID.fromString(id));
    }

    public Optional<User> findByGithubLogin(Authentication authentication) {
        DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();
        String githubLogin = user.getAttribute("login");
        return userRepository.findByLogin(githubLogin).stream().findFirst();
    }

    public boolean isNotFoundOrVerified(String githubLogin) {
        Optional<User> optionalUser = findByGithubLogin(githubLogin);
        return optionalUser.isEmpty() || optionalUser.get().getStatus() == UserEnums.UserStatus.NON_VERIFIED;
    }

    private void saveUser(User user) {
        userRepository.saveAndFlush(user);
    }

    private void partialUserUpdate(User user, String firstName, String lastName, String email, UserEnums.UserRole role, UserEnums.UserStatus status) {

        if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            throw new EmailAlreadyTakenException(email);
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        if (role!=null) user.setRole(role);
        if (status!=null) user.setStatus(status);
    }

}
