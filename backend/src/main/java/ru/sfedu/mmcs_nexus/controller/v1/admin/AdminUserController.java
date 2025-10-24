package ru.sfedu.mmcs_nexus.controller.v1.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.EditUserRequestPayload;
import ru.sfedu.mmcs_nexus.service.UserService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import java.util.Map;

import static ru.sfedu.mmcs_nexus.util.ResponseUtils.buildPageResponse;

@Validated
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
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset) {

        PaginationPayload paginationPayload = new PaginationPayload(limit, offset, sort, order, EntitySort.USER_SORT);

        Page<User> users = userService.getUsers(paginationPayload);

        return buildPageResponse(users);

    }

    @GetMapping(value = "/api/v1/admin/users/{id}", produces = "application/json")
    public ResponseEntity<User> getUserById(@PathVariable("id") @UUID String id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."User with id \{id} not found"));

        return ResponseEntity.ok(user);
    }

    @PutMapping(value = "/api/v1/admin/users/{id}", produces = "application/json")
    public ResponseEntity<User> editUserById(@PathVariable("id") @UUID String id, @Valid @RequestBody EditUserRequestPayload payload) {
        User user = userService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."User with id \{id} not found"));

        user = userService.editUser(user, payload);

        return ResponseEntity.ok(user);
    }

    @DeleteMapping(value = "/api/v1/admin/users/{id}")
    public ResponseEntity<Void> blockUserById(@PathVariable("id") @UUID String id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."User with id \{id} not found"));

        userService.blockUser(user);

        return ResponseEntity.noContent().build();
    }
}
