package uk.ac.newcastle.enterprisemiddleware.hotel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;

/**
 * <p> This class is responsible for validation and checks based on requirements. </p>
 * @author Divya Tewari
 * */
@ApplicationScoped
public class HotelValidator {
    @Inject
    Validator validator;

    @Inject
    HotelRepository crud;

    /**
     * <p>Validates the given Hotel object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing hotel with the same phoneNumber is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     *
     * @param hotel The Hotel object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If hotel with the same phoneNumber already exists
     */
    void validateHotel(Hotel hotel) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Hotel>> violations = validator.validate(hotel);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the phoneNumber
        if (phoneNumberAlreadyExists(hotel.getPhoneNumber(), hotel.getId())) {
            throw new UniquePhoneNumberException("Unique Phone Number Violation");
        }
    }

    /**
     * <p>Checks if a hotel with the same phone number is already registered.</p>
     *
     * <p>Since Update will being using an phoneNumber that is already in the database we need to make sure that it is the phoneNumber
     * from the record being updated.</p>
     *
     * @param phoneNumber The phoneNumber to check is unique
     * @param id The user id to check the phoneNumber against if it was found
     * @return boolean which represents whether the phoneNumber was found, and if so if it belongs to the user with id
     */
    boolean phoneNumberAlreadyExists(String phoneNumber, Long id) {
        Hotel hotel = null;
        Hotel hotelWithID;
        try {
            hotel = crud.findByPhoneNumber(phoneNumber);
        } catch (NoResultException e) {
            // ignore
        }

        if (hotel != null && id != null) {
            try {
                hotelWithID = crud.findById(id);
                if (hotelWithID != null && hotelWithID.getPhoneNumber().equals(phoneNumber)) {
                    hotel = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return hotel != null;
    }
}
