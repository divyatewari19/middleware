package uk.ac.newcastle.enterprisemiddleware.travelagent.clientmodel;

import java.util.Date;

/**
 * <p>POJO class  representing the structure of input JSON from the client</p>
 * @author Divya Tewari
 * */
public class TaxiBooking {
    private Long id;
    private Long taxiId;
    private Date bookingDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaxiId() {
        return taxiId;
    }

    public void setTaxiId(Long taxiId) {
        this.taxiId = taxiId;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }
}
