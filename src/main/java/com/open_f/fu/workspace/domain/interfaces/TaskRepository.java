package com.open_f.fu.workspace.domain.interfaces;

import java.util.List;
import java.util.Optional;

import com.open_f.fu.workspace.domain.model.Task;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(Long id);

    // positionY de las tarjetas de una columna, de menor a mayor
    List<Double> findPositionsByColumnId(Long columnId);

    List<Task> findTasksByProjectId(Long projectId);

    boolean existsByProjectId(Task task, Long projectId);

}
