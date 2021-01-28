
package org.acme.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import org.acme.entity.Box;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;


@Path("box")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class BoxService {

    private static final Logger LOGGER = Logger.getLogger(BoxService.class.getName());

    @GET
    public List<Box> get() {
        return Box.listAll();
    }

    @GET
    @Path("{id}")
    public Box getSingle(@PathParam Long id) {
        Box entity = Box.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Box with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    @Transactional
    public Response create(Box box) {
        if (box.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        box.persist();
        return Response.ok(box).status(201).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Box update(@PathParam Long id, Box box) {
        if (box.coefficient == 0) {
            throw new WebApplicationException("Box Name was not set on request.", 422);
        }

        Box entity = Box.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Box with id of " + id + " does not exist.", 404);
        }

        entity.coefficient = box.coefficient;

        return entity;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam Long id) {
        Box entity = Box.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Box with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {
            LOGGER.error("Failed to handle request", exception);

            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }

            ObjectNode exceptionJson = objectMapper.createObjectNode();
            exceptionJson.put("exceptionType", exception.getClass().getName());
            exceptionJson.put("code", code);

            if (exception.getMessage() != null) {
                exceptionJson.put("error", exception.getMessage());
            }

            return Response.status(code)
                    .entity(exceptionJson)
                    .build();
        }

    }
}
