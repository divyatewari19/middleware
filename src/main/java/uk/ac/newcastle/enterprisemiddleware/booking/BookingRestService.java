package uk.ac.newcastle.enterprisemiddleware.booking;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.reactive.Cache;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BookingRestService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    BookingService service;

    /**
     * <p>Creates a new booking from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param booking The Booking object, constructed automatically from JSON input, to be <i>created</i> via
     * {@link BookingService#create(Booking)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @Operation(description = "Add a new Booking to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Booking created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid Booking supplied in request body"),
            @APIResponse(responseCode = "409", description = "Booking supplied in request body conflicts with an existing Booking"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response createBooking(
            @Parameter(description = "JSON representation of Booking object to be added to the database", required = true)
            Booking booking) {

        if (booking == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Clear the ID if accidentally set
            booking.setId(null);

            // Go add the new Booking.
            Booking bookingResponse = service.create(booking);

            // Create a "Resource Created" 201 Response and pass the booking back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(bookingResponse);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                log.severe(violation.getPropertyPath().toString() + " " + violation.getMessage());
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }

            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueBookingException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            log.severe("booking is already registered, please register on another date or hotel");
            responseObj.put("booking", "booking is already registered, please register on another date or hotel");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (CustomerNotFoundException e) {
            // Handle the unique constraint violation
            log.severe("Customer not found!");
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("customer", "Customer not found!");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
        } catch (HotelNotFoundException e) {
            // Handle the unique constraint violation
            log.severe("Hotel not found!");
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("hotel", "Hotel not found!");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("createBooking completed. Booking = { " + booking + " }");
        return builder.build();
    }

    /**
     * <p>Return all the Booking. They are sorted by Date.</p>
     *
     * <p>Examples: <pre>GET api/bookings/<pre></p>
     *
     * @return A Response containing a list of Bookings
     */
    @GET
    @Operation(summary = "Fetch all Booking", description = "Returns a JSON array of all stored Booking objects.")
    public Response retrieveAllBooking() {
        //Create an empty collection to contain the intersection of Booking to be returned
        List<Booking> bookings;
        bookings = service.findAllOrderedByDate();

        return Response.ok(bookings).build();
    }

    /**
     * <p>Search for and return a Booking identified by id.</p>
     *
     * @param id The long parameter value provided as a Booking's id
     * @return A Response containing a single Booking
     */
    @GET
    @Cache
    @Path("/{id:[0-9]+}")
    @Operation(
            summary = "Fetch a Booking by id",
            description = "Returns a JSON representation of the Booking object with the provided id."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Booking found"),
            @APIResponse(responseCode = "404", description = "Booking with id not found")
    })
    public Response retrieveBookingById(
            @Parameter(description = "Id of Booking to be fetched")
            @Schema(minimum = "0", required = true)
            @PathParam("id")
            long id) {

        Booking booking = service.findById(id);
        if (booking == null) {
            // Verify that the booking exists. Return 404, if not present.
            throw new RestServiceException("No Booking with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Booking = " + booking);

        return Response.ok(booking).build();
    }

    /**
     * <p>Retrieve all booking made by a particular customer based on id.</p>
     *
     * @param customerId The long parameter value provided as a Booking's id
     * @return A Response containing a single Booking
     */
    @GET
    @Cache
    @Path("customer/{customerId: [0-9]+}")
    @Operation(
            summary = "Fetch bookings by customer id",
            description = "Returns a JSON representation of the Bookings made by a particular customer."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Booking found"),
            @APIResponse(responseCode = "404", description = "Booking with id not found")
    })
    public Response retrieveBookingByCustomerId(
            @Parameter(description = "Id of Booking to be fetched")
            @Schema(minimum = "0", required = true)
            @PathParam("customerId")
            long customerId) {

        List<Booking> bookings = service.findByCustomerId(customerId);
        if (bookings == null || bookings.size() == 0) {
            // Verify that the booking exists. Return 404, if not present.
            throw new RestServiceException("No Booking with the customerId " + customerId + " were found!", Response.Status.NOT_FOUND);
        }
        log.info("retrieveBookingByCustomerId " + customerId + ": found Booking = " + bookings);

        return Response.ok(bookings).build();
    }

    /**
     * <p>Deletes a booking using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Booking to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a Booking from the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "The booking has been successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid Booking id supplied"),
            @APIResponse(responseCode = "404", description = "Booking with id not found"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response deleteBooking(
            @Parameter(description = "Id of Booking to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id")
            long id) {

        Response.ResponseBuilder builder;
        Booking deletedBooking;
        try {
            deletedBooking = service.delete(id);

            builder = Response.noContent();

        } catch (RestServiceException e) {
            e.printStackTrace();
            // Handle generic exceptions
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        log.info("deleteBooking completed. Booking = " + deletedBooking);
        return builder.build();
    }
}
