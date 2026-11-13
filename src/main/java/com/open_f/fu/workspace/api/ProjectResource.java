package com.open_f.fu.workspace.api;

import java.net.URI;

import com.open_f.fu.shared.security.CurrentUser;
import com.open_f.fu.workspace.api.request.ProjectRequest;
import com.open_f.fu.workspace.api.request.TaskUpdateRequest;
import com.open_f.fu.workspace.api.response.ProjectResponse;
import com.open_f.fu.workspace.api.response.TaskResponse;
import com.open_f.fu.workspace.domain.model.Project;
import com.open_f.fu.workspace.domain.model.Task;
import com.open_f.fu.workspace.service.ProjectService;

import com.open_f.fu.workspace.service.TaskService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/project")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectResource {

    private final ProjectService projectService;
    private final TaskService taskService;

    @Inject
    public ProjectResource(ProjectService pService, TaskService tService) {
        this.projectService = pService;
        this.taskService = tService;
    }

    @POST
    public Response create(@Context SecurityContext securityContext, ProjectRequest req) {
        Project created = projectService.create(CurrentUser.id(securityContext), req.title(), req.description());
        return Response.created(URI.create("project/" + created.getId())).entity(ProjectResponse.from(created)).build();
    }

    @PUT()
    @Path("/{projectId}/task")
    public Response updateTask(@PathParam("projectId") Long projectId, @Context SecurityContext securityContext, TaskUpdateRequest req) {

        if (taskService.taskExistsOnProject(req.taskId(), projectId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Task taskUpdate = taskService.update(req.toCommand(req.taskId()));

        return Response.ok(TaskResponse.from(taskUpdate)).build();
    }

    @POST()
    @Path("/{projectId}/task/move")
    public Response moveTask(@PathParam("projectId") Long projectId, @Context SecurityContext securityContext, TaskUpdateRequest req) {

        if (taskService.taskExistsOnProject(req.taskId(), projectId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Task taskUpdate = taskService.updatePosition(req.taskUpdatePositionCommand(req.taskId(), req.positionOrder()));

        return Response.ok(TaskResponse.from(taskUpdate)).build();
    }


}
