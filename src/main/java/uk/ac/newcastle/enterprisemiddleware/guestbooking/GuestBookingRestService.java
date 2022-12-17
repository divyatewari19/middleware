package uk.ac.newcastle.enterprisemiddleware.guestbooking;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import uk.ac.newcastle.enterprisemiddleware.booking.*;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/guestbooking")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class GuestBookingRestService {
    @Inject
    private @Named("logger")
    Logger log;

    @Inject
    GuestBookingService service;

    @Inject
    UserTransaction userTransaction;

    /**
     * <p>Creates a guest booking in a single transaction using the customer and hotel service.
     * Failure at any point will rollback the transaction</p>
     *
     * @param guestBooking GuestBooking object, constructed automatically from JSON input, to be <i>created</i> via
     * {@link GuestBookingService#create(GuestBooking)}
     * @return A Response indicating the outcome of the create operation
     */
    @POST
    @Operation(description = "Add a new Booking to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Booking created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid Booking supplied in request body"),
            @APIResponse(responseCode = "409", description = "Booking supplied in request body conflicts with an existing Booking"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
//    @Transactional -> commented to use manual queries
    public Response createBooking(
            @Parameter(description = "JSON representation of Booking object to be added to the database", required = true)
            GuestBooking guestBooking) throws SystemException {

        if (guestBooking == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;
        Booking booking;

        try {
            // Go add the new Booking.
            booking = service.create(guestBooking);

            // Create a "Resource Created" 201 Response and pass the booking back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(booking);

        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueBookingException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("booking", "booking is already registered, please register on another date or hotel");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (CustomerNotFoundException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("customer", "Customer not found!");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
        } catch (HotelNotFoundException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("hotel", "Hotel not found!");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            e.printStackTrace();
            userTransaction.rollback();
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("createBooking completed. Booking = " + guestBooking );
        return builder.build();
    }

}
