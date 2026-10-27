package com.project.xansastays.RoomMaster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.xansastays.HotelMaster.HotelMaster;
import com.project.xansastays.ImageMaster.ImageMaster;
import com.project.xansastays.ImageMaster.ImageRepository;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_master")
public class RoomMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double pricePerDay;

    @Column(length = 2000)
    private String description;

    private Integer maxGuests;
    private Double areaSqm;
    private String bedType;
    private String viewType;

    @Column(length = 1000)
    private String facilities;

    public enum Status {
        AVAILABLE,
        BOOKED,
        UNDER_MAINTENANCE
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum RoomType {
        SINGLE,
        DOUBLE,
        SUITE,
        DORMITORY
    }

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    // LAZY: har room ke saath hotel ki alag query nahi chalegi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private HotelMaster hotel;

    @JsonIgnore
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImageMaster> images = new ArrayList<>();

    // DB column nahi: sirf image ki id/naam (bytes nahi). Controller isse bharta hai.
    @Transient
    @JsonIgnore
    private List<ImageRepository.RoomImageInfo> imageList = new ArrayList<>();

    // NOTE: 'facility' (@OneToOne mappedBy) hata diya, wo har room par extra query chalata tha.
    // Facility chahiye to FacilitiesRepository.findByRoomId(roomId) use karo.

    public RoomMaster() {}

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(Double pricePerDay) { this.pricePerDay = pricePerDay; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxGuests() { return maxGuests; }
    public void setMaxGuests(Integer maxGuests) { this.maxGuests = maxGuests; }

    public Double getAreaSqm() { return areaSqm; }
    public void setAreaSqm(Double areaSqm) { this.areaSqm = areaSqm; }

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public String getViewType() { return viewType; }
    public void setViewType(String viewType) { this.viewType = viewType; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public HotelMaster getHotel() { return hotel; }
    public void setHotel(HotelMaster hotel) { this.hotel = hotel; }

    public List<ImageMaster> getImages() { return images; }
    public void setImages(List<ImageMaster> images) { this.images = images; }

    public List<ImageRepository.RoomImageInfo> getImageList() { return imageList; }
    public void setImageList(List<ImageRepository.RoomImageInfo> imageList) { this.imageList = imageList; }
}