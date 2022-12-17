package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.validation.ValidationException;

/**
 * <p>ValidationException caused if a Customer's hotel and date combination are same.</p>
 *
 * <p>This violates the uniqueness constraint.</p>
 *
 * @author Divya Tewari
 * @see Booking
 */
public class UniqueBookingException extends ValidationException {
    public UniqueBookingException(String message){
        super(message);
    }

    public UniqueBookingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueBookingException(Throwable cause) {
        super(cause);
    }
}
