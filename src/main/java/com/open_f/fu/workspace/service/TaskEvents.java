package com.open_f.fu.workspace.service;

import com.open_f.fu.workspace.domain.event.TaskCreated;
import com.open_f.fu.workspace.domain.event.TaskMoved;
import com.open_f.fu.workspace.domain.event.TaskUpdated;
import com.open_f.fu.workspace.domain.model.Task;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class TaskEvents {

    @Inject
    Event<TaskCreated> createdEvents;

    @Inject
    Event<TaskUpdated> updatedEvents;

    @Inject
    Event<TaskMoved> movedEvents;

    public void created(Task task) {
        createdEvents.fire(TaskCreated.from(task));
    }

    public void updated(Task task) {
        updatedEvents.fire(TaskUpdated.from(task));
    }

    public void moved(Task task) {
        movedEvents.fire(TaskMoved.from(task));
    }
}
