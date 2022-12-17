package uk.ac.newcastle.enterprisemiddleware.travelagent.remotemodel;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * <p>POJO class serializing request from external services</p>
 * @author Divya Tewari
 * */
public class FlightBookingModel implements Serializable {

    private Long id;
    private Customer customer;
    private FlightModel flight;
    private Date date;

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public FlightModel getFlight() {
        return flight;
    }

    public void setFlight(FlightModel flight) {
        this.flight = flight;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightBookingModel)) return false;
        FlightBookingModel booking = (FlightBookingModel) o;
        return id.equals(booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "{ id = " + getId()
                + ", customer = " + getCustomer().getFirstName()
                + ", flight = " + getFlight().getNumber()
                + ", Date = " + getDate()
                + " }";
    }
}
