package uk.ac.newcastle.enterprisemiddleware.booking;

import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p> Service Class for validation and business logic. </p>
 * @author Divya Tewari
 * */
@Dependent
public class BookingService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    BookingRepository crud;

    @Inject
    BookingValidator validator;

    /**
     * <p>Returns a List of all persisted {@link Booking} objects, sorted alphabetically by name.<p/>
     *
     * @return List of Booking objects
     */
    List<Booking> findAllOrderedByDate() {
        return crud.findAllOrderedByDate();
    }

    /**
     * <p>Returns a single Booking object, specified by a Long id.<p/>
     *
     * @param id The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    public Booking findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Returns a List of Booking object, with specified by a hotel id and booking date.<p/>
     *
     * @param hotelId The id field of the hotel for which booking was made
     * @param bookingDate date when booking was made
     * @return List of bookings
     */
    List<Booking> findByDateAndHotelId(Long hotelId, Date bookingDate) {
        return crud.findByDateAndHotelId(hotelId, bookingDate);
    }

    /**
     * <p>Returns list of all bookings, specified by a Long customer id.<p/>
     *
     * @param customerId The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    public List<Booking> findByCustomerId(Long customerId) {
        return crud.findByCustomerId(customerId);
    }

    /**
     * <p>Writes the provided Booking object to the application database.<p/>
     *
     * <p>Validates the data in the provided Customer object using a {@link BookingValidator} object.<p/>
     *
     * @param booking The Booking object to be written to the database using a {@link BookingRepository} object
     * @return The Booking object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Booking create(Booking booking) throws Exception {
        log.info("BookingService.create() - Creating " + booking.getBookingDate());

        // Check to make sure the data fits with the parameters in the Booking model and passes validation.
        validator.validateBooking(booking);

        // Write the booking to the database.
        return crud.create(booking);
    }

    /**
     * <p>Deletes the provided Booking object from the application database if found there.<p/>
     *
     * @param id The id of Booking object to be removed from the application database
     * @return The Booking object that has been successfully removed from the application database; or null
     * @throws Exception if booking does not exist
     */
     public Booking delete(Long id) throws Exception {
        Booking deletedBooking = null;
        Booking booking = crud.findById(id);
         log.info("Enter delete() - Deleting ");

        if (booking == null) {
            // Verify that the booking exists. Return 404, if not present.
            throw new RestServiceException("No Booking with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        if (booking.getId() != null) {
            log.info("delete() - Deleting " + booking.toString());
            deletedBooking = crud.delete(booking);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedBooking;
    }
}
