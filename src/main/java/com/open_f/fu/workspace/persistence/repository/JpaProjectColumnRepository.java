package com.open_f.fu.workspace.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.open_f.fu.shared.persistence.JpaRepository;
import com.open_f.fu.workspace.domain.exception.ColumnNotFoundException;
import com.open_f.fu.workspace.domain.interfaces.ProjectColumnRepository;
import com.open_f.fu.workspace.domain.model.ProjectColumn;
import com.open_f.fu.workspace.persistence.entity.ProjectColumnEntity;
import com.open_f.fu.workspace.persistence.entity.ProjectEntity;
import com.open_f.fu.workspace.persistence.mapper.ProjectColumnMapper;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public class JpaProjectColumnRepository extends JpaRepository<ProjectColumnEntity>
        implements ProjectColumnRepository {

    private final EntityManager em;

    @Inject
    public JpaProjectColumnRepository(EntityManager em) {
        super(ProjectColumnEntity.class);
        this.em = em;
    }

    @Override
    public ProjectColumn save(ProjectColumn column) {
        if (column.getId() == null) {
            ProjectEntity project = this.em.getReference(ProjectEntity.class, column.getProjectId());
            return ProjectColumnMapper.toDomain(persist(this.em, ProjectColumnMapper.toNewEntity(column, project)));
        }
        ProjectColumnEntity e = findEntity(this.em, column.getId())
                .orElseThrow(() -> new ColumnNotFoundException(column.getId(), column.getProjectId()));
        ProjectColumnMapper.copy(column, e);
        return ProjectColumnMapper.toDomain(e);
    }

    @Override
    public List<ProjectColumn> findAllByProjectId(Long projectId) {
        return this.em.createQuery("""
                SELECT pc FROM ProjectColumnEntity pc
                WHERE pc.project.id = :projectId
                ORDER BY pc.positionY ASC
                """, ProjectColumnEntity.class)
                .setParameter("projectId", projectId)
                .getResultStream()
                .map(ProjectColumnMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProjectColumn> findByIdAndProjectId(Long columnId, Long projectId) {
        return this.em.createQuery("""
                SELECT pc FROM ProjectColumnEntity pc
                WHERE pc.id = :columnId AND pc.project.id = :projectId
                """, ProjectColumnEntity.class)
                .setParameter("columnId", columnId)
                .setParameter("projectId", projectId)
                .getResultStream()
                .findFirst()
                .map(ProjectColumnMapper::toDomain);
    }
}
