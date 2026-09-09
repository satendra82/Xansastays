package com.project.xansastays.CheckoutMaster;

import com.project.xansastays.BookingMaster.BookingMaster;
import com.project.xansastays.BookingMaster.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CheckoutController {

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/{bookingId:[0-9]+}")  // ✅ FIXED - sirf numbers match karega
    public String checkout(@PathVariable Long bookingId, Model model) {

        BookingMaster booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking Not Found"));

        model.addAttribute("booking", booking);
        model.addAttribute("guestName", booking.getGuest().getName());

        return "Rooms/checkout";
    }

}