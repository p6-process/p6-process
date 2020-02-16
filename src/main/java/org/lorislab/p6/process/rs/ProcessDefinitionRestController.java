package org.lorislab.p6.process.rs;

import org.lorislab.p6.process.dao.ProcessDefinitionDAO;
import org.lorislab.p6.process.dao.model.ProcessDefinition;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("definition")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessDefinitionRestController {

    @Inject
    ProcessDefinitionDAO dao;

    @GET
    public Response get() {
        List<ProcessDefinition> tmp = dao.find(null, null);
        if (tmp == null || tmp.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{guid}")
    public Response get(@PathParam("guid") String guid) {
        ProcessDefinition tmp = dao.findBy(guid);
        if (tmp == null) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }
}
