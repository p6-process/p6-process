package org.lorislab.p6.process.rs;

import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.model.ProcessInstance;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("instance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessInstanceRestController {

    @Inject
    ProcessInstanceDAO dao;

    @GET
    public Response get() {
        List<ProcessInstance> tmp = dao.findAll();
        if (tmp == null || tmp.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{guid}")
    public Response get(@PathParam("guid") String guid) {
        ProcessInstance tmp = dao.findByGuid(guid);
        if (tmp == null) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{guid}/parameters")
    public Response getParameters(@PathParam("guid") String guid) {
        ProcessInstance tmp = dao.findByGuid(guid);
        if (tmp == null || tmp.data == null) {
            return Response.noContent().build();
        }
        return Response.ok(tmp.data).build();
    }
}
