package ru.sfedu.mmcs_nexus.data.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    //todo change to Optional<User>
    List<User> findByLogin(String login);
    User findByFirstName(String login);

}
