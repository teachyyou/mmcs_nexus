package ru.sfedu.mmcs_nexus.controller.v1.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class AdminUserController {

    private final UserService userService;

    @Autowired
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/api/v1/admin/users", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getUsersList(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "limit") Integer limit,
            @RequestParam(defaultValue = "offset") Integer offset) {

        Page<User> users = userService.getUsers(sort, order, limit, offset);

        Map<String, Object> response = new HashMap<>();
        response.put("content", users.getContent());
        response.put("totalElements", users.getTotalElements());

        return ResponseEntity.ok().body(response);
    }


    @GetMapping(value = "/api/v1/admin/users/{id}", produces = "application/json")
    public ResponseEntity<User> getUserById(@PathVariable("id") UUID id, Authentication authentication) {
        User user = userService.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ResponseEntity.ok(user);
    }

    @PutMapping(value = "/api/v1/admin/users/{id}", produces = "application/json")
    public ResponseEntity<User> editUserById(@PathVariable("id") UUID id, Authentication authentication, @RequestBody User user) {

        User existingUser = userService.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(STR."User with id \{id} not found"));

        existingUser.editExistingUser(user);
        userService.saveUser(existingUser);

        return ResponseEntity.ok(existingUser);
    }

    @DeleteMapping(value = "/api/v1/admin/users/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") UUID id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteUserById(id);

        return ResponseEntity.noContent().build();
    }



}
