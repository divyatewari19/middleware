package uk.ac.newcastle.enterprisemiddleware.customer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p> Service Class for validation and business logic. </p>
 * @author Divya Tewari
 * */
@Dependent
public class CustomerService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    CustomerRepository crud;

    @Inject
    CustomerValidator validator;

    /**
     * <p>Returns a List of all persisted {@link Customer} objects, sorted alphabetically by name.<p/>
     *
     * @return List of Customer objects
     */
    List<Customer> findAllOrderedByName() {
        return crud.findAllOrderedByName();
    }

    /**
     * <p>Returns a List of all persisted {@link Customer} objects, filtered by name.<p/>
     *
     * @return List of Customer objects
     */
    List<Customer> findAllByName(String name) {
        return crud.findAllByName(name);
    }


    /**
     * <p>Returns a single Customer object, specified by a Long id.<p/>
     *
     * @param id The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    public Customer findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Returns a single Customer object, specified by a email.<p/>
     *
     * @param email The email field of the Customer to be returned
     * @return The Customer with the specified id
     */
    public Customer findByEmail(String email) {
        return crud.findByEmail(email);
    }

    /**
     * <p>Writes the provided Customer object to the application database.<p/>
     *
     * <p>Validates the data in the provided Customer object using a {@link CustomerValidator} object.<p/>
     *
     * @param customer The Customer object to be written to the database using a {@link CustomerRepository} object
     * @return The Customer object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Customer create(Customer customer) throws Exception {
        log.info("CustomerService.create() - Creating " + customer.getFirstName() + " " + customer.getLastName());

        // Check to make sure the data fits with the parameters in the Customer model and passes validation.
        validator.validateCustomer(customer);

        // Write the customer to the database.
        return crud.create(customer);
    }

    /**
     * <p>Deletes the provided Customer object from the application database if found there.<p/>
     *
     * @param customer The Customer object to be removed from the application database
     * @return The Customer object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Customer delete(Customer customer) throws Exception {
        log.info("delete() - Deleting " + customer.getFirstName() + " " + customer.getLastName());

        Customer deletedCustomer = null;

        if (customer.getId() != null) {
            deletedCustomer = crud.delete(customer);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedCustomer;
    }
}
