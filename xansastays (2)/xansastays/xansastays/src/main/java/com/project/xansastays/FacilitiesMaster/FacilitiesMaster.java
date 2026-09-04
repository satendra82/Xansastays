package com.project.xansastays.FacilitiesMaster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.xansastays.RoomMaster.RoomMaster;
import jakarta.persistence.*;

@Entity
@Table(name="facilities_master")
public class FacilitiesMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facilityId;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "room_id")
    private RoomMaster room;

    private int noOfBeds;
    private String spa;
    private String lift;
    private String emergencyService;
    private String roomService;
    private String gameZone;
    private String swimmingPool;
    private String wifi;
    private String parking;
    private String attachedBathroom;
    private String laundry;
    private String garden;
    private String gym;

    public void setRoomId(Long roomId) {
    }

    public enum Dining {
        BREAKFAST,
        LUNCH,
        SNACKS,
        DINNER
    }

    @Enumerated(EnumType.STRING)
    private Dining dining;

    public enum RoomType {
        AC,
        NON_AC
    }

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    public FacilitiesMaster() {
    }

    public Long getFacilityId() { return facilityId; }
    public void setFacilityId(Long facilityId) { this.facilityId = facilityId; }

    public RoomMaster getRoom() { return room; }
    public void setRoom(RoomMaster room) { this.room = room; }

    // Read-only helper — room ke through hi id milega, set nahi ho sakti
    public Long getRoomId() { return room != null ? room.getRoomId() : null; }

    public int getNoOfBeds() { return noOfBeds; }
    public void setNoOfBeds(int noOfBeds) { this.noOfBeds = noOfBeds; }

    public String getSpa() { return spa; }
    public void setSpa(String spa) { this.spa = spa; }

    public String getLift() { return lift; }
    public void setLift(String lift) { this.lift = lift; }

    public String getEmergencyService() { return emergencyService; }
    public void setEmergencyService(String emergencyService) { this.emergencyService = emergencyService; }

    public String getRoomService() { return roomService; }
    public void setRoomService(String roomService) { this.roomService = roomService; }

    public String getGameZone() { return gameZone; }
    public void setGameZone(String gameZone) { this.gameZone = gameZone; }

    public String getSwimmingPool() { return swimmingPool; }
    public void setSwimmingPool(String swimmingPool) { this.swimmingPool = swimmingPool; }

    public String getWifi() { return wifi; }
    public void setWifi(String wifi) { this.wifi = wifi; }

    public String getParking() { return parking; }
    public void setParking(String parking) { this.parking = parking; }

    public String getAttachedBathroom() { return attachedBathroom; }
    public void setAttachedBathroom(String attachedBathroom) { this.attachedBathroom = attachedBathroom; }

    public String getLaundry() { return laundry; }
    public void setLaundry(String laundry) { this.laundry = laundry; }

    public String getGarden() { return garden; }
    public void setGarden(String garden) { this.garden = garden; }

    public String getGym() { return gym; }
    public void setGym(String gym) { this.gym = gym; }

    public Dining getDining() { return dining; }
    public void setDining(Dining dining) { this.dining = dining; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
}