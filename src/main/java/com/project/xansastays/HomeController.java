package com.project.xansastays;

import com.project.xansastays.ReviewMaster.ReviewRepository;
import com.project.xansastays.RoomMaster.RoomMaster;
import com.project.xansastays.RoomMaster.RoomRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class HomeController {

    private final com.project.xansastays.GuestMaster.GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;

    public HomeController(com.project.xansastays.GuestMaster.GuestRepository guestRepository,
                          RoomRepository roomRepository,
                          ReviewRepository reviewRepository) {
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
        this.reviewRepository = reviewRepository;
    }

    private void addUserInfo(String principal, Model model) {
        guestRepository.findByEmail(principal).ifPresent(guest -> {
            String name = guest.getName();
            model.addAttribute("guestName", name);
            model.addAttribute("guestEmail", guest.getEmail());
            model.addAttribute("guestInitial",
                    (name != null && !name.isEmpty())
                            ? String.valueOf(name.charAt(0)).toUpperCase() : "G");
            model.addAttribute("guestProfilePic", guest.getProfilePic());
        });
    }

    @GetMapping({"/Homepage"})
    @Transactional(readOnly = true)
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            addUserInfo(authentication.getName(), model);
        }
        List<RoomMaster> rooms = roomRepository.findAll();
        rooms.forEach(room -> {
            if (room.getImages() != null) room.getImages().size();
        });
        model.addAttribute("rooms", rooms);
        model.addAttribute("latestReviews",
                reviewRepository.findTop6ByHotel_HotelIdOrderByReviewIdDesc(1L));
        model.addAttribute("activePage", "home");

        // NAYA — avg rating + total reviews badge ke liye
        Double avgRating = reviewRepository.findAverageRatingByHotelId(1L);
        model.addAttribute("avgRating",
                avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        model.addAttribute("totalReviews",
                reviewRepository.findByHotel_HotelId(1L).size());

        return "home";
    }

    @GetMapping("/room/{id}")
    @Transactional(readOnly = true)
    public String roomDetail(@PathVariable Long id, Model model,
                             Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            addUserInfo(authentication.getName(), model);
        }
        model.addAttribute("activePage", "rooms");
        roomRepository.findById(id).ifPresent(room -> {
            if (room.getImages() != null) room.getImages().size();
            model.addAttribute("room", room);


            Long hotelId = (room.getHotel() != null) ? room.getHotel().getHotelId() : 1L;
            Double avgRating = reviewRepository.findAverageRatingByHotelId(hotelId);
            model.addAttribute("avgRating",
                    avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        });
        return "Rooms/room-detail";
    }

    @GetMapping("/Dashboard")
    public String dashboard() {
        return "Dashboard";
    }

    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("activePage", "gallery");  // ← ADD
        return "Gallery/Gallery";
    }

    @GetMapping("/contact")
    @Transactional(readOnly = true)
    public String contact(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            addUserInfo(authentication.getName(), model);
        }
        model.addAttribute("activePage", "contact");  // ← ADD

        // NAYA — real average rating DB se
        Double avgRating = reviewRepository.findAverageRatingByHotelId(1L);
        model.addAttribute("avgRating",
                avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

        return "contact";
    }


    @PostMapping("/contact/send")
    public String sendMessage(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("success",
                "Thank you " + name + "! Your message has been received. We will get back to you soon.");
        return "redirect:/contact";
    }
}