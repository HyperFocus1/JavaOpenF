package com.open_f.fu.chat.api;

import java.net.URI;
import java.util.List;

import com.open_f.fu.chat.api.request.MessageCreateRequest;
import com.open_f.fu.chat.api.response.MessageResponse;
import com.open_f.fu.chat.domain.model.Message;
import com.open_f.fu.chat.service.MessageService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

    private final MessageService service;

    @Inject
    public MessageResource(MessageService service) {
        this.service = service;
    }

    @POST
    public Response create(MessageCreateRequest req) {
        Message created = service.send(req.channelId(), req.userId(), req.content());
        return Response.created(URI.create("messages/" + created.getId()))
                .entity(MessageResponse.from(created)).build();
    }

    @GET
    @Path("/channel/{id}")
    public List<MessageResponse> findAll(@PathParam("id") Long channelId) {
        return service.allMessages(channelId).stream()
                .map(MessageResponse::from)
                .toList();
    }
}
