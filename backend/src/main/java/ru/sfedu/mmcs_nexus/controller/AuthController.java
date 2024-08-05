package ru.sfedu.mmcs_nexus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.user.User;
import ru.sfedu.mmcs_nexus.user.UserService;

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

    @PostMapping("api/v1/auth/complete-profile")
    public void completeProfile(@RequestParam String githubLogin, @RequestBody User user) {
        user.setLogin(githubLogin);
        userService.saveUser(user);

    }
}
