package uk.ac.newcastle.enterprisemiddleware.guestbooking;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;
import uk.ac.newcastle.enterprisemiddleware.booking.HotelNotFoundException;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerValidator;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;
import java.util.logging.Logger;

/**
 * <p> Service Class for validation and business logic. </p>
 * @author Divya Tewari
 * */
@ApplicationScoped
public class GuestBookingService {

    @Inject
    private @Named("logger")
    Logger log;

    @Inject
    private GuestBookingValidator validator;

    @Inject
    private CustomerService customerService;

    @Inject
    private HotelService hotelService;

    @Inject
    BookingService bookingService;

    @Inject
    UserTransaction userTransaction;

    /**
     * <p> Creates a guest booking in a single transaction using the customer and hotel service.
     *  Failure at any point will rollback the transaction</p>
     *
     * <p>Validates the data in the provided Customer object using a {@link CustomerValidator} object.<p/>
     *
     * @param guestBooking The GuestBooking object to be written to the database
     * @return The GuestBooking object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Booking create(GuestBooking guestBooking) throws Exception {
        log.info("GuestBookingService.create() - Creating " + guestBooking.getCustomer().getFirstName() + " " + guestBooking.getCustomer().getLastName());

        // Check to make sure the data fits with the parameters in the Customer model and passes validation.
        validator.validateGuestBooking(guestBooking);
        Booking booking;

        try {
            userTransaction.begin();
            // 1. if customer exists, fetch from DB
            // else create a new customer
            Customer customer = customerService.findByEmail(guestBooking.getCustomer().getEmail());
            if (customer == null) {
                log.info("Customer does not exits.. Creating a new Customer");
                customer = new Customer();
                customer.setCustomerDetails(guestBooking.getCustomer());
                customer = customerService.create(customer);
            }
            log.info("Customer Details Fetched/Created:  " + customer);

            // 2. find hotel
            Hotel hotel = hotelService.findById(guestBooking.getHotelId());
            if (hotel == null) {
                // hotel not found handled while creating a booking
                log.severe("Hotel not found for id: " + guestBooking.getHotelId());
                throw new HotelNotFoundException("Hotel does not exist!");
            }
            log.info("Hotel details: " + hotel);

            // 3. Create a booking
            booking = new Booking();
            booking.setId(null);
            booking.setCustomer(customer);
            booking.setHotel(hotel);
            booking.setBookingDate(guestBooking.getBookingDate());
            bookingService.create(booking);
            userTransaction.commit();

        } catch (Exception e) {
            e.printStackTrace();
            userTransaction.rollback();
            throw e;
        }

        // Write the customer to the database.
        return booking;
    }
}
