package uk.ac.newcastle.enterprisemiddleware.guestbooking;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
public class GuestBookingValidator {
    @Inject
    Validator validator;

    void validateGuestBooking(GuestBooking guestBooking) throws ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<GuestBooking>> violations = validator.validate(guestBooking);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

    }
}
