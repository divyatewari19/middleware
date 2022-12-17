package uk.ac.newcastle.enterprisemiddleware.booking;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * <p> This class is responsible for validation and checks based on requirements. </p>
 * @author Divya Tewari
 * */
@ApplicationScoped
public class BookingValidator {
    @Inject
    Validator validator;

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    private CustomerService customerService;

    @Inject
    private HotelService hotelService;

    @Inject
    private BookingService bookingService;
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
     * @throws ValidationException If booking with the same hotel/date combination exists already exists
     */
    void validateBooking(Booking booking) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.

        Customer customer = booking.getCustomer();
        Hotel hotel = booking.getHotel();
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);

        log.info("Validating booking request");

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        if (customer == null || customerNotFound(customer.getId())) {
            throw new CustomerNotFoundException("Customer " + customer +" does not exist!");
        }

        if (hotel == null || hotelNotFound(hotel.getId())) {
            throw new HotelNotFoundException("Hotel with " + hotel +" does not exist");
        }

        // Check the uniqueness of hotel + booking date
        if (bookingAlreadyExists(booking.getBookingDate(), hotel.getId())) {
            throw new UniqueBookingException("Unique Booking Violation");
        }

        log.info("Booking request valid" + booking);
    }

    /**
     * Validates if customer exists in the database
     *
     * @param customerId customer who made the booking
     * @return boolean indicating if customer does not exist in database
     * */
    private boolean customerNotFound(Long customerId) {
        return customerService.findById(customerId) == null;
    }

    /**
     * Validates if hotel exists in the database
     *
     * @param hotelId hotel for which booking was made
     * @return boolean indicating if hotel does not exist in database
     * */
    private boolean hotelNotFound(Long hotelId) {
        return hotelService.findById(hotelId) == null;
    }

    /**
     * Checks if booking already exists based uniqueness of booking date and hotel
     *
     * @param bookingDate date on which booking is made
     * @param hotelId hotel for which booking was made
     * @return boolean indicating if booking already exists
     * */
    private boolean bookingAlreadyExists(Date bookingDate, Long hotelId) {
        List<Booking> bookings = bookingService.findByDateAndHotelId(hotelId, bookingDate);
        return bookings.size() > 0;
    }
}
