package uk.ac.newcastle.enterprisemiddleware.booking;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * <p>This is a domain or entity class which represents how resources are represented in database.
 * The class also defined @NamedQueries through which we can retrieve Customer from database.<p/>
 *
 * @author Divya Tewari
 */
@Entity
@NamedQueries({
        @NamedQuery(name = Booking.FIND_ALL, query = "SELECT p FROM Booking p ORDER BY p.customer.id ASC,p.hotel.id ASC,p.bookingDate DESC"),
        @NamedQuery(name = Booking.FIND_BY_CUSTOMER_ID, query = "SELECT b FROM Booking b WHERE b.customer.id = :customerId"),
        @NamedQuery(name = Booking.FIND_BY_DATE_AND_HOTEL_ID, query = "SELECT b FROM Booking b WHERE b.hotel.id = :hotelId AND b.bookingDate = :bookingDate")
})
public class Booking implements Serializable {

    public static final String FIND_ALL = "Booking.findAll";
    public static final String FIND_BY_CUSTOMER_ID = "Booking.findByCustomerId";
    public static final String FIND_BY_DATE_AND_HOTEL_ID = "Booking.findByDateAndHotelId";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne
    @PrimaryKeyJoinColumn
    // JPA currently requires that the many-to-one side always be the owner, hence no mappedBy
    private Customer customer;

    @NotNull
    @ManyToOne
    @PrimaryKeyJoinColumn
    private Hotel hotel;

    @NotNull
    @Future(message = "Booking date can not be in the past. Please choose one from the future")
    @Column(name = "booking_date")
    @Temporal(TemporalType.DATE)
    private Date bookingDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return getHotel().equals(booking.getHotel()) &&
                getBookingDate().equals(booking.getBookingDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHotel(), getBookingDate());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{ id = " + getId()
                + ", customer = " + getCustomer().getFirstName() + " " + getCustomer().getLastName()
                + ", hotel = " + getHotel().getName()
                + ", bookingDate = " + getBookingDate() + " }";
    }
}
