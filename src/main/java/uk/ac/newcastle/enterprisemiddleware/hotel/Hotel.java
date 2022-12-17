package uk.ac.newcastle.enterprisemiddleware.hotel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@NamedQueries({
        @NamedQuery(name = Hotel.FIND_ALL, query = "SELECT c FROM Hotel c ORDER BY c.name ASC"),
       @NamedQuery(name = Hotel.FIND_BY_PHONE_NUM, query = "SELECT c FROM Hotel c WHERE c.phoneNumber = :phoneNumber")
})
@Table(name = "hotel", uniqueConstraints = @UniqueConstraint(columnNames = "phone_number"))
public class Hotel implements Serializable {
    public static final String FIND_ALL = "Hotel.findAll";
    public static final String FIND_BY_PHONE_NUM = "Hotel.findByPhoneNum";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[A-Za-z-']+", message = "Please use a name without numbers or specials")
    @Column(name = "name")
    private String name;

    @NotNull
    @Pattern(regexp = "^0\\d{10}$", message = "The phone number starts with a 0, contains only digits and is 11 characters in length.")
    @Column(name = "phone_number")
    private String phoneNumber;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z\\d]{6}$", message = "Please use a non-empty alpha-numerical string which is 6 characters in length")
    @Column(name = "post_code")
    private String postCode;

    @JsonIgnore
    @OneToMany(mappedBy = "hotel", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private Set<Booking> bookings = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hotel)) return false;
        Hotel hotel = (Hotel) o;
        return phoneNumber.equals(hotel.getPhoneNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(phoneNumber);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{ id = " + getId()
                + ", name = " + getName()
                + ", postCode = " + getPostCode()
                + ", phoneNumber = " + getPhoneNumber() + " }";
    }
}
