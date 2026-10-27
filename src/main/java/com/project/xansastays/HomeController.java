package com.project.xansastays;

import com.project.xansastays.ImageMaster.ImageRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final com.project.xansastays.GuestMaster.GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;

    public HomeController(com.project.xansastays.GuestMaster.GuestRepository guestRepository,
                          RoomRepository roomRepository,
                          ReviewRepository reviewRepository,
                          ImageRepository imageRepository) {
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
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

    // Rooms ke saath sirf image id/naam jodta hai (bytes nahi) -> room.imageList
    private void attachImageInfo(List<RoomMaster> rooms) {
        List<Long> ids = rooms.stream().map(RoomMaster::getRoomId).toList();
        if (ids.isEmpty()) return;
        Map<Long, List<ImageRepository.RoomImageInfo>> byRoom =
                imageRepository.findInfoByRoomIds(ids).stream()
                        .collect(Collectors.groupingBy(ImageRepository.RoomImageInfo::getRoomId));
        rooms.forEach(r -> r.setImageList(byRoom.getOrDefault(r.getRoomId(), List.of())));
    }

    @GetMapping({"/", "/Homepage"})
    @Transactional(readOnly = true)
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            addUserInfo(authentication.getName(), model);
        }

        List<RoomMaster> rooms = roomRepository.findAll();
        attachImageInfo(rooms);
        model.addAttribute("rooms", rooms);

        model.addAttribute("latestReviews",
                reviewRepository.findTop6ByHotel_HotelIdOrderByReviewIdDesc(1L));
        model.addAttribute("activePage", "home");

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
            attachImageInfo(List.of(room));
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
        model.addAttribute("activePage", "gallery");
        return "Gallery/Gallery";
    }

    @GetMapping("/contact")
    @Transactional(readOnly = true)
    public String contact(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            addUserInfo(authentication.getName(), model);
        }
        model.addAttribute("activePage", "contact");

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