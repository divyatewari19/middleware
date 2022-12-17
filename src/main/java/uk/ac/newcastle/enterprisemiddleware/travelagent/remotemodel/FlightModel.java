package uk.ac.newcastle.enterprisemiddleware.travelagent.remotemodel;

import java.io.Serializable;
import java.util.*;

/**
 * <p>Simple model object representing Flight objects which are linked to remote objects</p>
 *
 * @author Divya Tewari
 */
public class FlightModel implements Serializable {
    private Long id;
    private String number;
    private String origin;

    private String destination;

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getNumber(){
        return number;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public String getOrigin(){
        return origin;
    }

    public void setOrigin(String origin){
        this.origin = origin;
    }

    public String getDestination(){
        return destination;
    }

    public void setDestination(String destination){
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightModel)) return false;
        FlightModel flight = (FlightModel) o;
        return number.equals(flight.number);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(number);
    }
}
