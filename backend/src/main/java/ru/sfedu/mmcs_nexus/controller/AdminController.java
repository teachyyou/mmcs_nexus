package ru.sfedu.mmcs_nexus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sfedu.mmcs_nexus.user.User;
import ru.sfedu.mmcs_nexus.user.UserService;

import java.util.List;

@RestController
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/api/v1/admin/users/list", produces="application/json")
    public @ResponseBody List<User> getUsersList(Authentication authentication) {
        User currentUser = userService.findByGithubLogin(authentication)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return userService.getUsers();
//        // Return a filtered list of users, excluding the current user
//        return userService.getUsers().stream()
//                .filter(user -> !user.equals(currentUser))
//                .collect(Collectors.toList());
    }





}
