package uk.ac.newcastle.enterprisemiddleware.travelagent.remotemodel;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * <p>POJO class serializing request from external services</p>
 * @author Divya Tewari
 * */
public class TaxiBookingModel implements Serializable {

    private Long id;
    private Customer customer;
    private TaxiModel taxi;
    private Date dateOfBooking;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TaxiModel getTaxi() {
        return taxi;
    }

    public void setTaxi(TaxiModel taxi) {
        this.taxi = taxi;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getDateOfBooking() {
        return dateOfBooking;
    }

    public void setDateOfBooking(Date dateOfBooking) {
        this.dateOfBooking = dateOfBooking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        TaxiBookingModel booking = (TaxiBookingModel) o;
        return taxi.equals(booking.getTaxi()) && dateOfBooking.equals(booking.dateOfBooking);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxi, dateOfBooking);
    }
}
