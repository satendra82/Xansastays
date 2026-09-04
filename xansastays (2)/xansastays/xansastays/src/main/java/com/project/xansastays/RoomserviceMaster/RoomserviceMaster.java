package com.project.xansastays.RoomserviceMaster;

import com.project.xansastays.FacilitiesMaster.FacilitiesMaster;
import com.project.xansastays.GuestMaster.GuestMaster;
import jakarta.persistence.*;

@Entity
@Table(name="roomservice_master")
public class RoomserviceMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomserviceId;

    public RoomserviceMaster() {
    }

    public RoomserviceMaster(Long roomserviceId) {
        this.roomserviceId = roomserviceId;
    }

    public Long getRoomserviceId() {
        return roomserviceId;
    }

    public void setRoomserviceId(Long roomserviceId) {
        this.roomserviceId = roomserviceId;
    }
}
