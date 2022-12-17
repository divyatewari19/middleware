package uk.ac.newcastle.enterprisemiddleware.hotel;

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
public class HotelService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    HotelRepository crud;

    @Inject
    HotelValidator validator;

    /**
     * <p>Returns a List of all persisted {@link Hotel} objects, sorted alphabetically by name.<p/>
     *
     * @return List of Hotel objects
     */
    List<Hotel> findAllOrderedByName() {
        return crud.findAllOrderedByName();
    }

    /**
     * <p>Returns a List of all persisted {@link Hotel} objects, filtered by name.<p/>
     *
     * @return List of Hotel objects
     */
    List<Hotel> findAllByName(String name) {
        return crud.findAllByName(name);
    }

    /**
     * <p>Returns a single Hotel object, specified by a Long id.<p/>
     *
     * @param id The id field of the Hotel to be returned
     * @return The Hotel with the specified id
     */
    public Hotel findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Writes the provided Hotel object to the application database.<p/>
     *
     * <p>Validates the data in the provided Hotel object using a {@link HotelValidator} object.<p/>
     *
     * @param hotel The Hotel object to be written to the database using a {@link HotelRepository} object
     * @return The Hotel object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Hotel create(Hotel hotel) throws Exception {
        log.info("HotelService.create() - Creating " + hotel.getName());

        // Check to make sure the data fits with the parameters in the Hotel model and passes validation.
        validator.validateHotel(hotel);

        // Write the hotel to the database.
        return crud.create(hotel);
    }

    /**
     * <p>Deletes the provided Hotel object from the application database if found there.<p/>
     *
     * @param hotel The Hotel object to be removed from the application database
     * @return The Hotel object that has been successfully removed from the application database; or null
     * @throws Exception Throws an exception
     */
    Hotel delete(Hotel hotel) throws Exception {
        log.info("delete() - Deleting " + hotel.toString());

        Hotel deletedHotel = null;

        if (hotel.getId() != null) {
            deletedHotel = crud.delete(hotel);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedHotel;
    }
}
