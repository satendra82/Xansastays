package com.project.xansastays.HotelMaster;

import com.project.xansastays.GuestMaster.GuestMaster;
import jakarta.persistence.*;

@Entity
@Table(name="hotel_master")
public class HotelMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hotelId;
    private String name;
    private String location;
    private String city;

    public enum Status {
        OPEN,
        CLOSED
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    public HotelMaster() {
    }

    public HotelMaster(Long hotelId, String name, String location, String city, Status status) {
        this.hotelId = hotelId;
        this.name = name;
        this.location = location;
        this.city = city;
        this.status = status;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
