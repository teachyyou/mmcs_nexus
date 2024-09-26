package ru.sfedu.mmcs_nexus.data.jury_to_project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sfedu.mmcs_nexus.data.user.User;

import java.util.List;

@Repository
public interface ProjectJuryRepository extends JpaRepository<ProjectJury, ProjectJuryKey> {

    List<ProjectJury> findByJuries(User user);
}
