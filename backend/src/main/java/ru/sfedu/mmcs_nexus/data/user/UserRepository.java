package ru.sfedu.mmcs_nexus.data.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    //todo change to Optional<User>
    List<User> findByLogin(String login);
    User findByFirstName(String login);

}
