package uk.ac.newcastle.enterprisemiddleware.hotel;

import javax.validation.ValidationException;

/**
 * <p>ValidationException caused if a Hotel's email address conflicts with that of another Hotel.</p>
 *
 * <p>This violates the uniqueness constraint.</p>
 *
 * @author Divya Tewari
 * @see Hotel
 */
public class UniquePhoneNumberException extends ValidationException {
    public UniquePhoneNumberException(String message) {
        super(message);
    }

    public UniquePhoneNumberException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniquePhoneNumberException(Throwable cause) {
        super(cause);
    }
}
