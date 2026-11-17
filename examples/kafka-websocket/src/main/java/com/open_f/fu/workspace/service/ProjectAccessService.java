package com.open_f.fu.workspace.service;

import java.util.List;
import com.open_f.fu.shared.persistence.PersistenceManager;
import com.open_f.fu.workspace.persistence.entity.ProjectColumnEntity;
import jakarta.ws.rs.ForbiddenException;

/** La aplicación todavía no define miembros: el ejemplo solo permite al propietario. */
public final class ProjectAccessService {
    public Long ownerOf(Long projectId) {
        return PersistenceManager.getEntityManagerFactory().callInTransaction(em ->
                em.createQuery("select p.user.id from ProjectEntity p where p.id = :id", Long.class)
                        .setParameter("id", projectId).getResultStream().findFirst().orElse(null));
    }

    public void requireOwner(Long userId, Long projectId) {
        if (userId == null || !userId.equals(ownerOf(projectId))) {
            throw new ForbiddenException("Sin acceso al proyecto");
        }
    }

    public List<ColumnView> columns(Long userId, Long projectId) {
        requireOwner(userId, projectId);
        return PersistenceManager.getEntityManagerFactory().callInTransaction(em ->
                em.createQuery("select c from ProjectColumnEntity c where c.project.id = :id "
                        + "order by c.positionY", ProjectColumnEntity.class)
                        .setParameter("id", projectId).getResultStream()
                        .map(c -> new ColumnView(c.getId(), c.getName())).toList());
    }

    public record ColumnView(Long id, String name) {}
}
