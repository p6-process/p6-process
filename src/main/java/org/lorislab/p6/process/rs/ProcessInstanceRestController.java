package org.lorislab.p6.process.rs;

import io.smallrye.mutiny.Uni;
import org.lorislab.p6.process.dao.model.ProcessInstance;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("instance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessInstanceRestController {

    @Inject
    io.vertx.mutiny.pgclient.PgPool client;

    @GET
    @Path("{id}")
    public Uni<Response> get(@PathParam("id") String id) {
        return ProcessInstance.findById(client, id)
                .onItem().apply(item -> item != null ? Response.ok(item) : Response.status(Response.Status.NOT_FOUND))
                .onItem().apply(Response.ResponseBuilder::build);
    }

}
