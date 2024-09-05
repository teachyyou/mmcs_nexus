package ru.sfedu.mmcs_nexus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
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

//    @GetMapping(value = "/api/v1/admin/users/list", produces="application/json")
//    public @ResponseBody List<User> getUsersList(Authentication authentication) {
//        User currentUser = userService.findByGithubLogin(authentication)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//        System.out.println("WOWOWO");
//        return userService.getUsers();
//        // Return a filtered list of users, excluding the current user
//        return userService.getUsers().stream()
//                .filter(user -> !user.equals(currentUser))
//                .collect(Collectors.toList());
//    }

    @GetMapping(value = "/api/v1/admin/users/list", produces = "application/json")
    public ResponseEntity<List<User>> getUsersList(Authentication authentication) {
        List<User> users = userService.getUsers();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(users.size()));

        System.out.println("LOLOLO");
        return ResponseEntity.ok().headers(headers).body(users);
    }

    @GetMapping(value = "/api/v1/admin/users/list/{id}", produces = "application/json")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id, Authentication authentication) {
        User user = userService.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(user);
    }

    @PutMapping(value = "/api/v1/admin/users/list/{id}", produces = "application/json")
    public ResponseEntity<User> editUserById(@PathVariable("id") Long id, Authentication authentication, @RequestBody User user) {

        User existingUser = userService.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(STR."User with id \{id} not found"));

        existingUser.editExistingUser(user);
        userService.saveUser(existingUser);

        return ResponseEntity.ok(existingUser);
    }

    @DeleteMapping(value = "/api/v1/admin/users/list/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") Long id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteUserById(id);

        return ResponseEntity.noContent().build();
    }



}
