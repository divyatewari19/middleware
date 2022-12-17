package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.validation.ValidationException;

public class CustomerNotFoundException extends ValidationException {
    /**
     * <p>ValidationException caused if a Customer is not registered or not present in the database.</p>
     *
     * <p>This violates the uniqueness constraint.</p>
     *
     * @author Divya Tewari
     * @see Booking
     */
    public CustomerNotFoundException(String message){
        super(message);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomerNotFoundException(Throwable cause) {
        super(cause);
    }
}
