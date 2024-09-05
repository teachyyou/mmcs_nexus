package ru.sfedu.mmcs_nexus.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;

        //todo for testing purposes
        User user1 = new User("John", "Doe", "johndoe", 1, User.UserStatus.VERIFIED, UserRole.ROLE_USER);
        User user2 = new User("Jane", "Smith", "janesmith", 2, User.UserStatus.NON_VERIFIED, UserRole.ROLE_ADMIN);
        User user3 = new User("Alice", "Johnson", "alicej", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user4 = new User("Alice", "Johnson", "alicej1", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user5 = new User("Alice", "Johnson", "alicej2", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user6 = new User("Alice", "Johnson", "alicej3", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user7 = new User("Alice", "Johnson", "alicej4", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user8 = new User("Alice", "Johnson", "alicej5", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user9 = new User("Alice", "Johnson", "alicej6", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user10 = new User("Alice", "Johnson", "alicej7", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user11 = new User("Alice", "Johnson", "alicej8", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user12 = new User("Alice", "Johnson", "alicej9", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user13 = new User("Alice", "Johnson", "alicej10", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user14 = new User("Alice", "Johnson", "alicej11", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user15 = new User("Alice", "Johnson", "alicej12", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user16 = new User("Alice", "Johnson", "alicej3", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);

        // Сохраняем пользователей в базу данных
        saveUser(user1);
        saveUser(user2);
        saveUser(user3);
        saveUser(user4);
        saveUser(user5);
        saveUser(user6);
        saveUser(user7);
        saveUser(user8);
        saveUser(user9);
        saveUser(user10);
        saveUser(user11);
        saveUser(user12);
        saveUser(user13);
        saveUser(user14);
        saveUser(user15);
        saveUser(user16);

    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public void saveUser(User user) {
        userRepository.saveAndFlush(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public void saveNewUser(String githubLogin) {
        if (findByGithubLogin(githubLogin).isEmpty()) {
            saveUser(new User(githubLogin));
        }
    }

    public Optional<User> findByGithubLogin(String githubLogin) {
        return userRepository.findByLogin(githubLogin).stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByGithubLogin(Authentication authentication) {
        DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();
        String githubLogin = user.getAttribute("login");
        return userRepository.findByLogin(githubLogin).stream().findFirst();
    }

    public boolean isNotFoundOrVerified(String githubLogin) {
        Optional<User> optionalUser = findByGithubLogin(githubLogin);
        return optionalUser.isEmpty() || optionalUser.get().getStatus() == User.UserStatus.NON_VERIFIED;
    }

}
