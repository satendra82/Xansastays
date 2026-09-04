package com.project.xansastays.Config;

import com.project.xansastays.FavouriteMaster.FavouriteMaster;
import com.project.xansastays.FavouriteMaster.FavouriteRepository;
import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.GuestMaster.GuestRepository;
import com.project.xansastays.RoomMaster.RoomMaster;
import com.project.xansastays.RoomMaster.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping("/profile")
    public String showProfile(Model model) {
        GuestMaster guest = getLoggedInGuest();
        if (guest == null) return "redirect:/login";

        model.addAttribute("guest", guest);

        List<RoomMaster> favoriteRooms = favouriteRepository.findByGuestOrderByFavouriteIdDesc(guest)
                .stream()
                .map(FavouriteMaster::getRoom)
                .collect(Collectors.toList());
        model.addAttribute("favoriteRooms", favoriteRooms);

        return "profile";
    }

    @GetMapping("/favourites")
    public String favouritesPage(Model model) {
        GuestMaster guest = getLoggedInGuest();
        if (guest == null) return "redirect:/login";

        List<RoomMaster> favoriteRooms = favouriteRepository.findByGuestOrderByFavouriteIdDesc(guest)
                .stream()
                .map(FavouriteMaster::getRoom)
                .collect(Collectors.toList());
        model.addAttribute("favoriteRooms", favoriteRooms);

        return "favourites";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("name") String name,
            @RequestParam(value = "contact", required = false) String contact,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "profilePicFile", required = false) MultipartFile pic,
            RedirectAttributes redirectAttributes) {

        GuestMaster guest = getLoggedInGuest();
        if (guest == null) return "redirect:/login";

        guest.setName(name.trim());
        if (contact != null) guest.setContact(contact.trim());
        if (city != null) guest.setCity(city.trim());
        if (state != null) guest.setState(state.trim());
        if (country != null) guest.setCountry(country.trim());
        if (address != null) guest.setAddress(address.trim());

        if (pic != null && !pic.isEmpty()) {
            String error = saveProfilePicture(guest, pic);
            if (error != null) {
                redirectAttributes.addFlashAttribute("profileError", error);
                return "redirect:/profile";
            }
        }

        guestRepository.save(guest);
        redirectAttributes.addFlashAttribute("profileUpdated", true);
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete-account")
    public String deleteAccount() {
        GuestMaster guest = getLoggedInGuest();
        if (guest != null) {
            guest.setActive(false);
            guestRepository.save(guest);
        }
        return "redirect:/logout";
    }

    // ── FAVOURITES ─────────────────────────────────────────────

    @PostMapping("/api/favourites/toggle/{roomId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavourite(@PathVariable Long roomId) {

        GuestMaster guest = getLoggedInGuest();
        if (guest == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Not logged in"));
        }

        Optional<RoomMaster> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Room not found"));
        }

        RoomMaster room = roomOpt.get();
        Optional<FavouriteMaster> existing = favouriteRepository.findByGuestAndRoom(guest, room);

        boolean nowFavourited;
        if (existing.isPresent()) {
            favouriteRepository.delete(existing.get());
            nowFavourited = false;
        } else {
            favouriteRepository.save(new FavouriteMaster(null, guest, room));
            nowFavourited = true;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("favourited", nowFavourited);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/favourites/check/{roomId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkFavourite(@PathVariable Long roomId) {

        GuestMaster guest = getLoggedInGuest();
        Map<String, Object> body = new HashMap<>();

        if (guest == null) {
            body.put("favourited", false);
            return ResponseEntity.ok(body);
        }

        Optional<RoomMaster> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            body.put("favourited", false);
            return ResponseEntity.ok(body);
        }

        boolean favourited = favouriteRepository.existsByGuestAndRoom(guest, roomOpt.get());
        body.put("favourited", favourited);
        return ResponseEntity.ok(body);
    }

    // ── SHARED HELPERS ────────────────────────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleFileTooLarge(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("profileError", "Image is too large. Maximum size is 5 MB.");
        return "redirect:/profile";
    }

    /**
     * Looks up the logged-in guest via Spring Security's authenticated principal
     * (email). NULL is_active (older rows) is treated as active — same rule as
     * CustomUserDetailsService.
     */
    private GuestMaster getLoggedInGuest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        Optional<GuestMaster> guestOpt = guestRepository.findByEmail(auth.getName());
        return guestOpt
                .filter(g -> !Boolean.FALSE.equals(g.getActive()))
                .orElse(null);
    }

    private String saveProfilePicture(GuestMaster guest, MultipartFile pic) {
        if (pic.getSize() > 5 * 1024 * 1024) {
            return "Image must be smaller than 5 MB.";
        }

        try {
            Path saveDir = Paths.get(uploadDir);
            Files.createDirectories(saveDir);

            String original = pic.getOriginalFilename();
            String extension = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf(".")).toLowerCase()
                    : ".jpg";

            String filename = UUID.randomUUID() + extension;
            Files.copy(pic.getInputStream(), saveDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            guest.setProfilePic("/uploads/profiles/" + filename);

        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload image. Please try again.";
        }

        return null;
    }
}