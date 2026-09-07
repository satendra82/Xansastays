package com.project.xansastays.ReviewMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewMaster, Long> {

    // Ek hotel ke saare reviews laata hai
    List<ReviewMaster> findByHotel_HotelId(Long hotelId);

    // Ek hotel ki average rating calculate karta hai
    @Query("SELECT AVG(r.rating) FROM ReviewMaster r WHERE r.hotel.hotelId = :hotelId")
    Double findAverageRatingByHotelId(@Param("hotelId") Long hotelId);

    // Homepage ke liye ek hotel ke latest 6 reviews (sabse naya review sabse pehle)
    List<ReviewMaster> findTop6ByHotel_HotelIdOrderByReviewIdDesc(Long hotelId);

    // FIX: Duplicate review check — ek guest ek hotel ko ek hi baar review kar sake
    boolean existsByGuest_GuestIdAndHotel_HotelId(Long guestId, Long hotelId);
    Optional<ReviewMaster> findByGuest_GuestIdAndHotel_HotelId(Long guestId, Long hotelId);
}