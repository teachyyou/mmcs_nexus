package ru.sfedu.mmcs_nexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.model.entity.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    List<User> findByLogin(String login);
    List<User> findByEmail(String email);

}
