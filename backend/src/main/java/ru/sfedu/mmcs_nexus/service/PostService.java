package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.sfedu.mmcs_nexus.model.dto.entity.PostDTO;
import ru.sfedu.mmcs_nexus.model.entity.Post;
import ru.sfedu.mmcs_nexus.model.entity.UploadedFile;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreatePostRequestPayload;
import ru.sfedu.mmcs_nexus.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;

    private final UploadedFileService uploadedFileService;

    @Autowired
    public PostService(PostRepository postRepository, UserService userService, UploadedFileService uploadedFileService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.uploadedFileService = uploadedFileService;
    }

    public Post find(String postId) {
        return findById(postId);
    }

    public Page<PostDTO> findAll(Integer year, PaginationPayload paginationPayload) {
        Pageable pageable = paginationPayload.getPageable();

        Page<Post> posts = year == null
                ? postRepository.findAll(pageable)
                : postRepository.findAllByYear(year, pageable);

        return posts.map(PostDTO::new);
    }

    public Post findPublished(String postId) {
        Post post = findById(postId);

        if (!post.isPublished()) {
            throw new EntityNotFoundException("Post with id " + postId + " not found");
        }

        return post;
    }

    public Page<PostDTO> findAllPublished(Integer year, PaginationPayload paginationPayload) {
        Pageable pageable = paginationPayload.getPageable();

        Page<Post> posts = year == null
                ? postRepository.findAllPublished(pageable)
                : postRepository.findAllPublishedByYear(year, pageable);

        return posts.map(PostDTO::new);
    }

    @Transactional
    public void create(CreatePostRequestPayload payload, String githubLogin) {
        User user = userService.findByGithubLogin(githubLogin).orElseThrow(() -> new UsernameNotFoundException("User " + githubLogin + " is not found"));

        UploadedFile file = uploadedFileService.find(payload.getBannerFileId().toString());

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Banner file must be an image");
        }

        Post post = new Post(
            payload.getTitle(),
            payload.getPreviewText(),
            payload.getContentHtml(),
            file,
            payload.getPublished(),
            user,
            payload.getPublished() ? LocalDateTime.now() : null
        );

        postRepository.save(post);

        if (!file.isAttached()) {
            uploadedFileService.setAttached(file.getId());
        }

    }

    @Transactional
    public Post edit(CreatePostRequestPayload payload, String postId) {
        Post post = findById(postId);

        UploadedFile file = uploadedFileService.find(payload.getBannerFileId().toString());

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Banner file must be an image");
        }

        post.setTitle(payload.getTitle());
        post.setPreviewText(payload.getPreviewText());
        post.setContentHtml(payload.getContentHtml());
        post.setBannerFile(file);

        postRepository.save(post);

        if (!file.isAttached()) {
            uploadedFileService.setAttached(file.getId());
        }

        return post;

    }

    @Transactional
    public void changePublished(String postId, boolean isPublished) {
        Post post = findById(postId);

        post.setPublished(isPublished);
        post.setPublishedAt(isPublished ? LocalDateTime.now() : null);

        postRepository.save(post);
    }

    private Post findById(String postId) {
        return postRepository.findById(UUID.fromString(postId))
                .orElseThrow(() -> new EntityNotFoundException("Post with id " + postId + " not found"));
    }
}
