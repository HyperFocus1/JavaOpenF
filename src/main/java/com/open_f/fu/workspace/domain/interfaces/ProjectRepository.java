package com.open_f.fu.workspace.domain.interfaces;

import java.util.List;
import java.util.Optional;

import com.open_f.fu.workspace.domain.model.Project;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(Long id);

    // Más nuevos primero
    List<Project> findAll();

    List<Project> findByTitle(String text);
}
