package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.validation.ValidationException;

public class HotelNotFoundException extends ValidationException {
    /**
     * <p>ValidationException caused if a Hotel is not registered or not present in the database.</p>
     *
     * <p>This violates the uniqueness constraint.</p>
     *
     * @author Divya Tewari
     * @see Booking
     */
    public HotelNotFoundException(String message){
        super(message);
    }

    public HotelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public HotelNotFoundException(Throwable cause) {
        super(cause);
    }
}
