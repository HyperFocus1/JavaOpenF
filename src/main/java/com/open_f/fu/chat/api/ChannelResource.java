package com.open_f.fu.chat.api;

import java.net.URI;

import com.open_f.fu.chat.api.request.ChannelCreateRequest;
import com.open_f.fu.chat.api.request.ChannelUpdateRequest;
import com.open_f.fu.chat.api.response.ChannelResponse;
import com.open_f.fu.chat.domain.model.Channel;
import com.open_f.fu.chat.service.ChannelService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/channels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChannelResource {

    private final ChannelService service;

    @Inject
    public ChannelResource(ChannelService service) {
        this.service = service;
    }

    @POST
    public Response create(ChannelCreateRequest req) {
        Channel created = service.create(req.title());
        return Response.created(URI.create("channels/" + created.getId()))
                .entity(ChannelResponse.from(created)).build();
    }

    @GET
    @Path("/{id}")
    public ChannelResponse find(@PathParam("id") Long id) {
        return ChannelResponse.from(service.find(id));
    }

    @GET
    @Path("/title/{title}")
    public ChannelResponse findByTitle(@PathParam("title") String title) {
        return ChannelResponse.from(service.findByTitle(title));
    }

    @PUT
    @Path("/{id}")
    public ChannelResponse update(@PathParam("id") Long id, ChannelUpdateRequest req) {
        return ChannelResponse.from(service.update(id, req.title()));
    }
}
