package com.open_f.fu.workspace.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.open_f.fu.shared.persistence.JpaRepository;
import com.open_f.fu.workspace.domain.exception.TaskNotFoundException;
import com.open_f.fu.workspace.domain.interfaces.TaskRepository;
import com.open_f.fu.workspace.domain.model.Task;
import com.open_f.fu.workspace.persistence.entity.ProjectColumnEntity;
import com.open_f.fu.workspace.persistence.entity.ProjectEntity;
import com.open_f.fu.workspace.persistence.entity.TaskEntity;
import com.open_f.fu.workspace.persistence.mapper.TaskMapper;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public class JpaTaskRepository extends JpaRepository<TaskEntity> implements TaskRepository {

    private final EntityManager em;

    @Inject
    public JpaTaskRepository(EntityManager em) {
        super(TaskEntity.class);
        this.em = em;
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            ProjectEntity project = this.em.getReference(ProjectEntity.class, task.getProjectId());
            ProjectColumnEntity column = this.em.getReference(ProjectColumnEntity.class, task.getColumnId());
            return TaskMapper.toDomain(persist(this.em, TaskMapper.toNewEntity(task, project, column)));
        }
        TaskEntity e = findEntity(this.em, task.getId()).orElseThrow(() -> new TaskNotFoundException(task.getId()));
        TaskMapper.copy(task, e);
        return TaskMapper.toDomain(e);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return findEntity(this.em, id).map(TaskMapper::toDomain);
    }

    @Override
    public List<Double> findPositionsByColumnId(Long columnId) {
        return this.em.createQuery("""
                SELECT t.positionY FROM TaskEntity t
                WHERE t.column.id = :columnId AND t.positionY IS NOT NULL
                ORDER BY t.positionY ASC
                """, Double.class).setParameter("columnId", columnId).getResultList();
    }

    @Override
    public List<Task> findTasksByProjectId(Long projectId) {
        return this.em.createQuery("""
                SELECT t FROM TaskEntity  t WHERE t.project.id = :projectId
                """, TaskEntity.class).setParameter("projectId", projectId).getResultStream().map(TaskMapper::toDomain).toList();
    }

    @Override
    public boolean existsByProjectId(Task task, Long projectId) {
        return this.em.createQuery("""
                SELECT COUNT(t) FROM TaskEntity  t WHERE t.id = :taskId AND t.project.id = :projectId
                """, Long.class).setParameter("taskId", task.getId()).setParameter("projectId", projectId).getSingleResult() > 0;
    }
}
