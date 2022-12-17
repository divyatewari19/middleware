package uk.ac.newcastle.enterprisemiddleware.travelagent;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;
import uk.ac.newcastle.enterprisemiddleware.travelagent.clientmodel.FlightBooking;
import uk.ac.newcastle.enterprisemiddleware.travelagent.clientmodel.HotelBooking;
import uk.ac.newcastle.enterprisemiddleware.travelagent.clientmodel.TaxiBooking;
import uk.ac.newcastle.enterprisemiddleware.travelagent.remotemodel.*;
import uk.ac.newcastle.enterprisemiddleware.travelagent.remoteservice.FlightService;
import uk.ac.newcastle.enterprisemiddleware.travelagent.remoteservice.TaxiService;
import uk.ac.newcastle.enterprisemiddleware.util.HttpError;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p> Service Class for validation and business logic. </p>
 * @author Divya Tewari
 * */
@Dependent
public class TravelAgentBookingService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    TravelAgentBookingRepository crud;

    @Inject
    TravelAgentBookingValidator validator;

    @Inject
    private CustomerService customerService;

    @Inject
    private BookingService hotelBookingService;

    @Inject
    private HotelService hotelService;

    @Inject
    UserTransaction userTransaction;

    @RestClient
    FlightService flightService;

    @RestClient
    TaxiService taxiService;

    // Travel agent details
    private static final String TA_FIRSTNAME = "Divya";
    private static final String TA_LASTNAME = "Tewari";
    private static final String TA_EMAIL = "div@example.com";
    private static final String TA_PHONE = "08866754320";

    /**
     * <p>Returns a List of all persisted {@link Booking} objects, sorted alphabetically by name.<p/>
     *
     * @return List of Booking objects
     */
    List<TravelAgentBooking> findAllOrderedByDate() {
        return crud.findAllOrderedByDate();
    }

    /**
     * <p>Returns a single TravelAgentBooking object, specified by a Long id.<p/>
     *
     * @param id The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    TravelAgentBooking findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Writes the provided TravelAgentBooking object to the application database.<p/>
     *
     * <p>Validates the data in the provided Customer object using a {@link TravelAgentBookingValidator} object.<p/>
     *
     * @param bookingRequest The TravelAgentBookingRequest object that needs to be parsed and written to the database using a {@link TravelAgentBookingRepository} object
     * @return The TravelAgentBooking object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    TravelAgentBookingRequest create(TravelAgentBookingRequest bookingRequest) throws Exception {
        log.info("TravelAgentBookingService.create() - Creating for Customer ID: " + bookingRequest.getCustomerId());
        Long flightId = bookingRequest.getFlightBooking().getFlightId();
        Long taxiId = bookingRequest.getTaxiBooking().getTaxiId();
        Date flightBookingDate = bookingRequest.getFlightBooking().getBookingDate();
        Date hotelBookingDate = bookingRequest.getHotelBooking().getBookingDate();
        Date taxiBookingDate = bookingRequest.getTaxiBooking().getBookingDate();

        // response object with all the persisted information
        TravelAgentBookingRequest taBookingResponse = new TravelAgentBookingRequest();

        // Fetch customer, hotel, taxi and flight details from ID
        Customer customerFetchedById = customerService.findById(bookingRequest.getCustomerId());
        Hotel hotelFetchedById = hotelService.findById(bookingRequest.getHotelBooking().getHotelId());
        FlightModel flightFetchedById = null;
        TaxiModel taxiFetchedById = null;

        // Fetch flight & taxi
        try {
            flightFetchedById = findFlightById(flightId);
            taxiFetchedById = findTaxiById(taxiId);
        } catch (Exception e) {
            throw e;
        }

        // Check to make sure the data fits with the parameters in the Booking model and passes validation.
        validator.validateBooking(bookingRequest, customerFetchedById, hotelFetchedById, flightFetchedById);

        // 1. Register a travel agent as a customer in external services FLIGHT and TAXI if does not exit already
        // Travel agent can be uniquely identified through email, thus no requirement to save persisted details
        Customer persistedTravelAgentFlight;
        Customer persistedTravelAgentTaxi;
        try {
            persistedTravelAgentFlight = flightService.getCustomerByEmail(TA_EMAIL);
        } catch (ClientWebApplicationException e) {
            if (e.getResponse().getStatus() ==  HttpError.NOT_FOUND) {
                // customer with email does not exist, create one
                persistedTravelAgentFlight = flightService.createCustomer(createTravelAgentObject());
                log.info("TravelAgent successfully created for  Flight");
            } else {
                persistedTravelAgentFlight = null;
                throw HttpError.throwServiceException(HttpError.FLIGHTBOOKING, "Travel Agent creation on flightService failed", null, e);
            }
        }
        log.info("TravelAgent Flight details: " + persistedTravelAgentFlight);

        try {
            persistedTravelAgentTaxi = taxiService.getCustomerByEmail(TA_EMAIL);
        } catch (ClientWebApplicationException e) {
            if (e.getResponse().getStatus() ==  HttpError.NOT_FOUND) {
                // customer with email does not exist, create one
                persistedTravelAgentTaxi = taxiService.createCustomer(createTravelAgentObject());
                log.info("TravelAgent successfully created for Taxi");
            } else {
                persistedTravelAgentTaxi = null;
                throw HttpError.throwServiceException(HttpError.TAXIBOOKING, "Travel Agent creation on taxiService failed", null, e);
            }
        }

        log.info("TravelAgent Taxi Details: " + persistedTravelAgentTaxi);

        FlightBookingModel persistedFlightBooking = null;
        TaxiBookingModel persistedTaxiBooking = null;
        Booking persistedHotelBooking = null;

        userTransaction.begin();

        // 2. Make booking in Flight Database
        log.info("Flight booking started...");

        try {
            FlightBookingModel flightBookingModel = createFlightBookingObject(persistedTravelAgentFlight, flightFetchedById, flightBookingDate);
            persistedFlightBooking = flightService.createBooking(flightBookingModel);
        } catch (ClientWebApplicationException e) {
            // revert previous booking made
            userTransaction.rollback();
            throw HttpError.throwServiceException(HttpError.FLIGHTBOOKING, null, null, e);
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }
        log.info("Flight booking completed: " + persistedFlightBooking);

        // 3. Make booking in Taxi Database
        log.info("Taxi booking started...");
        try {
            TaxiBookingModel taxiBookingModel = createTaxiBookingObject(persistedTravelAgentTaxi, taxiFetchedById, taxiBookingDate);
            persistedTaxiBooking = taxiService.createBooking(taxiBookingModel);
        } catch (ClientWebApplicationException e) {
            // revert previous booking made
            userTransaction.rollback();
            flightService.deleteFlightBooking(persistedFlightBooking.getId());
            throw HttpError.throwServiceException(HttpError.TAXIBOOKING, null, null, e);
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }
        log.info("Taxi booking completed: " + persistedTaxiBooking);

        // 4. Make internal Hotel Booking
        log.info("Hotel booking started...");
        try {
            Booking hotelBooking = createHotelBookingObject(customerFetchedById, hotelFetchedById, hotelBookingDate);
            persistedHotelBooking = hotelBookingService.create(hotelBooking);
        } catch (ClientWebApplicationException e) {
            // revert previous booking made
            userTransaction.rollback();
            flightService.deleteFlightBooking(persistedFlightBooking.getId());
            taxiService.deleteTaxiBooking(persistedTaxiBooking.getId());
            throw HttpError.throwServiceException(HttpError.FLIGHTBOOKING, null, null, e);
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }
        log.info("Hotel booking completed:: " + persistedHotelBooking);

        // 5. Store persisted information in TravelAgentBooking object
        // Write the TravelAgentBooking booking object to the internal TravelAgent database.
        TravelAgentBooking dbBooking = createTABookingObject(customerFetchedById, persistedFlightBooking.getId(), persistedHotelBooking.getId(), persistedTaxiBooking.getId());
        TravelAgentBooking persistedTABooking = crud.create(dbBooking);

        // 6. Create response to send back to Client
        taBookingResponse = createTABookingResponse(persistedTABooking, persistedFlightBooking, persistedHotelBooking, persistedTaxiBooking);

        // 7. commit the transaction
        userTransaction.commit();

        log.info("Travel Agent Booking completed!");

        // return persisted booking request
        return taBookingResponse;
    }

    /**
     * <p>Returns list of all bookings, specified by a Long customer id.<p/>
     *
     * @param customerId The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    public List<TravelAgentBooking> findByCustomerId(Long customerId) {
        return crud.findByCustomerId(customerId);
    }

    private FlightModel findFlightById(Long id) throws RestServiceException {
        FlightModel flight;
        try {
            flight = flightService.getFlightById(id);
        } catch (ClientWebApplicationException e) {
            if (e.getResponse().getStatus() ==  HttpError.NOT_FOUND) {
                throw HttpError.throwServiceException(HttpError.FLIGHTBOOKING, "id", "Flight with id " + id + " does not exist", e);
            } else {
                throw HttpError.throwServiceException(HttpError.FLIGHTBOOKING, null, null, e);
            }
        }
        return flight;
    }

    private TaxiModel findTaxiById(Long id) throws RestServiceException {
        TaxiModel taxi = null;
        try {
            taxi = taxiService.getTaxiById(id);
        } catch (ClientWebApplicationException e) {
            if (e.getResponse().getStatus() ==  HttpError.NOT_FOUND) {
                HttpError.throwServiceException(HttpError.TAXIBOOKING, "id", "Taxi with id " + id + " does not exist", e);
            } else {
                 HttpError.throwServiceException(HttpError.TAXIBOOKING, null, null, e);
            }
        }
        return taxi;
    }

    /**
     * Creates a hardcoded travelAgent to store in external services
     * to-do: Can be made into a singleton object returning the same instance
     * */
    private Customer createTravelAgentObject() {
        Customer travelAgent = new Customer();
        travelAgent.setFirstName(TA_FIRSTNAME);
        travelAgent.setLastName(TA_LASTNAME);
        travelAgent.setPhoneNumber(TA_PHONE);
        travelAgent.setEmail(TA_EMAIL);
        return travelAgent;
    }

    /**
     * Creates a Hotel booking object for persisting in to internal hotel database
     * Customer for Hotel external service is the actual customer making the request
     * @see Booking
     * */
    private Booking createHotelBookingObject(Customer customer, Hotel hotel, Date bookingDate) {
        Booking booking = new Booking();
        booking.setId(null);
        booking.setBookingDate(bookingDate);
        booking.setCustomer(customer);
        booking.setHotel(hotel);

        return booking;
    }

    /**
     * Creates a Hotel booking object for persisting in to internal hotel database
     * Customer for Hotel external service is the actual customer making the request
     * @see Booking
     * */
    private TaxiBookingModel createTaxiBookingObject(Customer customer, TaxiModel taxi, Date bookingDate) {
        TaxiBookingModel booking = new TaxiBookingModel();
        booking.setId(null);
        booking.setDateOfBooking(bookingDate);
        booking.setCustomer(customer);
        booking.setTaxi(taxi);

        return booking;
    }

    /**
     * Creates a Flight booking object for persisting in to flight database
     * Customer for Flight external service is going to be the Travel Agent
     * @see FlightBookingModel
     * */
    private FlightBookingModel createFlightBookingObject(Customer travelAgent, FlightModel flight, Date bookingDate) throws ClientWebApplicationException {
        FlightBookingModel booking = new FlightBookingModel();
        booking.setId(null);
        // travel agent is the customer for external services
        booking.setCustomer(travelAgent);
        booking.setFlight(flight);
        booking.setDate(bookingDate);

        return booking;
    }

    /**
     * Creates a Travel Agent booking object for persisting in the database
     * @see TravelAgentBooking
     * */
    private TravelAgentBooking createTABookingObject(Customer customer, Long flightId, Long hotelId, Long taxiId) {
        TravelAgentBooking dbBooking = new TravelAgentBooking();
        dbBooking.setCustomer(customer);
        dbBooking.setHotelBookingId(hotelId);
        dbBooking.setFlightBookingId(flightId);
        dbBooking.setTaxiBookingId(taxiId);

        return dbBooking;
    }

    /**
     * Creates a response object for sending it back to the client in the format of
     * @see TravelAgentBookingRequest
     * */
    private TravelAgentBookingRequest createTABookingResponse(TravelAgentBooking taBooking, FlightBookingModel flightBooking, Booking hotelBooking, TaxiBookingModel taxiBooking) {
        TravelAgentBookingRequest bookingResponse = new TravelAgentBookingRequest();
        bookingResponse.setId(taBooking.getId());
        bookingResponse.setCustomerId(taBooking.getCustomer().getId());
        // create Flight Response Model
        FlightBooking flightModelResponse = new FlightBooking();
        flightModelResponse.setFlightId(flightBooking.getFlight().getId());
        flightModelResponse.setBookingDate(flightBooking.getDate());
        flightModelResponse.setId(flightBooking.getId());
        // set flight to Travel Agent Response Object
        bookingResponse.setFlightBooking(flightModelResponse);

        // create Taxi Response Model
        TaxiBooking taxiModelResponse = new TaxiBooking();
        taxiModelResponse.setTaxiId(taxiBooking.getTaxi().getId());
        taxiModelResponse.setBookingDate(taxiBooking.getDateOfBooking());
        taxiModelResponse.setId(taxiBooking.getId());
        // set taxi to Travel Agent Response Object
        bookingResponse.setTaxiBooking(taxiModelResponse);

        // create Hotel Response Model
        HotelBooking hotelBookingModel = new HotelBooking();
        hotelBookingModel.setId(hotelBooking.getId());
        hotelBookingModel.setHotelId(hotelBooking.getHotel().getId());
        hotelBookingModel.setBookingDate(hotelBooking.getBookingDate());
        // set hotel to Travel Agent Response Object
        bookingResponse.setHotelBooking(hotelBookingModel);

        return bookingResponse;
    }

    /**
     * <p>Deletes the provided TravelAgentBooking object from the application database if found there.<p/>
     *
     * @param booking The TravelAgentBooking object to be removed from the application database
     * @return The TravelAgentBooking object that has been successfully removed from the application database; or null
     * @throws Exception if booking does not exist
     */
    TravelAgentBooking delete(TravelAgentBooking booking) throws Exception {
        log.info("TravelAgentBookingService.delete() - Deleting " + booking.toString());

        TravelAgentBooking deletedBooking = null;
        Long flightBookingId = booking.getFlightBookingId();
        Long hotelBookingId = booking.getHotelBookingId();
        Long taxiBookingId = booking.getTaxiBookingId();

        if (booking.getId() == null) {
            log.info("delete() - No ID was found so can't Delete.");
            return null;
        }

        userTransaction.begin();

        // 1. Delete associated Flight Booking
        try {
            flightService.deleteFlightBooking(flightBookingId);
            log.info("Flight with ID: " + flightBookingId + " deleted.");
        } catch (ClientWebApplicationException e) {
            // revert previous booking made
            userTransaction.rollback();
            throw HttpError.throwServiceException(HttpError.FLIGHTBOOKING, null, null, e);
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }

        // 2. Delete associated Taxi Booking
        try {
            taxiService.deleteTaxiBooking(taxiBookingId);
            log.info("Taxi with ID: " + taxiBookingId + " deleted.");
        } catch (ClientWebApplicationException e) {
            // revert previous booking made
            userTransaction.rollback();
            throw HttpError.throwServiceException(HttpError.TAXIBOOKING, null, null, e);
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }

        // 4. Delete associated Hotel Booking
        try {
            hotelBookingService.delete(hotelBookingId);
            log.info("Hotel with ID: " + hotelBookingId + " deleted.");
        } catch (ClientWebApplicationException e) {
            // revert previous booking made
            userTransaction.rollback();
            throw HttpError.throwServiceException(HttpError.HOTELBOOKING, null, null, e);
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }


        // 5. Delete travel agent booking
        deletedBooking = crud.delete(booking);

        userTransaction.commit();

        return deletedBooking;
    }
}
