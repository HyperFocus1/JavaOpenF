package com.open_f.fu.workspace.persistence.repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.open_f.fu.shared.persistence.JpaRepository;
import com.open_f.fu.workspace.domain.exception.ProjectNotFoundException;
import com.open_f.fu.workspace.domain.interfaces.ProjectRepository;
import com.open_f.fu.workspace.domain.model.Project;
import com.open_f.fu.workspace.persistence.entity.ProjectEntity;
import com.open_f.fu.workspace.persistence.mapper.ProjectMapper;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public class JpaProjectRepository extends JpaRepository<ProjectEntity> implements ProjectRepository {

    private final EntityManager em;

    @Inject
    public JpaProjectRepository(EntityManager em) {
        super(ProjectEntity.class);
        this.em = em;
    }

    @Override
    public Project save(Project project) {
        if (project.getId() == null) {
            return ProjectMapper.toDomain(persist(this.em, ProjectMapper.toNewEntity(project)));
        }
        ProjectEntity e = findEntity(this.em, project.getId())
                .orElseThrow(() -> new ProjectNotFoundException(project.getId()));
        ProjectMapper.copy(project, e);
        return ProjectMapper.toDomain(e);
    }

    @Override
    public Optional<Project> findById(Long id) {
        return findEntity(this.em, id).map(ProjectMapper::toDomain);
    }

    @Override
    public List<Project> findAll() {
        return this.em.createQuery("SELECT p FROM ProjectEntity p ORDER BY p.createdAt DESC", ProjectEntity.class)
                .getResultStream()
                .map(ProjectMapper::toDomain)
                .toList();
    }

    @Override
    public List<Project> findByTitle(String text) {
        return this.em.createQuery("SELECT p FROM ProjectEntity p WHERE LOWER(p.title) LIKE :title", ProjectEntity.class)
                .setParameter("title", "%" + text.toLowerCase(Locale.ROOT) + "%")
                .getResultStream()
                .map(ProjectMapper::toDomain)
                .toList();
    }
}
