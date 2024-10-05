package ru.sfedu.mmcs_nexus.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
        response.put("isAuthenticated", authentication != null && authentication.isAuthenticated());
        return response;
    }

    @GetMapping(value = "/api/v1/auth/verify_status", produces="application/json")
    public Boolean verifyAuthStatus(Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            String githubLogin = oauthUser.getAttribute("login");

            return !userService.isNotFoundOrVerified(githubLogin);
        }

        return true;
    }

    @ResponseBody
    @GetMapping(value = "/api/v1/auth/user", produces="application/json")
    public Map<String, String> getUserInfo(Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();

        response.put("login", user.getAttribute("login"));
        response.put("name", user.getAttribute("name"));
        response.put("avatar_url", user.getAttribute("avatar_url"));

        return response;
    }

    @PostMapping("api/v1/auth/update-profile")
    public void updateProfile(Authentication authentication, @RequestBody User user) {

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String githubLogin = oauthUser.getAttribute("login");


        User existingUser = userService.findByGithubLogin(githubLogin)
                .orElseThrow(() -> new UsernameNotFoundException(STR."User with GitHub login \{githubLogin} not found"));

        existingUser.verifyExistingUser(user);
        userService.saveUser(existingUser);
    }
}
