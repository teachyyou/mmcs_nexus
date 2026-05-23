package ru.sfedu.mmcs_nexus.controller.v1.admin;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.dto.entity.PostDTO;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.ChangePostPublishedRequestPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreatePostRequestPayload;
import ru.sfedu.mmcs_nexus.service.PostService;
import ru.sfedu.mmcs_nexus.util.ResponseUtils;

import static ru.sfedu.mmcs_nexus.util.ResponseUtils.buildPageResponse;

@RestController
public class AdminPostController {

    private final PostService postService;

    @Autowired
    public AdminPostController(PostService postService) {
        this.postService = postService;
    }


    @GetMapping(value = "/api/v1/admin/posts", produces = "application/json")
    public ResponseEntity<?> getPostsList(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            //todo пофиксить баг с фильтрацией по году
            @RequestParam(required = false) Integer year
    ) {
        PaginationPayload paginationPayload = new PaginationPayload(limit, offset, sort, order, EntitySort.POST_SORT);

        Page<PostDTO> posts = postService.findAll(year, paginationPayload);

        return buildPageResponse(posts);
    }

    @GetMapping(value = "/api/v1/admin/posts/{id}", produces = "application/json")
    public ResponseEntity<?> getPostById(@PathVariable("id") @UUID String postId) {
        PostDTO postDTO = new PostDTO(postService.find(postId));

        return ResponseEntity.ok(postDTO);
    }

    @PostMapping(value = "/api/v1/admin/posts", produces = "application/json")
    public ResponseEntity<?> create(
            @AuthenticationPrincipal OAuth2User user,
            @Valid @RequestBody CreatePostRequestPayload payload) {
        String githubLogin = user.getAttribute("login");
        postService.create(payload, githubLogin);

        return ResponseUtils.success(HttpStatus.OK, "saved successfully");
    }

    @PutMapping(value = "/api/v1/admin/posts/{id}", produces = "application/json")
    public ResponseEntity<PostDTO> edit(
            @PathVariable("id") @UUID String postId,
            @Valid @RequestBody CreatePostRequestPayload payload) {
        PostDTO postDTO = new PostDTO(postService.edit(payload, postId));

        return ResponseEntity.ok(postDTO);
    }

    @PatchMapping(value = "/api/v1/admin/posts/{id}", produces = "application/json")
    public ResponseEntity<?> changePublished(
            @PathVariable("id") @UUID String postId,
            @Valid @RequestBody ChangePostPublishedRequestPayload payload) {
        postService.changePublished(postId, payload.getPublished());

        return ResponseUtils.success(
                HttpStatus.OK,
                payload.getPublished() ? "published successfully" : "unpublished successfully"
        );
    }

}
