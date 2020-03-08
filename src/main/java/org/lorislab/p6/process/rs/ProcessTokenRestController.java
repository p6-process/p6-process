package org.lorislab.p6.process.rs;

import org.lorislab.p6.process.mem.service.ProcessTokenService;
import org.lorislab.p6.process.stream.model.ProcessTokenStream;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessTokenRestController {

    @Inject
    ProcessTokenService dao;

    @GET
    public Response get() {
        List<ProcessTokenStream> tmp = dao.findAll();
        if (tmp == null || tmp.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{guid}")
    public Response get(@PathParam("guid") String guid) {
        ProcessTokenStream tmp = dao.findByGuid(guid);
        if (tmp == null) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }
}
