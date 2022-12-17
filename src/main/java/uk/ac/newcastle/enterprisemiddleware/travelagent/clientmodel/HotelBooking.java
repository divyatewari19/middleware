package uk.ac.newcastle.enterprisemiddleware.travelagent.clientmodel;

import java.util.Date;

/**
 * <p>POJO class  representing the structure of input JSON from the client</p>
 * @author Divya Tewari
 * */
public class HotelBooking {
    private Long id;
    private Long hotelId;
    private Date bookingDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }
}
