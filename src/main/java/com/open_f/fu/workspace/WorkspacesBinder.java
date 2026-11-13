package com.open_f.fu.workspace;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.open_f.fu.workspace.domain.interfaces.ProjectColumnRepository;
import com.open_f.fu.workspace.domain.interfaces.ProjectRepository;
import com.open_f.fu.workspace.domain.interfaces.TaskRepository;
import com.open_f.fu.workspace.persistence.repository.JpaProjectColumnRepository;
import com.open_f.fu.workspace.persistence.repository.JpaProjectRepository;
import com.open_f.fu.workspace.persistence.repository.JpaTaskRepository;
import com.open_f.fu.workspace.service.ProjectService;
import com.open_f.fu.workspace.service.TaskService;

import jakarta.inject.Singleton;

public class WorkspacesBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(JpaProjectRepository.class).to(ProjectRepository.class).in(Singleton.class);
        bind(JpaProjectColumnRepository.class).to(ProjectColumnRepository.class).in(Singleton.class);
        bind(JpaTaskRepository.class).to(TaskRepository.class).in(Singleton.class);
        bindAsContract(ProjectService.class).in(Singleton.class);
        bindAsContract(TaskService.class).in(Singleton.class);
    }
}
