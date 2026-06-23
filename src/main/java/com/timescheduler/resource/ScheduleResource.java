package com.timescheduler.resource;

import com.timescheduler.dto.ScheduleDTO;
import com.timescheduler.service.ScheduleService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/users/{userId}/schedules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScheduleResource {

    @Inject
    ScheduleService scheduleService;

    @POST
    public Response createSchedule(@PathParam("userId") Long userId, ScheduleDTO scheduleDTO) {
        try {
            ScheduleDTO createdSchedule = scheduleService.createSchedule(userId, scheduleDTO);
            return Response.status(Response.Status.CREATED).entity(createdSchedule).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @GET
    public Response getSchedulesByUser(@PathParam("userId") Long userId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesByUser(userId);
        return Response.ok(schedules).build();
    }

    @GET
    @Path("/active")
    public Response getActiveSchedulesByUser(@PathParam("userId") Long userId) {
        List<ScheduleDTO> schedules = scheduleService.getActiveSchedulesByUser(userId);
        return Response.ok(schedules).build();
    }

    @GET
    @Path("/{id}")
    public Response getScheduleById(@PathParam("userId") Long userId, @PathParam("id") Long id) {
        return scheduleService.getScheduleById(id)
                .map(schedule -> Response.ok(schedule).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Schedule not found: " + id)).build());
    }

    @PUT
    @Path("/{id}")
    public Response updateSchedule(@PathParam("userId") Long userId, @PathParam("id") Long id, ScheduleDTO scheduleDTO) {
        try {
            ScheduleDTO updatedSchedule = scheduleService.updateSchedule(id, scheduleDTO);
            return Response.ok(updatedSchedule).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteSchedule(@PathParam("userId") Long userId, @PathParam("id") Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @POST
    @Path("/{id}/deactivate")
    public Response deactivateSchedule(@PathParam("userId") Long userId, @PathParam("id") Long id) {
        try {
            scheduleService.deactivateSchedule(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
