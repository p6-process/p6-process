package org.lorislab.p6.process.rs;

import org.lorislab.p6.process.dao.ProcessTokenDAO;
import org.lorislab.p6.process.dao.model.ProcessToken;

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
    ProcessTokenDAO dao;

    @GET
    public Response get() {
        List<ProcessToken> tmp = dao.find(null, null);
        if (tmp == null || tmp.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{guid}")
    public Response get(@PathParam("guid") String guid) {
        ProcessToken tmp = dao.findBy(guid);
        if (tmp == null) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }
}
