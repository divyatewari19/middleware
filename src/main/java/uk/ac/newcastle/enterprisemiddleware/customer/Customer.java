package uk.ac.newcastle.enterprisemiddleware.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
* <p>This is a domain or entity class which represents how resources are represented in database.
* The class also defined @NamedQueries through which we can retrieve Customer from database.<p/>
*
* @author Divya Tewari
*/

@Entity
@NamedQueries({
        @NamedQuery(name = Customer.FIND_ALL, query = "SELECT c FROM Customer c ORDER BY c.lastName ASC, c.firstName ASC"),
        @NamedQuery(name = Customer.FIND_BY_EMAIL, query = "SELECT c FROM Customer c WHERE c.email = :email")
})
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Customer implements Serializable {
    public static final String FIND_ALL = "Customer.findAll";
    public static final String FIND_BY_EMAIL = "Customer.findByEmail";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[A-Za-z-']+", message = "Please use a name without numbers or specials")
    @Column(name = "first_name")
    private String firstName;

    @NotNull
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[A-Za-z-']+", message = "Please use a name without numbers or specials")
    @Column(name = "last_name")
    private String lastName;

    @NotNull
    @NotEmpty
    @Email(message = "The email address must be in the format of name@domain.com")
    private String email;

    @NotNull
    @Pattern(regexp = "^0\\d{10}$", message = "The phone number starts with a 0, contains only digits and is 11 characters in length.")
    @Column(name = "phone_number")
    private String phoneNumber;

    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private Set<Booking> bookings = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Set<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    @JsonIgnore
    public void setCustomerDetails(Customer customer) {
        this.firstName = customer.firstName;
        this.lastName = customer.lastName;
        this.email = customer.email;
        this.phoneNumber = customer.phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return email.equals(customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{ id = " + getId()
                + ", name = " + getFirstName() + "  " + getLastName()
                + ", email = " + getEmail()
                + ", phoneNumber = " + getPhoneNumber() + " }";
    }
}