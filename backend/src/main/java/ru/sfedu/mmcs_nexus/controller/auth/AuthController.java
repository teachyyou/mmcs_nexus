package ru.sfedu.mmcs_nexus.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.data.user.User;
import ru.sfedu.mmcs_nexus.data.user.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @ResponseBody
    @GetMapping(value = "/api/v1/auth/status", produces="application/json")
    public Map<String, Boolean> getAuthStatus(Authentication authentication) {
        Map<String, Boolean> response = new HashMap<>();

        boolean isAuthenticated = authentication != null &&
                !(authentication instanceof AnonymousAuthenticationToken) &&
                authentication.isAuthenticated();

        response.put("isAuthenticated", isAuthenticated);
        System.out.println("LALA" + isAuthenticated);
        return response;
    }

    @GetMapping(value = "/api/v1/auth/verify_status", produces="application/json")
    public Boolean verifyAuthStatus(Authentication authentication) {

        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            String githubLogin = oauthUser.getAttribute("login");

            return !userService.isNotFoundOrVerified(githubLogin);
        }

        return true;
    }

    @ResponseBody
    @GetMapping(value = "/api/v1/auth/user", produces="application/json")
    public Map<String, Object> getUserInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        DefaultOAuth2User github_user = (DefaultOAuth2User) authentication.getPrincipal();

        String login = github_user.getAttribute("login");

        User existingUser = userService.findByGithubLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(STR."User with GitHub login \{login} not found"));

        response.put("login", login);
        response.put("github_name", github_user.getAttribute("name"));
        response.put("firstname", existingUser.getFirstName());
        response.put("lastname", existingUser.getLastName());
        response.put("group", existingUser.getUserGroup());
        response.put("course", existingUser.getUserCourse());
        response.put("avatar_url", github_user.getAttribute("avatar_url"));


        return response;
    }

    @PostMapping("api/v1/auth/update_profile")
    public void updateProfile(Authentication authentication, @RequestBody User user) {

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String githubLogin = oauthUser.getAttribute("login");


        User existingUser = userService.findByGithubLogin(githubLogin)
                .orElseThrow(() -> new UsernameNotFoundException(STR."User with GitHub login \{githubLogin} not found"));

        existingUser.verifyExistingUser(user);
        userService.saveUser(existingUser);
    }
}
