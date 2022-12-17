package uk.ac.newcastle.enterprisemiddleware.guestbooking;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * <p>This is a POJO class which deserialize a guest booking for customer and booking object.<p/>
 *
 * @author Divya Tewari
 */

@XmlRootElement
public class GuestBooking implements Serializable {

    private Long id;
    private Customer customer;
    private long hotelId;
    private Date bookingDate;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public long getHotelId() {
        return hotelId;
    }

    public void setHotelId(long hotelId) {
        this.hotelId = hotelId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuestBooking)) return false;
        GuestBooking guestBooking = (GuestBooking) o;
        return getCustomer().equals(guestBooking.getCustomer()) &&
                getHotelId() == guestBooking.getHotelId() &&
                getBookingDate().equals(guestBooking.getBookingDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCustomer(), getHotelId(), getBookingDate());
    }

    public String toString() {
        return "{ customer = " + getCustomer().getFirstName() + "" + getCustomer().getLastName()
                + ", hotelId = " + getHotelId()
                + ", bookingDate = " + getBookingDate() + " }";
    }
}
