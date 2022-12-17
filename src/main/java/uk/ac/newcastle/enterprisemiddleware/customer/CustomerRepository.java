package uk.ac.newcastle.enterprisemiddleware.customer;

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
 * This is a repository class that connects service layer of the Customer to the entity Object
 *
 *  @author Divya Tewari
 *  @see Customer
 * */
@RequestScoped
public class CustomerRepository {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    EntityManager em;

    /**
     * <p>Returns a List of all persisted {@link Customer} objects, sorted alphabetically by last name.</p>
     *
     * @return List of Customer objects
     */
    List<Customer> findAllOrderedByName() {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_ALL, Customer.class);
        return query.getResultList();
    }

    /**
     * <p>Returns a list of Customer objects, specified by a String name.<p/>
     *
     * @param name The name field of the Customers to be returned
     * @return The Customers with the specified name
     */
    List<Customer> findAllByName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Customer> criteria = cb.createQuery(Customer.class);
        Root<Customer> customer = criteria.from(Customer.class);
        criteria.select(customer).where(cb.equal(customer.get("name"), name));
        return em.createQuery(criteria).getResultList();
    }

    /**
     * <p>Returns a Customer object fetched by a id.<p/>
     *
     * @param id The id of the Customer to be returned
     * @return The Customer with the specified id
     */
    Customer findById(Long id) {
        return em.find(Customer.class, id);
    }

    /**
     * <p>Returns a Customer object, specified by a String email.</p>
     *
     * <p>If there is more than one Customer with the specified email, only the first encountered will be returned.<p/>
     *
     * @param email The email field of the Customer to be returned
     * @return The first Customer with the specified email
     */
    Customer findByEmail(String email) {
        Customer result = null;
        try {
            TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_BY_EMAIL, Customer.class).setParameter("email", email);
            result = query.getSingleResult();
        } catch (Exception e) {
            log.info("FIND_BY_EMAIL: No Results found!");
        }

        return result;
    }
    /**
     * <p>Persists the Customer object to the application database using the EntityManager.</
     *
     * @param customer The Customer object to be persisted
     * @return The Customer object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Customer create(Customer customer) throws Exception {
        log.info("CustomerRepository.create() - Creating " + customer.getFirstName() + " " + customer.getLastName());

        // Write the customer to the database.
        em.persist(customer);

        return customer;
    }

    /**
     * <p>Deletes the provided Customer object from the application database if found there</p>
     *
     * @param customer The Customer object to be removed from the application database
     * @return The Customer object that has been successfully removed from the application database; or null
     * @throws Exception if customer does not exist
     */
    Customer delete(Customer customer) throws Exception {
        log.info("CustomerRepository.create() - DELETING - " + customer.getFirstName() + " " + customer.getLastName());

        if (customer.getId() != null) {
            em.remove(em.merge(customer));

        } else {
            log.info("CustomerRepository.delete() - No ID was found so can't Delete.");
        }

        return customer;
    }
}
