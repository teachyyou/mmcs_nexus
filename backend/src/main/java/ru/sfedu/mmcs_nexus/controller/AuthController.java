package ru.sfedu.mmcs_nexus.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @ResponseBody
    @GetMapping(value = "/api/auth/status", produces="application/json")
    public Map<String, Boolean> getAuthStatus(Authentication authentication) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAuthenticated", authentication != null && authentication.isAuthenticated());
        return response;
    }
}
