package com.project.xansastays.ReviewMaster;

import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.HotelMaster.HotelMaster;
import com.project.xansastays.RoomMaster.RoomMaster;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ReviewMaster")
public class ReviewMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    private int rating;

    private String review;

    // FIX: created_at column — auto set hoga jab review save hoga
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // FIX: room_id column
    @ManyToOne
    @JoinColumn(name = "room_id")
    private RoomMaster room;

    // Guest se connection
    @ManyToOne
    @JoinColumn(name = "guest_id")
    private GuestMaster guest;

    // Hotel se connection
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private HotelMaster hotel;

    // FIX: createdAt auto-set before saving
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public ReviewMaster() {}

    public ReviewMaster(Long reviewId, int rating, String review) {
        this.reviewId = reviewId;
        this.rating   = rating;
        this.review   = review;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public RoomMaster getRoom() { return room; }
    public void setRoom(RoomMaster room) { this.room = room; }

    public GuestMaster getGuest() { return guest; }
    public void setGuest(GuestMaster guest) { this.guest = guest; }

    public HotelMaster getHotel() { return hotel; }
    public void setHotel(HotelMaster hotel) { this.hotel = hotel; }
}