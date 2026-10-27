package com.project.xansastays.RoomMaster;

import com.project.xansastays.GuestMaster.GuestRepository;
import com.project.xansastays.ImageMaster.ImageRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rooms")
public class RoomPageController {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ImageRepository imageRepository;

    public RoomPageController(RoomRepository roomRepository,
                              GuestRepository guestRepository,
                              ImageRepository imageRepository) {
        this.roomRepository  = roomRepository;
        this.guestRepository = guestRepository;
        this.imageRepository = imageRepository;
    }

    // ── Helper — naam aur initial model mein daalo ──
    private void addUserInfo(String principal, Model model) {
        guestRepository.findByEmail(principal).ifPresent(guest -> {
            String name = (guest.getName() != null && !guest.getName().isEmpty())
                    ? guest.getName() : "Guest";
            model.addAttribute("guestName",    name);
            model.addAttribute("guestEmail",   guest.getEmail());
            model.addAttribute("guestInitial",
                    String.valueOf(name.charAt(0)).toUpperCase());
        });
    }

    // ── All Rooms Page ──
    @GetMapping
    @Transactional(readOnly = true)
    public String allRooms(Model model, Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            addUserInfo(authentication.getName(), model);
        }

        List<RoomMaster> rooms = roomRepository.findAll();

        // Sirf image id/naam laao (bytes nahi), aur room.imageList mein daalo
        List<Long> ids = rooms.stream().map(RoomMaster::getRoomId).toList();
        if (!ids.isEmpty()) {
            Map<Long, List<ImageRepository.RoomImageInfo>> byRoom =
                    imageRepository.findInfoByRoomIds(ids).stream()
                            .collect(Collectors.groupingBy(ImageRepository.RoomImageInfo::getRoomId));
            rooms.forEach(r -> r.setImageList(byRoom.getOrDefault(r.getRoomId(), List.of())));
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("activePage", "rooms");

        return "Rooms/Guestrooms";
    }

    // ── Add Room Page ──
    @GetMapping("/addroom")
    public String addRoom() {
        return "Rooms/Viewrooms";
    }
}