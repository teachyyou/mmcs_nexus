package ru.sfedu.mmcs_nexus.controller.v1.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.model.payload.user.UpdateProfileRequestPayload;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.entity.UserEnums;
import ru.sfedu.mmcs_nexus.service.UserService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @ResponseBody
    @GetMapping(value = "/api/v1/auth/status", produces="application/json")
    public ResponseEntity<Map<String, Object>> getAuthStatus(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        boolean isAuthenticated = authentication != null &&
                !(authentication instanceof AnonymousAuthenticationToken) &&
                authentication.isAuthenticated();

        response.put("isAuthenticated", isAuthenticated);

        UserEnums.UserStatus status = null;
        UserEnums.UserRole role = UserEnums.UserRole.ROLE_GUEST;
        Map<String, Object> userMap = null;

        if (isAuthenticated) {
            Optional<User> optionalUser = userService.findByGithubLogin(authentication);
            if (optionalUser.isEmpty()) {
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            User user = optionalUser.get();
            status = user.getStatus();
            role = user.getRole();

            DefaultOAuth2User githubUser = (DefaultOAuth2User) authentication.getPrincipal();

            userMap = new HashMap<>();
            userMap.put("login", githubUser.getAttribute("login"));
            userMap.put("github_name", githubUser.getAttribute("name"));
            userMap.put("firstname", user.getFirstName());
            userMap.put("lastname", user.getLastName());
            userMap.put("email", user.getEmail());
            userMap.put("avatar_url", githubUser.getAttribute("avatar_url"));

            response.put("userId", user.getId());

        }
        response.put("userStatus", status);
        response.put("userRole", role);
        response.put("user", userMap);


        return ResponseEntity.ok(response);
    }

    //todo remove this and use only above
    @ResponseBody
    @GetMapping(value = "/api/v1/auth/user", produces="application/json")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            DefaultOAuth2User github_user = (DefaultOAuth2User) authentication.getPrincipal();
            String login = github_user.getAttribute("login");

            Optional<User> optionalUser = userService.findByGithubLogin(login);
            if (optionalUser.isEmpty()) {
                response.put("error", STR."User with GitHub login \{login} not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User existingUser = optionalUser.get();
            response.put("login", login);
            response.put("github_name", github_user.getAttribute("name"));
            response.put("firstname", existingUser.getFirstName());
            response.put("lastname", existingUser.getLastName());
            response.put("email", existingUser.getEmail());
            response.put("avatar_url", github_user.getAttribute("avatar_url"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //todo сделать нормальное обновление инфы через POST/PUT
    @PutMapping("api/v1/auth/update_profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal OAuth2User oauthUser, @Valid @RequestBody UpdateProfileRequestPayload userDTO) {
        String githubLogin = oauthUser.getAttribute("login");

        userService.updateUserInfo(githubLogin, userDTO.getEmail(), userDTO.getFirstName(), userDTO.getLastName());

        return ResponseUtils.success(HttpStatus.OK, "success");
    }

    //Очень полезная штука чтобы залогиниться под другим пользователем, не забыть убрать потом
    @Profile("test")
    @GetMapping("/api/v1/auth/test-login")
    public ResponseEntity<String> loginAs(
            @RequestParam String githubLogin,
            HttpServletRequest request) {

        return userService.findByGithubLogin(githubLogin)
                .map(user -> {
                    // Создаем аутентификацию
                    var principal = new DefaultOAuth2User(
                            List.of(new SimpleGrantedAuthority(user.getRole().name())),
                            Map.of(
                                    "login", user.getLogin(),
                                    "id", user.getId().toString()
                            ),
                            "login"
                    );

                    Authentication auth = new OAuth2AuthenticationToken(
                            principal,
                            principal.getAuthorities(),
                            "github"
                    );

                    // Устанавливаем в SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // Создаем сессию, если нужно
                    HttpSession session = request.getSession(true);
                    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

                    return ResponseEntity.ok()
                            .body("Successfully logged in as: " + user.getLogin());
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with login: " + githubLogin));
    }

}
