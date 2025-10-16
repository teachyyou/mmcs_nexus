package ru.sfedu.mmcs_nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.exceptions.EmailAlreadyTakenException;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
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

    public Page<User> getUsers(String sort, String order, Integer limit, Integer offset) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        //todo позже убрать необходимость кратности
        int page = offset / limit;

        Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sort));

        return userRepository.findAll(pageable);
    }


    public void saveUser(User user) {
        userRepository.saveAndFlush(user);
    }

    //for creating non-verified user with just github login
    public void saveUser(String githubLogin) {
        if (findByGithubLogin(githubLogin).isEmpty()) {
            saveUser(new User(githubLogin));
        }
    }

    public void updateUserInfo(String login, String email, String firstName, String lastName) {
        User user = findByGithubLogin(login).orElseThrow(
                () -> new UsernameNotFoundException(STR."User \{login} is not found")
        );

        //Проверка на то, что данная почта уже занята другим пользователем
        if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            throw new EmailAlreadyTakenException(email);
        }

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        if (user.getStatus() == UserEnums.UserStatus.NON_VERIFIED) {
            user.setStatus(UserEnums.UserStatus.VERIFIED);
        }

        userRepository.save(user);
    }

    public void deleteUserById(UUID id) {
        userRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    public Optional<User> findByGithubLogin(String githubLogin) {
        return userRepository.findByLogin(githubLogin).stream().findFirst();
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
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

}
