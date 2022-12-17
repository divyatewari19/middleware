package uk.ac.newcastle.enterprisemiddleware.travelagent.remotemodel;

import java.util.Objects;

/**
 * <p>POJO class responsible for serialising requests to/from client</p>
 * @author Divya Tewari
 * */
public class TaxiModel {
    private Long id;
    private String registrationNumber;
    // min=2, max=20
    private Integer numberOfSeats;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaxiModel)) return false;
        TaxiModel taxi = (TaxiModel) o;
        return registrationNumber.equals(taxi.registrationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(registrationNumber);
    }
}
