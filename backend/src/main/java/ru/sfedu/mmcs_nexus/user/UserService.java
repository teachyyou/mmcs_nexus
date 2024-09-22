package ru.sfedu.mmcs_nexus.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

        // Создаем пользователей для тестирования
        User user1 = new User("John", "Doe", "johndoe", 1, User.UserStatus.VERIFIED, UserRole.ROLE_USER);
        User user2 = new User("Jane", "Smith", "janesmith", 2, User.UserStatus.NON_VERIFIED, UserRole.ROLE_ADMIN);
        User user3 = new User("Alice", "Johnson", "alicej", 3, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user4 = new User("Bob", "Brown", "bobbrown", 4, User.UserStatus.VERIFIED, UserRole.ROLE_USER);
        User user5 = new User("Charlie", "Davis", "charlied", 5, User.UserStatus.NON_VERIFIED, UserRole.ROLE_USER);
        User user6 = new User("Diana", "Evans", "dianaev", 6, User.UserStatus.VERIFIED, UserRole.ROLE_ADMIN);
        User user7 = new User("Ethan", "Foster", "ethanf", 7, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user8 = new User("Fiona", "Green", "fionag", 8, User.UserStatus.NON_VERIFIED, UserRole.ROLE_USER);
        User user9 = new User("George", "Harris", "georgeh", 9, User.UserStatus.VERIFIED, UserRole.ROLE_USER);
        User user10 = new User("Hannah", "Irvine", "hannahir", 10, User.UserStatus.BLOCKED, UserRole.ROLE_ADMIN);
        User user11 = new User("Isaac", "James", "isaacj", 11, User.UserStatus.VERIFIED, UserRole.ROLE_USER);
        User user12 = new User("Julia", "Kane", "juliak", 12, User.UserStatus.NON_VERIFIED, UserRole.ROLE_USER);
        User user13 = new User("Kevin", "Lewis", "kevinl", 13, User.UserStatus.BLOCKED, UserRole.ROLE_USER);
        User user14 = new User("Lily", "Moore", "lilym", 14, User.UserStatus.VERIFIED, UserRole.ROLE_USER);
        User user15 = new User("Michael", "Nolan", "michaeln", 15, User.UserStatus.NON_VERIFIED, UserRole.ROLE_USER);
        User user16 = new User("Nina", "Owens", "ninao", 16, User.UserStatus.BLOCKED, UserRole.ROLE_USER);

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

    public List<User> getUsers(String sort, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return userRepository.findAll(Sort.by(direction, sort));
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
