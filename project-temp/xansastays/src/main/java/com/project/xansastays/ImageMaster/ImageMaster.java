package com.project.xansastays.ImageMaster;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.HotelMaster.HotelMaster;
import com.project.xansastays.RoomMaster.RoomMaster;
import jakarta.persistence.*;

@Entity
@Table(name = "image_master")
public class ImageMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column(name = "room_img", columnDefinition = "bytea")
    private byte[] roomImg;

    private String contentType;

    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private HotelMaster hotel;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomMaster room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private GuestMaster guest;

    public ImageMaster() {
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public byte[] getRoomImg() {
        return roomImg;
    }

    public void setRoomImg(byte[] roomImg) {
        this.roomImg = roomImg;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public HotelMaster getHotel() {
        return hotel;
    }

    public void setHotel(HotelMaster hotel) {
        this.hotel = hotel;
    }

    public RoomMaster getRoom() {
        return room;
    }

    public void setRoom(RoomMaster room) {
        this.room = room;
    }

    public GuestMaster getGuest() {
        return guest;
    }

    public void setGuest(GuestMaster guest) {
        this.guest = guest;
    }
}