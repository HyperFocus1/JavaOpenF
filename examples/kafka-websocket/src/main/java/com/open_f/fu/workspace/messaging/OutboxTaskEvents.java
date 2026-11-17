package com.open_f.fu.workspace.messaging;

import com.open_f.fu.shared.messaging.RealtimeServices;
import com.open_f.fu.workspace.domain.interfaces.TaskEvents;
import com.open_f.fu.workspace.domain.model.Task;
import jakarta.inject.Inject;

public final class OutboxTaskEvents implements TaskEvents {
    private final RealtimeServices realtime;

    @Inject public OutboxTaskEvents(RealtimeServices realtime) { this.realtime = realtime; }

    @Override public void created(Task task) {
        realtime.outbox().append(TaskCreatedEvent.from(task));
    }
}
