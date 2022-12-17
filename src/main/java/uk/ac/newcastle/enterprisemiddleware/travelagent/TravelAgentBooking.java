package uk.ac.newcastle.enterprisemiddleware.travelagent;

import org.hibernate.annotations.CreationTimestamp;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>This is a domain or entity class which represents how resources are represented in database.
 * The class also defined @NamedQueries through which we can retrieve Booking from database.<p/>\
 * @author Divya Tewari
 */
@Entity
@NamedQueries({
        @NamedQuery(name = TravelAgentBooking.FIND_ALL, query = "SELECT p FROM TravelAgentBooking p ORDER BY p.customer.id ASC,p.hotelBookingId ASC,p.createdOn DESC"),
        @NamedQuery(name = TravelAgentBooking.FIND_BY_CUSTOMER_ID, query = "SELECT b FROM TravelAgentBooking b WHERE b.customer.id = :customerId")
})
@XmlRootElement
@Table(name = "travel_agent")
public class TravelAgentBooking implements Serializable {
    public static final String FIND_ALL = "TravelAgentBooking.findAll";
    public static final String FIND_BY_CUSTOMER_ID = "TravelAgentBooking.findByCustomerId";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne
    @PrimaryKeyJoinColumn
    // JPA currently requires that the many-to-one side always be the owner, hence no mappedBy
    private Customer customer;

    @Column(name = "flight_booking_id")
    private Long flightBookingId;

    @Column(name = "hotel_booking_id")
    private Long hotelBookingId;

    @Column(name = "taxi_booking_id")
    private Long taxiBookingId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "creation_date")
    private Date createdOn;

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

    public Long getHotelBookingId() {
        return hotelBookingId;
    }

    public void setHotelBookingId(Long hotelBookingId) {
        this.hotelBookingId = hotelBookingId;
    }

    public Long getFlightBookingId() {
        return flightBookingId;
    }

    public void setFlightBookingId(Long flightBookingId) {
        this.flightBookingId = flightBookingId;
    }

    public Long getTaxiBookingId() {
        return taxiBookingId;
    }

    public void setTaxiBookingId(Long taxiBookingId) {
        this.taxiBookingId = taxiBookingId;
    }
}
