package ru.sfedu.mmcs_nexus.controller.publicapi;

import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sfedu.mmcs_nexus.config.ApplicationConfig;
import ru.sfedu.mmcs_nexus.model.dto.entity.PostDTO;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.service.PostService;

import static ru.sfedu.mmcs_nexus.util.ResponseUtils.buildPageResponse;

@RestController
public class PublicPostsController {

    private final PostService postService;

    @Autowired
    public PublicPostsController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping(value = "/api/v1/public/posts", produces = "application/json")
    public ResponseEntity<?> getPostsList(
            @RequestParam(defaultValue = "publishedAt") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_LIMIT) Integer limit,
            @RequestParam(defaultValue = ApplicationConfig.DEFAULT_OFFSET) Integer offset,
            @RequestParam(required = false) Integer year
    ) {
        PaginationPayload paginationPayload = new PaginationPayload(limit, offset, sort, order, EntitySort.POST_SORT);

        Page<PostDTO> posts = postService.findAllPublished(year, paginationPayload);

        return buildPageResponse(posts);
    }

    @GetMapping(value = "/api/v1/public/posts/{id}", produces = "application/json")
    public ResponseEntity<PostDTO> getPostById(@PathVariable("id") @UUID String postId) {
        PostDTO postDTO = new PostDTO(postService.findPublished(postId));

        return ResponseEntity.ok(postDTO);
    }
}