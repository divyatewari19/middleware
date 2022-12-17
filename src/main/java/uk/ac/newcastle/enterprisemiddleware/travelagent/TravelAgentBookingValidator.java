package uk.ac.newcastle.enterprisemiddleware.travelagent;

import uk.ac.newcastle.enterprisemiddleware.booking.CustomerNotFoundException;
import uk.ac.newcastle.enterprisemiddleware.booking.HotelNotFoundException;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;
import uk.ac.newcastle.enterprisemiddleware.travelagent.remotemodel.FlightModel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * <p> This class is responsible for validation and checks based on requirements. </p>
 * @author Divya Tewari
 * */
@ApplicationScoped
public class TravelAgentBookingValidator {
    @Inject
    Validator validator;

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    private CustomerService customerService;

    @Inject
    private HotelService hotelService;

    /**
     * <p>Validates the given Booking object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>Error is thrown if booking hotel and date combination is not unique i.e.
     * There can be only one customer per hotel and date combination.
     * it throws a regular validation exception so that it can be interpreted separately.</p>
     *
     *
     * @param booking The Booking object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If customer, hotel or flight is not found
     */
    void validateBooking(TravelAgentBookingRequest booking, Customer customer, Hotel hotel, FlightModel flight ) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.

        Set<ConstraintViolation<TravelAgentBookingRequest>> violations = validator.validate(booking);

        log.info("Validating booking request...");

        log.info("Checking: Violations");
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        log.info("Checking: If Customer exists");
        if (customer == null) {
            throw new CustomerNotFoundException("Customer does not exist!");
        }

        log.info("Checking: If Hotel exists");
        if (hotel == null) {
            throw new HotelNotFoundException("Hotel does not exist!");
        }

        log.info("Booking request valid" + booking);
    }

}
