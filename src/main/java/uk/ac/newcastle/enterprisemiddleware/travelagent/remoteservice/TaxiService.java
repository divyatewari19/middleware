package uk.ac.newcastle.enterprisemiddleware.travelagent.remoteservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.travelagent.remotemodel.FlightModel;
import uk.ac.newcastle.enterprisemiddleware.travelagent.remotemodel.TaxiBookingModel;
import uk.ac.newcastle.enterprisemiddleware.travelagent.remotemodel.TaxiModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * <p>Clientside representation of an Taxi object pulled from an external RESTFul API.
 * Serves as a proxy for for RestClient</p>
 *
 * <p>This is the mirror opposite of a server side JAX-RS service</p>
 *
 * @author Divya Tewari
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "taxi-api")
public interface TaxiService {

    @GET
    @Path("/taxis")
    @Produces(MediaType.APPLICATION_JSON)
    List<FlightModel> getTaxis();

    @GET
    @Path("/taxis/{id:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    TaxiModel getTaxiById(@PathParam("id") Long id);

    @POST
    @Path("/customers")
    @Consumes(MediaType.APPLICATION_JSON)
    Customer createCustomer(Customer customer);

    @GET
    @Path("/customers/email/{email: {email:.+[%40|@].+}}")
    @Consumes(MediaType.APPLICATION_JSON)
    Customer getCustomerByEmail(@PathParam("email") String email);

    @POST
    @Path("/bookings")
    @Consumes(MediaType.APPLICATION_JSON)
    TaxiBookingModel createBooking(TaxiBookingModel request);

    @DELETE
    @Path("/bookings/{id:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    void deleteTaxiBooking(@PathParam("id") Long id);
}
