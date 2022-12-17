package uk.ac.newcastle.enterprisemiddleware.hotel;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is a repository class that connects service layer of the Hotel to the entity Object
 *
 *  @author Divya Tewari
 *  @see Hotel
 * */
@RequestScoped
public class HotelRepository {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    EntityManager em;

    /**
     * <p>Returns a List of all persisted {@link Hotel} objects, sorted alphabetically by last name.</p>
     *
     * @return List of Hotel objects
     */
    List<Hotel> findAllOrderedByName() {
        TypedQuery<Hotel> query = em.createNamedQuery(Hotel.FIND_ALL, Hotel.class);
        return query.getResultList();
    }

    /**
     * <p>Returns a list of Hotel objects, specified by a String name.<p/>
     *
     * @param name The name field of the Hotels to be returned
     * @return The Hotels with the specified name
     */
    List<Hotel> findAllByName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Hotel> criteria = cb.createQuery(Hotel.class);
        Root<Hotel> hotel = criteria.from(Hotel.class);
        criteria.select(hotel).where(cb.equal(hotel.get("name"), name));
        return em.createQuery(criteria).getResultList();
    }

    /**
     * <p>Returns a Hotel object fetched by a id.<p/>
     *
     * @param id The id of the Hotel to be returned
     * @return The Hotel with the specified id
     */
    Hotel findById(Long id) {
        return em.find(Hotel.class, id);
    }

    /**
     * <p>Returns a Hotel object, specified by a String phone number.</p>
     *
     * <p>If there is more than one Hotel with the specified phone number, only the first encountered will be returned.<p/>
     *
     * @param phoneNumber The  field of the Hotel to be returned
     * @return The first Hotel with the specified phone number
     */
    Hotel findByPhoneNumber(String phoneNumber) {
        TypedQuery<Hotel> query = em.createNamedQuery(Hotel.FIND_BY_PHONE_NUM, Hotel.class).setParameter("phoneNumber", phoneNumber);
        return query.getSingleResult();
    }

    /**
     * <p>Persists the Hotel object to the application database using the EntityManager.</p>
     *
     * @param hotel The Hotel object to be persisted
     * @return The Hotel object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Hotel create(Hotel hotel) throws Exception {
        log.info("HotelRepository.create() - Creating " + hotel.getName());

        // Write the hotel to the database.
        em.persist(hotel);

        return hotel;
    }

    /**
     * <p>Deletes the provided Hotel object from the application database if found there</p>
     *
     * @param hotel The Hotel object to be removed from the application database
     * @return The Hotel object that has been successfully removed from the application database; or null
     * @throws Exception if hotel does not exist
     */
    Hotel delete(Hotel hotel) throws Exception {
        log.info("HotelRepository.create() - DELETING - " + hotel.getName());

        if (hotel.getId() != null) {
            em.remove(em.merge(hotel));

        } else {
            log.info("HotelRepository.delete() - No ID was found so can't Delete.");
        }

        return hotel;
    }
}
