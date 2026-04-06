package controller;

import dto.PollCreateDTO;
import dto.PollResponseDTO;
import dto.PollUpdatedEventDTO;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.PollService;

@Path("/poll")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class PollController {

    @Inject
    PollService pollService;

    @POST
    public Response createPoll(@Valid PollCreateDTO dto) {
        PollResponseDTO created = pollService.create(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePoll(@PathParam("id") Long id) {
        pollService.delete(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}")
    public Response putPoll(@PathParam("id") Long id, @Valid PollUpdatedEventDTO dto) {
        pollService.update(id, dto);
        return Response.noContent().build();
    }
}
