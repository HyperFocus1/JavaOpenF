package com.open_f.fu.workspace.persistence.mapper;

import com.open_f.fu.workspace.domain.model.ProjectColumn;
import com.open_f.fu.workspace.persistence.entity.ProjectColumnEntity;
import com.open_f.fu.workspace.persistence.entity.ProjectEntity;

/**
 * Traduce entre {@link ProjectColumnEntity} y el modelo de dominio {@link ProjectColumn}.
 */
public final class ProjectColumnMapper {

    private ProjectColumnMapper() {
    }

    public static ProjectColumn toDomain(ProjectColumnEntity e) {
        return ProjectColumn.restore(e.getId(), e.getProject().getId(), e.getName(), e.getColor(), e.getPositionY());
    }

    public static ProjectColumnEntity toNewEntity(ProjectColumn c, ProjectEntity project) {
        return new ProjectColumnEntity(c.getName(), c.getColor(), c.getPosition(), project);
    }

    public static void copy(ProjectColumn from, ProjectColumnEntity to) {
        to.setName(from.getName());
        to.setColor(from.getColor());
        to.setPositionY(from.getPosition());
    }
}
