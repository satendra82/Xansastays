package com.project.xansastays.amenities;

import com.project.xansastays.RoomMaster.RoomMaster;
import jakarta.persistence.*;

@Entity
@Table(name = "amenities_master")
public class AmenitiesMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String amenitiesName;
    private Double price;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private RoomMaster room;

    public AmenitiesMaster() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAmenitiesName() {
        return amenitiesName;
    }

    public void setAmenitiesName(String amenitiesName) {
        this.amenitiesName = amenitiesName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public RoomMaster getRoom() {
        return room;
    }

    public void setRoom(RoomMaster room) {
        this.room = room;
    }
}