package com.project.xansastays.BookingMaster;

import com.project.xansastays.GuestMaster.GuestMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingMaster,Long> {
    List<BookingMaster> findByGuestOrderByCreatedAtDesc(GuestMaster guest);

    List<BookingMaster> findByStatusAndCheckoutBefore(BookingMaster.Status status, LocalDateTime now);
}