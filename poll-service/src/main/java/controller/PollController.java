package controller;

import dto.PollCreateDTO;
import dto.PollResponseDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.PollService;

import java.net.URI;
import java.util.List;

@Path("/polls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PollController {

    @Inject
    PollService pollService;

    @POST
    public Response createPoll(@Valid PollCreateDTO dto) {
        PollResponseDTO created = pollService.create(dto);
        return Response
                .created(URI.create("/polls/" + created.id()))
                .entity(created)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getPoll(@PathParam("id") Long id) {
        PollResponseDTO poll = pollService.findById(id);
        return Response.ok(poll).build();
    }

    @GET
    public List<PollResponseDTO> listPolls() {
        return pollService.listAll();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePoll(@PathParam("id") Long id) {
        pollService.delete(id);
        return Response.noContent().build();
    }
}
