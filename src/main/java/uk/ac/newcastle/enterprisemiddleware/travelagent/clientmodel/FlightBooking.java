package uk.ac.newcastle.enterprisemiddleware.travelagent.clientmodel;

import java.util.Date;

/**
 * <p>POJO class  representing the structure of input JSON from the client</p>
 * @author Divya Tewari
 * */
public class FlightBooking {
    private Long id;
    private Long flightId;
    private Date bookingDate;

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

}
