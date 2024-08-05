package ru.sfedu.mmcs_nexus.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.user.User;
import ru.sfedu.mmcs_nexus.user.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
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

    @PostMapping("api/v1/auth/verify_status")
    public String verifyAuthStatus(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {

        if (authentication != null && authentication.isAuthenticated()) {
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            String githubLogin = oauthUser.getAttribute("login");

            if (userService.findByGithubLogin(githubLogin).isEmpty()) {
                logoutHandler.logout(request, response, authentication);
                Cookie cookie = new Cookie("JSESSIONID", null);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(0); 
                response.addCookie(cookie);

                return "User not found, logged out";
            }
        }

        return "User verified";
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
