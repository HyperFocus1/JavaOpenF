package com.open_f.fu.workspace.api;

import java.net.URI;
import java.util.List;
import com.open_f.fu.shared.messaging.RealtimeServices;
import com.open_f.fu.shared.security.CurrentUser;
import com.open_f.fu.workspace.api.request.TaskCreateRequest;
import com.open_f.fu.workspace.api.response.TaskResponse;
import com.open_f.fu.workspace.service.ProjectAccessService.ColumnView;
import com.open_f.fu.workspace.service.TaskService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/realtime/projects/{projectId}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class ProjectRealtimeResource {
    private final RealtimeServices realtime;
    private final TaskService tasks;

    @Inject public ProjectRealtimeResource(RealtimeServices realtime, TaskService tasks) {
        this.realtime = realtime;
        this.tasks = tasks;
    }

    @GET public Snapshot snapshot(@PathParam("projectId") Long projectId,
            @Context SecurityContext security) {
        Long userId = CurrentUser.id(security);
        var columns = realtime.access().columns(userId, projectId);
        return new Snapshot(columns, tasks.getAll(projectId).stream().map(TaskResponse::from).toList());
    }

    @POST @Path("/columns/{columnId}/tasks")
    public Response create(@PathParam("projectId") Long projectId,
            @PathParam("columnId") Long columnId,
            @QueryParam("positionOrder") @DefaultValue("0") int positionOrder,
            @Context SecurityContext security, TaskCreateRequest request) {
        if (request == null) throw new BadRequestException("Falta la tarea");
        Long userId = CurrentUser.id(security);
        realtime.access().requireOwner(userId, projectId);
        var created = tasks.create(request.toCommand(projectId, columnId, userId, positionOrder));
        return Response.created(URI.create("project/" + projectId + "/tasks/" + created.getId()))
                .entity(TaskResponse.from(created)).build();
    }

    public record Snapshot(List<ColumnView> columns, List<TaskResponse> tasks) {}
}
