package com.open_f.fu.workspace.persistence.mapper;

import com.open_f.fu.workspace.domain.model.Project;
import com.open_f.fu.workspace.persistence.entity.ProjectEntity;

/**
 * Traduce entre {@link ProjectEntity} y el modelo de dominio {@link Project}.
 */
public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static Project toDomain(ProjectEntity e) {
        return Project.restore(e.getId(), e.getOwnerId(), e.getTitle(), e.getDescription());
    }

    public static ProjectEntity toNewEntity(Project p) {
        return new ProjectEntity(p.getOwnerId(), p.getTitle(), p.getDescription());
    }

    public static void copy(Project from, ProjectEntity to) {
        to.setTitle(from.getTitle());
        to.setDescription(from.getDescription());
    }
}
