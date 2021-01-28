package org.acme.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import org.acme.entity.Parking;
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


@Path("parking")
@Authenticated
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ParkingService {

    private static final Logger LOGGER = Logger.getLogger(ParkingService.class.getName());

    @GET
    public List<Parking> get() {
        return Parking.listAll();
    }

    @GET
    @Path("{id}")
    public Parking getSingle(@PathParam Long id) {
        Parking entity = Parking.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Parking with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    @Transactional
    public Response create(Parking parking) {
        if (parking.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        parking.persist();
        return Response.ok(parking).status(201).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Parking update(@PathParam Long id, Parking parking) {
        if (parking.box == null) {
            throw new WebApplicationException("Parking Name was not set on request.", 422);
        }

        Parking entity = Parking.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Parking with id of " + id + " does not exist.", 404);
        }

        entity.box = parking.box;

        return entity;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam Long id) {
        Parking entity = Parking.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Parking with id of " + id + " does not exist.", 404);
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
