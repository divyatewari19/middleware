package uk.ac.newcastle.enterprisemiddleware.travelagent;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class TravelAgentBookingRepository {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    EntityManager em;

    /**
     * <p>Persists the TravelAgentBooking object to the application database using the EntityManager.</p>
     *
     * @param booking The TravelAgentBooking object to be persisted
     * @return The TravelAgentBooking object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    TravelAgentBooking create(TravelAgentBooking booking) throws Exception {
        log.info("TravelAgentBookingRepository.create() - CREATING for customer =  " + booking.getCustomer());

        // Write the booking to the database.
        em.persist(booking);

        return booking;
    }

    /**
     * <p>Returns a List of all persisted {@link TravelAgentBooking} objects.</p>
     *
     * @return List of TravelAgentBooking objects
     */
    List<TravelAgentBooking> findAllOrderedByDate() {
        TypedQuery<TravelAgentBooking> query = em.createNamedQuery(TravelAgentBooking.FIND_ALL, TravelAgentBooking.class);
        return query.getResultList();
    }

    /**
     * <p>Returns a TravelAgentBooking object fetched by a id.<p/>
     *
     * @param id The id of the TravelAgentBooking to be returned
     * @return The TravelAgentBooking with the specified id
     */
    TravelAgentBooking findById(Long id) {
        return em.find(TravelAgentBooking.class, id);
    }

    /**
     * <p> Returns a list of booking made by a customer id. </p>
     * @param customerId customer id
     * @return List of bookings
     */
    public List<TravelAgentBooking> findByCustomerId (Long customerId) {
        TypedQuery<TravelAgentBooking> query = em.createNamedQuery(TravelAgentBooking.FIND_BY_CUSTOMER_ID, TravelAgentBooking.class).setParameter("customerId", customerId);
        return query.getResultList();
    }

    /**
     * <p>Deletes the provided TravelAgentBooking object from the application database if found there</p>
     *
     * @param booking The TravelAgentBooking object to be removed from the application database
     * @return The TravelAgentBooking object that has been successfully removed from the application database; or null
     * @throws Exception if booking does not exist
     */
    TravelAgentBooking delete(TravelAgentBooking booking) throws Exception {
        log.info("TravelAgentBookingRepository.delete() - DELETING for customer =  " + booking.getCustomer() + " , bookingID =  " + booking.getId());

        if (booking.getId() != null) {
            em.remove(em.merge(booking));
        } else {
            log.info("TravelAgentBookingRepository.delete() - No ID was found so can't Delete.");
        }

        return booking;
    }
}
