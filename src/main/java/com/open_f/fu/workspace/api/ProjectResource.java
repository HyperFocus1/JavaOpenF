package com.open_f.fu.workspace.api;

import jakarta.enterprise.context.Dependent;

import java.net.URI;
import java.util.List;

import com.open_f.fu.shared.security.CurrentUser;
import com.open_f.fu.workspace.api.request.ProjectRequest;
import com.open_f.fu.workspace.api.request.TaskCreateRequest;
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
@Dependent
public class ProjectResource {

    @Inject
    ProjectService projectService;

    @Inject
    TaskService taskService;

    @POST
    public Response create(@Context SecurityContext securityContext, ProjectRequest req) {
        Project created = projectService.create(CurrentUser.id(securityContext), req.title(), req.description());
        return Response.created(URI.create("project/" + created.getId())).entity(ProjectResponse.from(created)).build();
    }

    @GET
    @Path("/{projectId}/tasks")
    public Response getTasks(@PathParam("projectId") Long projectId,
                             @Context SecurityContext securityContext) {
        List<Task> listTasks = taskService.getAll(projectId, CurrentUser.id(securityContext));
        return Response.ok(listTasks.stream().map(TaskResponse::from).toList()).build();
    }

    @POST
    @Path("/{projectId}/columns/{columnId}/tasks")
    public Response createTask(@PathParam("projectId") Long projectId,
                               @PathParam("columnId") Long columnId,
                               @QueryParam("positionOrder") @DefaultValue("0") int positionOrder,
                               @Context SecurityContext securityContext, TaskCreateRequest request) {
        Task task = taskService.create(request.toCommand(
                projectId, columnId, CurrentUser.id(securityContext), positionOrder));
        return Response.status(Response.Status.CREATED).entity(TaskResponse.from(task)).build();
    }

    @PUT()
    @Path("/{projectId}/tasks")
    public Response updateTask(@PathParam("projectId") Long projectId,
                               @Context SecurityContext securityContext, TaskUpdateRequest req) {
        Task taskUpdate = taskService.update(
                req.toCommand(req.taskId()), projectId, CurrentUser.id(securityContext));

        return Response.ok(TaskResponse.from(taskUpdate)).build();
    }

    @POST()
    @Path("/{projectId}/tasks/move")
    public Response moveTask(@PathParam("projectId") Long projectId,
                             @Context SecurityContext securityContext, TaskUpdateRequest req) {
        Task taskUpdate = taskService.updatePosition(
                req.taskUpdatePositionCommand(req.taskId(), req.positionOrder()),
                projectId, CurrentUser.id(securityContext));

        return Response.ok(TaskResponse.from(taskUpdate)).build();
    }


}
