package org.lorislab.p6.process.rs;

import org.lorislab.p6.process.mem.service.PersistenceInstanceService;
import org.lorislab.p6.process.stream.model.ProcessInstance;

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
    PersistenceInstanceService persistenceInstanceCache;

    @GET
    public Response get() {
        List<ProcessInstance> tmp = persistenceInstanceCache.findAll();
        if (tmp == null || tmp.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{guid}")
    public Response get(@PathParam("guid") String guid) {
        ProcessInstance tmp = persistenceInstanceCache.findByGuid(guid);
        if (tmp == null) {
            return Response.noContent().build();
        }
        return Response.ok(tmp).build();
    }

}
