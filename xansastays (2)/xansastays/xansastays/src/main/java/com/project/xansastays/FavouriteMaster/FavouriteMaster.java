package com.project.xansastays.FavouriteMaster;

import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.RoomMaster.RoomMaster;
import jakarta.persistence.*;

@Entity
@Table(name="favourite_master")
public class FavouriteMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favouriteId;

    // Guest se connection
    @ManyToOne
    @JoinColumn(name = "guest_id")
    private GuestMaster guest;

    // Room se connection
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomMaster room;

    public FavouriteMaster() {
    }

    public FavouriteMaster(Long favouriteId, GuestMaster guest, RoomMaster room) {
        this.favouriteId = favouriteId;
        this.guest = guest;
        this.room = room;
    }

    public Long getFavouriteId() {
        return favouriteId;
    }

    public void setFavouriteId(Long favouriteId) {
        this.favouriteId = favouriteId;
    }

    public GuestMaster getGuest() {
        return guest;
    }

    public void setGuest(GuestMaster guest) {
        this.guest = guest;
    }

    public RoomMaster getRoom() {
        return room;
    }

    public void setRoom(RoomMaster room) {
        this.room = room;
    }
}