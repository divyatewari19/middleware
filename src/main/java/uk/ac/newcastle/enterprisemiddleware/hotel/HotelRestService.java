package uk.ac.newcastle.enterprisemiddleware.hotel;

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

/**
 * <p>This class produces a RESTful service exposing the functionality of {@link HotelService}.</p>
 *
 * <p>The full path for accessing endpoints defined herein is: api/hotel/*</p>
 *
 * @author Divya Tewari
 * @see HotelService
 * @see javax.ws.rs.core.Response
 */

@Path("/hotels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HotelRestService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    HotelService service;

    /**
     * <p>Return all the Hotel. They are sorted alphabetically by name.</p>
     *
     * <p>The url may optionally include query parameters specifying a Hotel's name</p>
     *
     * <p>Examples: <pre>GET api/hotel?name=TheInn</pre></p>
     *
     * @return A Response containing a list of Hotels
     */
    @GET
    @Operation(summary = "Fetch all Hotels", description = "Returns a JSON array of all stored Hotels objects.")
    public Response retrieveAllHotels(@QueryParam("name") String name) {
        //Create an empty collection to contain the intersection of Hotels to be returned
        List<Hotel> hotels;

        if(name == null) {
            hotels = service.findAllOrderedByName();
        } else {
            hotels = service.findAllByName(name);
        }
        return Response.ok(hotels).build();
    }

    /**
     * <p>Search for and return a Hotel identified by id.</p>
     *
     * @param id The long parameter value provided as a Hotel's id
     * @return A Response containing a single Hotel
     */
    @GET
    @Cache
    @Path("/{id:[0-9]+}")
    @Operation(
            summary = "Fetch a Hotel by id",
            description = "Returns a JSON representation of the Hotel object with the provided id."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Hotel found"),
            @APIResponse(responseCode = "404", description = "Hotel with id not found")
    })
    public Response retrieveHotelById(
            @Parameter(description = "Id of Hotel to be fetched")
            @Schema(minimum = "0", required = true)
            @PathParam("id")
            long id) {

        Hotel hotel = service.findById(id);
        if (hotel == null) {
            // Verify that the hotel exists. Return 404, if not present.
            throw new RestServiceException("No Hotel with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Hotel = " + hotel);

        return Response.ok(hotel).build();
    }

    /**
     * <p>Creates a new hotel from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param hotel The Hotel object, constructed automatically from JSON input, to be <i>created</i> via
     * {@link HotelService#create(Hotel)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @Operation(description = "Add a new Hotel to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Hotel created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid Hotel supplied in request body"),
            @APIResponse(responseCode = "409", description = "Hotel supplied in request body conflicts with an existing Hotel"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response createHotel(
            @Parameter(description = "JSON representation of Hotel object to be added to the database", required = true)
            Hotel hotel) {

        if (hotel == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Clear the ID if accidentally set
            hotel.setId(null);

            // Go add the new Hotel.
            service.create(hotel);

            // Create a "Resource Created" 201 Response and pass the hotel back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(hotel);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniquePhoneNumberException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("phoneNumber", "That phone number is already used, please use a unique phone number");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("createHotel completed. Hotel = " + hotel);
        return builder.build();
    }

    /**
     * <p>Deletes a hotel using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Hotel to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a Hotel from the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "The hotel has been successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid Hotel id supplied"),
            @APIResponse(responseCode = "404", description = "Hotel with id not found"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response deleteHotel(
            @Parameter(description = "Id of Hotel to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id")
            long id) {

        Response.ResponseBuilder builder;

        Hotel hotel = service.findById(id);
        if (hotel == null) {
            // Verify that the hotel exists. Return 404, if not present.
            throw new RestServiceException("No Hotel with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(hotel);

            builder = Response.noContent();

        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        log.info("deleteHotel completed. Hotel = " + hotel);
        return builder.build();
    }
}
