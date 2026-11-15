package com.open_f.fu.workspace.persistence.mapper;

import com.open_f.fu.account.persistence.entity.UserEntity;
import com.open_f.fu.workspace.domain.model.Project;
import com.open_f.fu.workspace.persistence.entity.ProjectEntity;

/**
 * Traduce entre {@link ProjectEntity} y el modelo de dominio {@link Project}.
 */
public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static Project toDomain(ProjectEntity e) {
        return Project.restore(e.getId(), e.getUser().getId(), e.getTitle(), e.getDescription());
    }

    public static ProjectEntity toNewEntity(Project p, UserEntity user) {
        return new ProjectEntity(user, p.getTitle(), p.getDescription());
    }

    public static void copy(Project from, ProjectEntity to) {
        to.setTitle(from.getTitle());
        to.setDescription(from.getDescription());
    }
}
