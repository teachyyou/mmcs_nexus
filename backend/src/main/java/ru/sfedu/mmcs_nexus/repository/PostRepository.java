package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.model.entity.Post;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("select p from Post p where function('year', p.createdAt) = :year")
    Page<Post> findAllByYear(@Param("year") Integer year, Pageable pageable);

    @Query("select p from Post p where function('year', p.createdAt) = :year")
    List<Post> findAllByYear(@Param("year") Integer year);

    @Query("""
            select post
            from Post post
            where post.isPublished = true
            """)
    Page<Post> findAllPublished(Pageable pageable);

    @Query("""
            select post
            from Post post
            where post.isPublished = true
              and extract(year from post.createdAt) = :year
            """)
    Page<Post> findAllPublishedByYear(Integer year, Pageable pageable);
}
