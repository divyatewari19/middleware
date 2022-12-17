package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class BookingRepository {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    EntityManager em;

    /**
     * <p>Persists the Booking object to the application database using the EntityManager.</p>
     *
     * @param booking The Booking object to be persisted
     * @return The Booking object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Booking create(Booking booking) {
        log.info("BookingRepository.create() - CREATING - customer =  " + booking.getCustomer() + ", hotel = " + booking.getHotel() + ", bookingDate = " + booking.getBookingDate());

        // Write the booking to the database.
        em.persist(booking);
        // refreshes booking object to send back to the client -> nested objects are not getting refreshed
        em.refresh(booking);

        return booking;
    }

    /**
     * <p>Returns a List of all persisted {@link Booking} objects.</p>
     *
     * @return List of Booking objects
     */
    List<Booking> findAllOrderedByDate() {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_ALL, Booking.class);
        return query.getResultList();
    }

    /**
     * <p>Returns a Booking object fetched by a id.<p/>
     *
     * @param id The id of the Booking to be returned
     * @return The Booking with the specified id
     */
    Booking findById(Long id) {
        return em.find(Booking.class, id);
    }

    /**
     * <p> Returns a list of booking made by a customer id. </p>
     * @param customerId customer id
     * @return List of bookings
     */
    public List<Booking> findByCustomerId (Long customerId) {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_BY_CUSTOMER_ID, Booking.class).setParameter("customerId", customerId);
        return query.getResultList();
    }

    /**
     * <p>Returns a List of Bookings made by customer on a specific hotel and date.<p/>
     *
     * @param hotelId The id field of the hotel for which booking was made
     * @param bookingDate date when booking was made
     * @return List of bookings
     */
    public List<Booking> findByDateAndHotelId(Long hotelId, Date bookingDate) {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_BY_DATE_AND_HOTEL_ID, Booking.class)
                .setParameter("hotelId", hotelId)
                .setParameter("bookingDate", bookingDate);
        return query.getResultList();
    }

    /**
     * <p>Deletes the provided Booking object from the application database if found there</p>
     *
     * @param booking The Booking object to be removed from the application database
     * @return The Booking object that has been successfully removed from the application database; or null
     * @throws Exception if booking does not exist
     */
    Booking delete(Booking booking) {
        log.info("BookingRepository.delete() - DELETING - customer =  " + booking.getCustomer() + " , hotel =  " + booking.getHotel() + " , bookingDate = " + booking.getBookingDate());

        try {
            if (booking.getId() != null) {
                em.remove(em.merge(booking));
            } else {
                log.info("BookingRepository.delete() - No ID was found so can't Delete.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }


        return booking;
    }

}
