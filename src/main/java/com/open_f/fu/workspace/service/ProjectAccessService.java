package com.open_f.fu.workspace.service;

import com.open_f.fu.shared.domain.ForbiddenException;
import com.open_f.fu.workspace.domain.interfaces.ProjectRepository;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;

@Dependent
@Transactional
public class ProjectAccessService {

    private final ProjectRepository projects;

    @Inject
    public ProjectAccessService(ProjectRepository projects) {
        this.projects = projects;
    }

    public boolean canAccess(Long userId, Long projectId) {
        return userId != null && projects.findById(projectId)
                .map(project -> Objects.equals(project.getUserId(), userId))
                .orElse(false);
    }

    public void requireAccess(Long userId, Long projectId) {
        if (!canAccess(userId, projectId)) {
            throw new ForbiddenException("No tienes acceso a este proyecto");
        }
    }
}
