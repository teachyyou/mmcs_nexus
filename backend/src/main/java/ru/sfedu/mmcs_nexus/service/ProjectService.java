    package ru.sfedu.mmcs_nexus.service;

    import jakarta.persistence.EntityExistsException;
    import jakarta.persistence.EntityNotFoundException;
    import jakarta.transaction.Transactional;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.dao.EmptyResultDataAccessException;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.stereotype.Service;
    import ru.sfedu.mmcs_nexus.model.entity.Event;
    import ru.sfedu.mmcs_nexus.model.entity.Project;
    import ru.sfedu.mmcs_nexus.model.internal.PaginationPayload;
    import ru.sfedu.mmcs_nexus.model.payload.admin.CreateProjectRequestPayload;
    import ru.sfedu.mmcs_nexus.repository.ProjectRepository;

    import java.util.List;
    import java.util.UUID;

    @Service
    public class ProjectService {

        private final ProjectRepository projectRepository;

        @Autowired
        public ProjectService(ProjectRepository projectRepository)
        {
            this.projectRepository = projectRepository;
        }

        public Page<Project> findAll(Integer year, PaginationPayload paginationPayload) {
            Pageable pageable = paginationPayload.getPageable();

            if (year != null) {
                return projectRepository.findAllByYear(year, pageable);
            }

            return projectRepository.findAll(pageable);
        }

        public List<Project> findAll(Integer year) {
            return projectRepository.findAllByYear(year);
        }

        public List<Project> findAll(List<UUID> ids) {
            return projectRepository.findAllById(ids);
        }

        public Project find(String projectId) {
            return getById(projectId);
        }

        @Transactional
        public void create(CreateProjectRequestPayload payload) {
            if (existsByName(payload.getName())) {
                throw new EntityExistsException(STR."Project with name \{payload.getName()} already exists");
            }

            Project project = new Project(
                    payload.getExternalId(),
                    payload.getQuantityOfStudents(),
                    payload.getCaptainName(),
                    payload.isFull(),
                    payload.getTrack(),
                    payload.getTechnologies(),
                    payload.getName(),
                    payload.getDescription(),
                    payload.getType(),
                    payload.getYear()
            );

            projectRepository.save(project);
        }

        @Transactional
        public Project edit(String projectId, CreateProjectRequestPayload payload) {
            Project project = getById(projectId);

            if (projectRepository.existsByNameAndIdNot(payload.getName(), project.getId())) {
                throw new EntityExistsException(STR."Project with name \{payload.getName()} already exists");
            }

            project.setName(payload.getName());
            project.setDescription(payload.getDescription());
            project.setType(payload.getType());
            project.setYear(payload.getYear());
            project.setExternalId(payload.getExternalId());
            project.setQuantityOfStudents(payload.getQuantityOfStudents());
            project.setCaptainName(payload.getCaptainName());
            project.setFull(payload.isFull());
            project.setTrack(payload.getTrack());
            project.setTechnologies(payload.getTechnologies());

            return project;
        }

        public void deleteById(String projectId) {
            try {
                projectRepository.deleteById(UUID.fromString(projectId));
            } catch (EmptyResultDataAccessException e) {
                throw new EntityNotFoundException(STR."Project with id \{projectId} not found");
            }
        }

        public boolean existsById(UUID id) {
            return projectRepository.existsById(id);
        }

        private Project getById(String projectId) {
            return projectRepository.findById(UUID.fromString(projectId))
                    .orElseThrow(() -> new EntityNotFoundException(STR."Project with id \{projectId} not found"));
        }

        public boolean existsByName(String name) {
            return projectRepository.existsByName(name);
        }

    }
