package com.project.xansastays.ReviewMaster;

import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.GuestMaster.GuestRepository;
import com.project.xansastays.HotelMaster.HotelMaster;
import com.project.xansastays.HotelMaster.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    GuestRepository guestRepository;

    // Hotel ke reviews dikhao — koi bhi dekh sakta hai, login zaroori nahi
    @GetMapping("/hotel/{hotelId}")
    public String viewReviews(@PathVariable Long hotelId, Model model, Authentication authentication) {
        Optional<HotelMaster> hotel = hotelRepository.findById(hotelId);
        if (hotel.isEmpty()) {
            return "redirect:/home";
        }

        List<ReviewMaster> reviews = reviewRepository.findByHotel_HotelId(hotelId);
        Double avgRating = reviewRepository.findAverageRatingByHotelId(hotelId);

        model.addAttribute("hotel", hotel.get());
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        model.addAttribute("totalReviews", reviews.size());

        boolean alreadyReviewed = false;
        ReviewMaster myReview = null;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String loggedInEmail = authentication.getName();
            Optional<GuestMaster> guest = guestRepository.findByEmail(loggedInEmail);

            if (guest.isPresent()) {
                alreadyReviewed = reviewRepository.existsByGuest_GuestIdAndHotel_HotelId(
                        guest.get().getGuestId(), hotelId);

                if (alreadyReviewed) {
                    myReview = reviewRepository
                            .findByGuest_GuestIdAndHotel_HotelId(guest.get().getGuestId(), hotelId)
                            .orElse(null);
                }
            }
        }

        model.addAttribute("alreadyReviewed", alreadyReviewed);
        model.addAttribute("myReview", myReview); // ← NAYA

        return "Reviews/viewReviews";
    }

    // Review submit karo
    @PostMapping("/submit")
    public String submitReview(
            @RequestParam Long hotelId,
            @RequestParam int rating,
            @RequestParam String reviewText,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String loggedInEmail = authentication.getName();

        Optional<GuestMaster> guest = guestRepository.findByEmail(loggedInEmail);
        Optional<HotelMaster> hotel = hotelRepository.findById(hotelId);

        if (guest.isEmpty() || hotel.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid request.");
            return "redirect:/reviews/hotel/" + hotelId;
        }

        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Rating must be between 1 and 5.");
            return "redirect:/reviews/hotel/" + hotelId;
        }

        boolean alreadyReviewed = reviewRepository.existsByGuest_GuestIdAndHotel_HotelId(
                guest.get().getGuestId(), hotelId);
        if (alreadyReviewed) {
            redirectAttributes.addFlashAttribute("error", "Aap is hotel ko pehle hi review de chuke ho.");
            return "redirect:/reviews/hotel/" + hotelId;
        }

        ReviewMaster newReview = new ReviewMaster();
        newReview.setRating(rating);
        newReview.setReview(reviewText);
        newReview.setGuest(guest.get());
        newReview.setHotel(hotel.get());

        reviewRepository.save(newReview);

        redirectAttributes.addFlashAttribute("success", "Thank you! Your review has been submitted.");
        return "redirect:/home";
    }

    // ── Review Update (NAYA) ──────────────────────────────────
    @PostMapping("/update")
    public String updateReview(
            @RequestParam Long reviewId,
            @RequestParam Long hotelId,
            @RequestParam int rating,
            @RequestParam String reviewText,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<ReviewMaster> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Review nahi mili.");
            return "redirect:/reviews/hotel/" + hotelId;
        }

        ReviewMaster review = reviewOpt.get();

        // Security check — sirf apni hi review edit kar sake
        String loggedInEmail = authentication.getName();
        Optional<GuestMaster> guest = guestRepository.findByEmail(loggedInEmail);

        if (guest.isEmpty() || !review.getGuest().getGuestId().equals(guest.get().getGuestId())) {
            redirectAttributes.addFlashAttribute("error", "Aap sirf apni review edit kar sakte ho.");
            return "redirect:/reviews/hotel/" + hotelId;
        }

        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Rating must be between 1 and 5.");
            return "redirect:/reviews/hotel/" + hotelId;
        }

        review.setRating(rating);
        review.setReview(reviewText);
        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute("success", "Your review has been updated.");
        return "redirect:/reviews/hotel/" + hotelId;
    }
}