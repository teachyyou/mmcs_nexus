package ru.sfedu.mmcs_nexus.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.sfedu.mmcs_nexus.model.dto.entity.PostDTO;
import ru.sfedu.mmcs_nexus.model.entity.Post;
import ru.sfedu.mmcs_nexus.model.entity.UploadedFile;
import ru.sfedu.mmcs_nexus.model.entity.User;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;
import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
import ru.sfedu.mmcs_nexus.model.payload.admin.CreatePostRequestPayload;
import ru.sfedu.mmcs_nexus.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostsServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserService userService;

    @Mock
    private UploadedFileService uploadedFileService;

    @InjectMocks
    private PostService postService;

    @Test
    void shouldFindPostById() {
        Post post = createPost(true);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        Post result = postService.find(post.getId().toString());

        assertEquals(post, result);

        verify(postRepository).findById(post.getId());
    }

    @Test
    void shouldThrowWhenPostNotFound() {
        UUID postId = UUID.randomUUID();

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.find(postId.toString())
        );

        assertEquals("Post with id " + postId + " not found", exception.getMessage());

        verify(postRepository).findById(postId);
    }

    @Test
    void shouldFindAllPostsWithoutYear() {
        Post post = createPost(true);
        PaginationPayload paginationPayload = new PaginationPayload(10, 0, "id", "asc", EntitySort.POST_SORT);

        when(postRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));

        Page<PostDTO> result = postService.findAll(null, paginationPayload);

        assertEquals(1, result.getTotalElements());
        assertEquals(post.getId(), result.getContent().getFirst().getId());
        assertEquals(post.getTitle(), result.getContent().getFirst().getTitle());

        verify(postRepository).findAll(any(org.springframework.data.domain.Pageable.class));
        verify(postRepository, never()).findAllByYear(anyInt(), any());
    }

    @Test
    void shouldFindAllPostsByYear() {
        Post post = createPost(true);
        PaginationPayload paginationPayload = new PaginationPayload(10, 0, "id", "asc", EntitySort.POST_SORT);

        when(postRepository.findAllByYear(eq(2026), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));

        Page<PostDTO> result = postService.findAll(2026, paginationPayload);

        assertEquals(1, result.getTotalElements());
        assertEquals(post.getId(), result.getContent().getFirst().getId());

        verify(postRepository).findAllByYear(eq(2026), any(org.springframework.data.domain.Pageable.class));
        verify(postRepository, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void shouldFindPublishedPost() {
        Post post = createPost(true);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        Post result = postService.findPublished(post.getId().toString());

        assertEquals(post, result);

        verify(postRepository).findById(post.getId());
    }

    @Test
    void shouldThrowWhenPostIsNotPublished() {
        Post post = createPost(false);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.findPublished(post.getId().toString())
        );

        assertEquals("Post with id " + post.getId() + " not found", exception.getMessage());

        verify(postRepository).findById(post.getId());
    }

    @Test
    void shouldFindAllPublishedPostsWithoutYear() {
        Post post = createPost(true);
        PaginationPayload paginationPayload = new PaginationPayload(10, 0, "publishedAt", "desc", EntitySort.POST_SORT);

        when(postRepository.findAllPublished(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));

        Page<PostDTO> result = postService.findAllPublished(null, paginationPayload);

        assertEquals(1, result.getTotalElements());
        assertEquals(post.getId(), result.getContent().getFirst().getId());

        verify(postRepository).findAllPublished(any(org.springframework.data.domain.Pageable.class));
        verify(postRepository, never()).findAllPublishedByYear(anyInt(), any());
    }

    @Test
    void shouldFindAllPublishedPostsByYear() {
        Post post = createPost(true);
        PaginationPayload paginationPayload = new PaginationPayload(10, 0, "publishedAt", "desc", EntitySort.POST_SORT);

        when(postRepository.findAllPublishedByYear(eq(2026), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));

        Page<PostDTO> result = postService.findAllPublished(2026, paginationPayload);

        assertEquals(1, result.getTotalElements());
        assertEquals(post.getId(), result.getContent().getFirst().getId());

        verify(postRepository).findAllPublishedByYear(eq(2026), any(org.springframework.data.domain.Pageable.class));
        verify(postRepository, never()).findAllPublished(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void shouldCreatePublishedPost() {
        User user = createUser();
        UploadedFile file = createUploadedFile("image/png", false);
        CreatePostRequestPayload payload = createPayload(file.getId(), true);

        when(userService.findByGithubLogin("teachyyou")).thenReturn(Optional.of(user));
        when(uploadedFileService.find(file.getId().toString())).thenReturn(file);

        postService.create(payload, "teachyyou");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        verify(postRepository).save(postCaptor.capture());
        verify(uploadedFileService).setAttached(file.getId());

        Post savedPost = postCaptor.getValue();

        assertEquals("Тестовая новость", savedPost.getTitle());
        assertEquals("Краткое описание", savedPost.getPreviewText());
        assertEquals("<p>Содержимое новости</p>", savedPost.getContentHtml());
        assertEquals(file, savedPost.getBannerFile());
        assertTrue(savedPost.isPublished());
        assertNotNull(savedPost.getPublishedAt());
        assertEquals(user, savedPost.getAuthor());
    }

    @Test
    void shouldCreateDraftPost() {
        User user = createUser();
        UploadedFile file = createUploadedFile("image/jpeg", false);
        CreatePostRequestPayload payload = createPayload(file.getId(), false);

        when(userService.findByGithubLogin("teachyyou")).thenReturn(Optional.of(user));
        when(uploadedFileService.find(file.getId().toString())).thenReturn(file);

        postService.create(payload, "teachyyou");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        verify(postRepository).save(postCaptor.capture());
        verify(uploadedFileService).setAttached(file.getId());

        Post savedPost = postCaptor.getValue();

        assertFalse(savedPost.isPublished());
        assertNull(savedPost.getPublishedAt());
    }

    @Test
    void shouldNotAttachFileAgainWhenCreatingPostWithAlreadyAttachedFile() {
        User user = createUser();
        UploadedFile file = createUploadedFile("image/png", true);
        CreatePostRequestPayload payload = createPayload(file.getId(), true);

        when(userService.findByGithubLogin("teachyyou")).thenReturn(Optional.of(user));
        when(uploadedFileService.find(file.getId().toString())).thenReturn(file);

        postService.create(payload, "teachyyou");

        verify(postRepository).save(any(Post.class));
        verify(uploadedFileService, never()).setAttached(any(UUID.class));
    }

    @Test
    void shouldThrowWhenCreatingPostWithUnknownUser() {
        UUID bannerFileId = UUID.randomUUID();
        CreatePostRequestPayload payload = createPayload(bannerFileId, true);

        when(userService.findByGithubLogin("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> postService.create(payload, "unknown")
        );

        assertEquals("User unknown is not found", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
        verify(uploadedFileService, never()).find(anyString());
    }

    @Test
    void shouldThrowWhenCreatingPostWithNonImageFile() {
        User user = createUser();
        UploadedFile file = createUploadedFile("application/pdf", false);
        CreatePostRequestPayload payload = createPayload(file.getId(), true);

        when(userService.findByGithubLogin("teachyyou")).thenReturn(Optional.of(user));
        when(uploadedFileService.find(file.getId().toString())).thenReturn(file);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.create(payload, "teachyyou")
        );

        assertEquals("Banner file must be an image", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
        verify(uploadedFileService, never()).setAttached(any(UUID.class));
    }

    @Test
    void shouldThrowWhenCreatingPostWithFileWithoutContentType() {
        User user = createUser();
        UploadedFile file = createUploadedFile(null, false);
        CreatePostRequestPayload payload = createPayload(file.getId(), true);

        when(userService.findByGithubLogin("teachyyou")).thenReturn(Optional.of(user));
        when(uploadedFileService.find(file.getId().toString())).thenReturn(file);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.create(payload, "teachyyou")
        );

        assertEquals("Banner file must be an image", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldEditPost() {
        Post post = createPost(true);
        UploadedFile file = createUploadedFile("image/webp", false);
        CreatePostRequestPayload payload = createPayload(file.getId(), true);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(uploadedFileService.find(file.getId().toString())).thenReturn(file);

        Post result = postService.edit(payload, post.getId().toString());

        assertEquals(post, result);
        assertEquals("Тестовая новость", post.getTitle());
        assertEquals("Краткое описание", post.getPreviewText());
        assertEquals("<p>Содержимое новости</p>", post.getContentHtml());
        assertEquals(file, post.getBannerFile());

        verify(postRepository).save(post);
        verify(uploadedFileService).setAttached(file.getId());
    }

    @Test
    void shouldNotAttachFileAgainWhenEditingPostWithAlreadyAttachedFile() {
        Post post = createPost(true);
        UploadedFile file = createUploadedFile("image/png", true);
        CreatePostRequestPayload payload = createPayload(file.getId(), true);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(uploadedFileService.find(file.getId().toString())).thenReturn(file);

        postService.edit(payload, post.getId().toString());

        verify(postRepository).save(post);
        verify(uploadedFileService, never()).setAttached(any(UUID.class));
    }

    @Test
    void shouldThrowWhenEditingUnknownPost() {
        UUID postId = UUID.randomUUID();
        UUID bannerFileId = UUID.randomUUID();
        CreatePostRequestPayload payload = createPayload(bannerFileId, true);

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.edit(payload, postId.toString())
        );

        assertEquals("Post with id " + postId + " not found", exception.getMessage());

        verify(uploadedFileService, never()).find(anyString());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shouldThrowWhenEditingPostWithNonImageFile() {
        Post post = createPost(true);
        UploadedFile file = createUploadedFile("application/pdf", false);
        CreatePostRequestPayload payload = createPayload(file.getId(), true);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(uploadedFileService.find(file.getId().toString())).thenReturn(file);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.edit(payload, post.getId().toString())
        );

        assertEquals("Banner file must be an image", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
        verify(uploadedFileService, never()).setAttached(any(UUID.class));
    }

    @Test
    void shouldPublishPost() {
        Post post = createPost(false);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        postService.changePublished(post.getId().toString(), true);

        assertTrue(post.isPublished());
        assertNotNull(post.getPublishedAt());

        verify(postRepository).save(post);
    }

    @Test
    void shouldUnpublishPost() {
        Post post = createPost(true);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        postService.changePublished(post.getId().toString(), false);

        assertFalse(post.isPublished());
        assertNull(post.getPublishedAt());

        verify(postRepository).save(post);
    }

    @Test
    void shouldThrowWhenChangingPublishedForUnknownPost() {
        UUID postId = UUID.randomUUID();

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.changePublished(postId.toString(), true)
        );

        assertEquals("Post with id " + postId + " not found", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
    }

    private Post createPost(boolean published) {
        User author = createUser();
        UploadedFile bannerFile = createUploadedFile("image/png", true);

        Post post = new Post();

        post.setId(UUID.randomUUID());
        post.setTitle("Старая новость");
        post.setPreviewText("Старое описание");
        post.setContentHtml("<p>Старое содержимое</p>");
        post.setPublished(published);
        post.setPublishedAt(published ? LocalDateTime.of(2026, 5, 20, 12, 0) : null);
        post.setCreatedAt(LocalDateTime.of(2026, 5, 20, 10, 0));
        post.setUpdatedAt(LocalDateTime.of(2026, 5, 20, 11, 0));
        post.setAuthor(author);
        post.setBannerFile(bannerFile);

        return post;
    }

    private User createUser() {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setLogin("teachyyou");

        return user;
    }

    private UploadedFile createUploadedFile(String contentType, boolean attached) {
        UploadedFile file = new UploadedFile();

        file.setId(UUID.randomUUID());
        file.setOriginalFilename("banner.png");
        file.setStoragePath("/tmp/banner.png");
        file.setContentType(contentType);
        file.setSizeBytes(1024L);
        file.setAttached(attached);

        return file;
    }

    private CreatePostRequestPayload createPayload(UUID bannerFileId, boolean published) {
        CreatePostRequestPayload payload = new CreatePostRequestPayload();

        payload.setTitle("Тестовая новость");
        payload.setPreviewText("Краткое описание");
        payload.setContentHtml("<p>Содержимое новости</p>");
        payload.setBannerFileId(bannerFileId);
        payload.setPublished(published);

        return payload;
    }
}