package uk.ac.newcastle.enterprisemiddleware.travelagent;

import uk.ac.newcastle.enterprisemiddleware.travelagent.clientmodel.FlightBooking;
import uk.ac.newcastle.enterprisemiddleware.travelagent.clientmodel.HotelBooking;
import uk.ac.newcastle.enterprisemiddleware.travelagent.clientmodel.TaxiBooking;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * <p>A simple DTO class to intercept request and response from client and connects it to the Domain/Entity Object. </p>*/
public class TravelAgentBookingRequest {
//    private Customer customer;
    @NotNull
    private Long id;
    @NotNull
    private Long customerId;
    @NotNull
    private FlightBooking flightBooking;
    @NotNull
    private HotelBooking hotelBooking;

    @NotNull
    private TaxiBooking taxiBooking;

    private Date createdOn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public FlightBooking getFlightBooking() {
        return flightBooking;
    }

    public void setFlightBooking(FlightBooking flightBooking) {
        this.flightBooking = flightBooking;
    }

    public HotelBooking getHotelBooking() {
        return hotelBooking;
    }

    public void setHotelBooking(HotelBooking hotelBooking) {
        this.hotelBooking = hotelBooking;
    }

    public TaxiBooking getTaxiBooking() {
        return taxiBooking;
    }

    public void setTaxiBooking(TaxiBooking taxiBooking) {
        this.taxiBooking = taxiBooking;
    }
}
