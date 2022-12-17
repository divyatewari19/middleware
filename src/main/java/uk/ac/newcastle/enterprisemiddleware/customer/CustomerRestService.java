package uk.ac.newcastle.enterprisemiddleware.customer;

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
 * <p>This class produces a RESTful service exposing the functionality of {@link CustomerService}.</p>
 *
 * <p>The full path for accessing endpoints defined herein is: api/customer/*</p>
 *
 * @author Divya Tewari
 * @see CustomerService
 * @see javax.ws.rs.core.Response
 */

@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerRestService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    CustomerService service;

    /**
     * <p>Return all the Customer. They are sorted alphabetically by name.</p>
     *
     * <p>The url may optionally include query parameters specifying a Customer's name</p>
     *
     * <p>Examples: <pre>GET api/customer?name=Jason</pre></p>
     *
     * @return A Response containing a list of Customer
     */
    @GET
    @Operation(summary = "Fetch all Customer", description = "Returns a JSON array of all stored Customer objects.")
    public Response retrieveAllCustomer(@QueryParam("name") String name) {
        //Create an empty collection to contain the intersection of Customer to be returned
        List<Customer> customers;

        if(name == null) {
            customers = service.findAllOrderedByName();
        } else {
            customers = service.findAllByName(name);
        }
        return Response.ok(customers).build();
    }

    /**
     * <p>Search for and return a Customer identified by id.</p>
     *
     * @param id The long parameter value provided as a Customer's id
     * @return A Response containing a single Customer
     */
    @GET
    @Cache
    @Path("/{id:[0-9]+}")
    @Operation(
            summary = "Fetch a Customer by id",
            description = "Returns a JSON representation of the Customer object with the provided id."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Customer found"),
            @APIResponse(responseCode = "404", description = "Customer with id not found")
    })
    public Response retrieveCustomerById(
            @Parameter(description = "Id of Customer to be fetched", example = "1")
            @Schema(minimum = "0", required = true)
            @PathParam("id")
            long id) {

        Customer customer = service.findById(id);
        if (customer == null) {
            // Verify that the customer exists. Return 404, if not present.
            throw new RestServiceException("No Customer with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Customer = " + customer);

        return Response.ok(customer).build();
    }

    @GET
    @Cache
    @Path("/email/{email:.+[%40|@].+}")
    @Operation(
            summary = "Fetch a Customer by Email",
            description = "Returns a JSON representation of the Customer object with the provided email."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Customer found"),
            @APIResponse(responseCode = "404", description = "Customer with email not found")
    })
    public Response retrieveCustomersByEmail(
            @Parameter(description = "Email of Customer to be fetched", required = true)
            @PathParam("email")
            String email) {

        Customer customer = service.findByEmail(email);
        if (customer == null) {
            // Verify that the customer exists. Return 404, if not present.
            throw new RestServiceException("No Customer with the email " + email + " was found!", Response.Status.NOT_FOUND);
        }
        return Response.ok(customer).build();
    }

    /**
     * <p>Creates a new customer from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param customer The Customer object, constructed automatically from JSON input, to be <i>created</i> via
     * {@link CustomerService#create(Customer)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @Operation(description = "Add a new Customer to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Customer created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid Customer supplied in request body"),
            @APIResponse(responseCode = "409", description = "Customer supplied in request body conflicts with an existing Customer"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response createCustomer(
            @Parameter(description = "JSON representation of Customer object to be added to the database", required = true)
            Customer customer) {

        if (customer == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Clear the ID if accidentally set
            customer.setId(null);

            // Go add the new Customer.
            service.create(customer);

            // Create a "Resource Created" 201 Response and pass the customer back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(customer);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            log.info("createCustomer checking." + responseObj.values());
            ce.printStackTrace();
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueEmailException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
            e.printStackTrace();
            throw new RestServiceException(e);
        }

        log.info("createCustomer completed. Customer = " + customer);
        return builder.build();
    }

    /**
     * <p>Deletes a customer using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Customer to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a Customer from the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "The customer has been successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid Customer id supplied"),
            @APIResponse(responseCode = "404", description = "Customer with id not found"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response deleteCustomer(
            @Parameter(description = "Id of Customer to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id")
            long id) {

        Response.ResponseBuilder builder;

        Customer customer = service.findById(id);
        if (customer == null) {
            // Verify that the customer exists. Return 404, if not present.
            throw new RestServiceException("No Customer with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(customer);

            builder = Response.noContent();

        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        log.info("deleteCustomer completed. Customer = " + customer);
        return builder.build();
    }
}
