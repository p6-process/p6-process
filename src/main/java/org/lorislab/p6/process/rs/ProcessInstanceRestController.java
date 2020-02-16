package org.lorislab.p6.process.rs;

import org.lorislab.p6.process.dao.ProcessInstanceContentDAO;
import org.lorislab.p6.process.dao.ProcessInstanceDAO;
import org.lorislab.p6.process.dao.model.ProcessInstance;
import org.lorislab.p6.process.dao.model.ProcessInstanceContent;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.lorislab.p6.process.stream.DataUtil.deserialize;

@Path("instance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessInstanceRestController {

    @Inject
    ProcessInstanceDAO dao;

    @Inject
    ProcessInstanceContentDAO contentDAO;

    @GET
    public Response get() {
        List<ProcessInstance> tmp = dao.find(null, null);
        if (tmp == null || tmp.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{guid}")
    public Response get(@PathParam("guid") String guid) {
        ProcessInstance tmp = dao.findBy(guid);
        if (tmp == null) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{guid}/parameters")
    public Response getParameters(@PathParam("guid") String guid) {
        ProcessInstanceContent content = contentDAO.findBy(guid);
        if (content == null || content.getData() == null) {
            return Response.noContent().build();
        }
        return Response.ok(deserialize(content.getData())).build();
    }
}
