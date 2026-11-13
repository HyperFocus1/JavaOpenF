package com.open_f.fu.workspace.domain.interfaces;

import java.util.List;
import java.util.Optional;

import com.open_f.fu.workspace.domain.model.ProjectColumn;

public interface ProjectColumnRepository {

    ProjectColumn save(ProjectColumn column);

    // Ordenadas por posición
    List<ProjectColumn> findAllByProjectId(Long projectId);

    Optional<ProjectColumn> findByIdAndProjectId(Long columnId, Long projectId);
}
