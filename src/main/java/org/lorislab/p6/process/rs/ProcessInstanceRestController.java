package org.lorislab.p6.process.rs;

import org.lorislab.p6.process.dao.ProcessInstanceDAO;
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
    ProcessInstanceDAO dao;

    @GET
    @Path("{guid}")
    public Response get(@PathParam("guid") String guid) {
        ProcessInstance tmp = dao.findByGuid(guid);
        if (tmp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{guid}/parameters")
    public Response getParameters(@PathParam("guid") String guid) {
        ProcessInstance tmp = dao.findByGuid(guid);
        if (tmp == null || tmp.getData() == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(tmp.getData()).build();
    }
}
