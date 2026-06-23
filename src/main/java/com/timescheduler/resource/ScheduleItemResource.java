package com.timescheduler.resource;

import com.timescheduler.dto.ScheduleItemDTO;
import com.timescheduler.service.ScheduleItemService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;

@Path("/schedules/{scheduleId}/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScheduleItemResource {

    @Inject
    ScheduleItemService itemService;

    @POST
    public Response createItem(@PathParam("scheduleId") Long scheduleId, ScheduleItemDTO itemDTO) {
        try {
            ScheduleItemDTO createdItem = itemService.createItem(scheduleId, itemDTO);
            return Response.status(Response.Status.CREATED).entity(createdItem).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @GET
    public Response getItemsBySchedule(@PathParam("scheduleId") Long scheduleId) {
        List<ScheduleItemDTO> items = itemService.getItemsBySchedule(scheduleId);
        return Response.ok(items).build();
    }

    @GET
    @Path("/upcoming")
    public Response getUpcomingItems(@PathParam("scheduleId") Long scheduleId) {
        List<ScheduleItemDTO> items = itemService.getUpcomingItems(scheduleId);
        return Response.ok(items).build();
    }

    @GET
    @Path("/completed")
    public Response getCompletedItems(@PathParam("scheduleId") Long scheduleId) {
        List<ScheduleItemDTO> items = itemService.getCompletedItems(scheduleId);
        return Response.ok(items).build();
    }

    @GET
    @Path("/by-priority/{priority}")
    public Response getItemsByPriority(@PathParam("scheduleId") Long scheduleId, @PathParam("priority") Integer priority) {
        List<ScheduleItemDTO> items = itemService.getItemsByPriority(scheduleId, priority);
        return Response.ok(items).build();
    }

    @GET
    @Path("/{id}")
    public Response getItemById(@PathParam("scheduleId") Long scheduleId, @PathParam("id") Long id) {
        return itemService.getItemById(id)
                .map(item -> Response.ok(item).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Schedule item not found: " + id)).build());
    }

    @PUT
    @Path("/{id}")
    public Response updateItem(@PathParam("scheduleId") Long scheduleId, @PathParam("id") Long id, ScheduleItemDTO itemDTO) {
        try {
            ScheduleItemDTO updatedItem = itemService.updateItem(id, itemDTO);
            return Response.ok(updatedItem).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteItem(@PathParam("scheduleId") Long scheduleId, @PathParam("id") Long id) {
        try {
            itemService.deleteItem(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @POST
    @Path("/{id}/complete")
    public Response markAsCompleted(@PathParam("scheduleId") Long scheduleId, @PathParam("id") Long id) {
        try {
            ScheduleItemDTO completedItem = itemService.markAsCompleted(id);
            return Response.ok(completedItem).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    @POST
    @Path("/{id}/incomplete")
    public Response markAsIncompleted(@PathParam("scheduleId") Long scheduleId, @PathParam("id") Long id) {
        try {
            ScheduleItemDTO incompleteItem = itemService.markAsIncompleted(id);
            return Response.ok(incompleteItem).build();
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
