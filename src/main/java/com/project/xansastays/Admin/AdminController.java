package com.project.xansastays.Admin;
import com.project.xansastays.BookingMaster.BookingMaster;
import com.project.xansastays.BookingMaster.BookingRepository;
import com.project.xansastays.CouponMaster.CouponMaster;
import com.project.xansastays.CouponMaster.CouponRepository;
import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.ImageMaster.ImageMaster;
import com.project.xansastays.ImageMaster.ImageRepository;
import com.project.xansastays.ReviewMaster.ReviewMaster;
import com.project.xansastays.ReviewMaster.ReviewRepository;
import com.project.xansastays.FavouriteMaster.FavouriteRepository;
import com.project.xansastays.RoomMaster.RoomMaster;
import com.project.xansastays.RoomMaster.RoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RoomRepository    roomRepository;
    private final ImageRepository   imageRepository;
    private final BookingRepository bookingRepository;
    private final com.project.xansastays.GuestMaster.GuestRepository guestRepository;
    private final com.project.xansastays.FacilitiesMaster.FacilitiesRepository facilitiesRepository;
    private final com.project.xansastays.amenities.AmenitiesRepository amenitiesRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final FavouriteRepository favouriteRepository;

    public AdminController(RoomRepository roomRepository, ImageRepository imageRepository, BookingRepository bookingRepository,
                           com.project.xansastays.GuestMaster.GuestRepository guestRepository,
                           com.project.xansastays.FacilitiesMaster.FacilitiesRepository facilitiesRepository,
                           com.project.xansastays.amenities.AmenitiesRepository amenitiesRepository, PasswordEncoder passwordEncoder, ReviewRepository reviewRepository, CouponRepository couponRepository, FavouriteRepository favouriteRepository) {
        this.roomRepository    = roomRepository;
        this.imageRepository   = imageRepository;
        this.bookingRepository = bookingRepository;
        this.guestRepository   = guestRepository;
        this.facilitiesRepository = facilitiesRepository;
        this.amenitiesRepository = amenitiesRepository;
        this.passwordEncoder   = passwordEncoder;
        this.reviewRepository  = reviewRepository;
        this.couponRepository  = couponRepository;
        this.favouriteRepository = favouriteRepository;
    }

    // ══════════════════════════════════════════
    // Dashboard Page
    // ══════════════════════════════════════════
    @GetMapping({"", "/", "/dashboard"})
    public String adminPage(Model model) {
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("room", new RoomMaster());
        model.addAttribute("activePage", "dashboard");
        return "admin/admindashboard";
    }

    // ══════════════════════════════════════════
    // Stats API
    // ══════════════════════════════════════════
    @GetMapping("/api/dashboard")
    @ResponseBody
    public Map<String, Object> dashboardStats() {
        List<BookingMaster> allBookings = bookingRepository.findAll();

        double totalRevenue = allBookings.stream()
                .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0)
                .sum();

        String today = java.time.LocalDate.now().toString(); // "yyyy-MM-dd"

        List<BookingMaster> todaysBookingsList = allBookings.stream()
                .filter(b -> b.getCheckin() != null && b.getCheckin().toString().startsWith(today))
                .toList();

        double todaysRevenue = todaysBookingsList.stream()
                .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0)
                .sum();

        Map<String, Object> map = new HashMap<>();
        map.put("totalRooms",      roomRepository.count());
        map.put("availableRooms",  roomRepository.countByStatus(RoomMaster.Status.AVAILABLE));
        map.put("bookedRooms",     roomRepository.countByStatus(RoomMaster.Status.BOOKED));
        map.put("totalBookings",   allBookings.size());
        map.put("todaysBookings",  todaysBookingsList.size());
        map.put("todaysRevenue",   todaysRevenue);
        map.put("totalGuests",     guestRepository.count());
        map.put("totalRevenue",    totalRevenue);
        return map;
    }

    // ══════════════════════════════════════════
    // Add Room Page — naya theme wala form
    // ══════════════════════════════════════════
    @GetMapping("/rooms/addroom")
    public String addRoomPage() {
        return "admin/addrooms";
    }

    // ══════════════════════════════════════════
    // Rooms API — GET ALL
    // ══════════════════════════════════════════
    @GetMapping("/rooms/api")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getRoomsApi() {
        List<RoomMaster> rooms = roomRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (RoomMaster room : rooms) {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId",      room.getRoomId());
            map.put("roomNumber",  room.getRoomNumber());
            map.put("name",        room.getName());
            map.put("pricePerDay", room.getPricePerDay());
            map.put("description", room.getDescription());
            map.put("maxGuests",   room.getMaxGuests());
            map.put("areaSqm",     room.getAreaSqm());
            map.put("bedType",     room.getBedType());
            map.put("viewType",    room.getViewType());
            map.put("status",      room.getStatus()   != null ? room.getStatus().name()   : "AVAILABLE");
            map.put("roomType",    room.getRoomType() != null ? room.getRoomType().name() : "SINGLE");

            List<Map<String, Object>> imgList = new ArrayList<>();
            if (room.getImages() != null) {
                for (ImageMaster img : room.getImages()) {
                    Map<String, Object> imgMap = new HashMap<>();
                    imgMap.put("imageId",  img.getImageId());
                    imgMap.put("fileName", img.getFileName());
                    imgList.add(imgMap);
                }
            }
            map.put("images", imgList);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // ══════════════════════════════════════════
    // Bookings API
    // ══════════════════════════════════════════
    @GetMapping("/api/bookings")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getBookings() {
        List<BookingMaster> bookings = bookingRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (BookingMaster b : bookings) {
            Map<String, Object> map = new HashMap<>();
            map.put("bookingId",       b.getBookingId());
            map.put("checkInDate",     b.getCheckin()  != null ? b.getCheckin().toString()  : null);
            map.put("checkOutDate",    b.getCheckout() != null ? b.getCheckout().toString() : null);
            map.put("guests",          b.getGuests());
            map.put("totalAmount",     b.getTotalAmount());
            map.put("status",          b.getStatus() != null ? b.getStatus().name() : "PENDING");
            map.put("specialRequests", b.getSpecialRequests());
            map.put("createdAt",       b.getCreatedAt() != null ? b.getCreatedAt().toString() : null);

            if (b.getRoom() != null) {
                Map<String, Object> roomMap = new HashMap<>();
                roomMap.put("roomId",     b.getRoom().getRoomId());
                roomMap.put("name",       b.getRoom().getName());
                roomMap.put("roomNumber", b.getRoom().getRoomNumber());
                roomMap.put("roomType",   b.getRoom().getRoomType() != null ? b.getRoom().getRoomType().name() : "-");
                map.put("room", roomMap);
            }

            if (b.getGuest() != null) {
                Map<String, Object> guestMap = new HashMap<>();
                guestMap.put("name",  b.getGuest().getName());
                guestMap.put("email", b.getGuest().getEmail());
                guestMap.put("phone", b.getGuest().getContact() != null ? b.getGuest().getContact() : "-");
                map.put("guest", guestMap);
            }

            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // ══════════════════════════════════════════
    // Recent Checkouts API
    // ══════════════════════════════════════════
    @GetMapping("/api/checkouts")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getRecentCheckouts() {
        List<BookingMaster> bookings = bookingRepository.findAll();
        bookings.sort((a, b) -> {
            if (a.getCheckout() == null && b.getCheckout() == null) return 0;
            if (a.getCheckout() == null) return 1;
            if (b.getCheckout() == null) return -1;
            return b.getCheckout().compareTo(a.getCheckout());
        });

        List<Map<String, Object>> result = new ArrayList<>();
        for (BookingMaster b : bookings) {
            Map<String, Object> map = new HashMap<>();
            map.put("bookingId",       b.getBookingId());
            map.put("guestName",       b.getGuest() != null ? b.getGuest().getName() : "-");
            map.put("guestContact",    b.getGuest() != null && b.getGuest().getContact() != null ? b.getGuest().getContact() : "-");
            map.put("guestProfilePic", b.getGuest() != null && b.getGuest().getProfilePic() != null ? b.getGuest().getProfilePic() : "");
            map.put("roomName",        b.getRoom() != null ? b.getRoom().getName() : "-");
            map.put("roomNumber",      b.getRoom() != null ? b.getRoom().getRoomNumber() : "-");
            map.put("checkInDate",     b.getCheckin()  != null ? b.getCheckin().toString()  : null);
            map.put("checkOutDate",    b.getCheckout() != null ? b.getCheckout().toString() : null);
            map.put("totalAmount",     b.getTotalAmount());
            map.put("paymentStatus",   b.getPaymentStatus() != null ? b.getPaymentStatus().name() : "PENDING");
            map.put("status",          b.getStatus() != null ? b.getStatus().name() : "PENDING");
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // ══════════════════════════════════════════
    // Checkouts Page
    // ══════════════════════════════════════════
    @GetMapping("/checkouts")
    public String checkoutsPage(Model model) {
        List<BookingMaster> bookings = bookingRepository.findAll();

        bookings.sort((a, b) -> {
            if (a.getCheckout() == null && b.getCheckout() == null) return 0;
            if (a.getCheckout() == null) return 1;
            if (b.getCheckout() == null) return -1;
            return b.getCheckout().compareTo(a.getCheckout());
        });

        long totalBookings = bookings.size();
        long paidCount = bookings.stream()
                .filter(b -> b.getPaymentStatus() != null && b.getPaymentStatus().name().equals("PAID"))
                .count();
        long pendingCount = totalBookings - paidCount;

        long paidPct    = totalBookings == 0 ? 0 : Math.round(paidCount    * 100.0 / totalBookings);
        long pendingPct = totalBookings == 0 ? 0 : Math.round(pendingCount * 100.0 / totalBookings);

        model.addAttribute("bookings",      bookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("paidCount",     paidCount);
        model.addAttribute("pendingCount",  pendingCount);
        model.addAttribute("paidPct",       paidPct);
        model.addAttribute("pendingPct",    pendingPct);
        return "admin/checkouts";
    }

    // ══════════════════════════════════════════
    // Guests Page
    // ══════════════════════════════════════════
    @GetMapping("/guests")
    public String guestsPage(Model model) {
        List<GuestMaster> guests = guestRepository.findAll();

        long totalGuests    = guests.size();
        long activeGuests   = guests.stream().filter(g -> !Boolean.FALSE.equals(g.getActive())).count();
        long inactiveGuests = totalGuests - activeGuests;

        long activePct   = totalGuests == 0 ? 0 : Math.round(activeGuests   * 100.0 / totalGuests);
        long inactivePct = totalGuests == 0 ? 0 : Math.round(inactiveGuests * 100.0 / totalGuests);

        Map<Object, String> joinDates = new HashMap<>();
        for (BookingMaster b : bookingRepository.findAll()) {
            if (b.getGuest() == null || b.getCheckin() == null) continue;
            Object gid = b.getGuest().getGuestId();
            String checkin = b.getCheckin().toString();
            if (!joinDates.containsKey(gid) || checkin.compareTo(joinDates.get(gid)) < 0) {
                joinDates.put(gid, checkin);
            }
        }

        model.addAttribute("guests",         guests);
        model.addAttribute("totalGuests",    totalGuests);
        model.addAttribute("activeGuests",   activeGuests);
        model.addAttribute("inactiveGuests", inactiveGuests);
        model.addAttribute("activePct",      activePct);
        model.addAttribute("inactivePct",    inactivePct);
        model.addAttribute("joinDates",      joinDates);
        return "admin/guests";
    }

    // ══════════════════════════════════════════
    // Guests API
    // ══════════════════════════════════════════
    @GetMapping("/api/guests")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getGuests() {
        List<GuestMaster> guests = guestRepository.findAll();

        Map<Object, String> joinDates = new HashMap<>();
        for (BookingMaster b : bookingRepository.findAll()) {
            if (b.getGuest() == null || b.getCheckin() == null) continue;
            Object gid = b.getGuest().getGuestId();
            String checkin = b.getCheckin().toString();
            if (!joinDates.containsKey(gid) || checkin.compareTo(joinDates.get(gid)) < 0) {
                joinDates.put(gid, checkin);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (GuestMaster g : guests) {
            Map<String, Object> map = new HashMap<>();
            map.put("guestId",    g.getGuestId());
            map.put("name",       g.getName());
            map.put("email",      g.getEmail());
            map.put("profilePic", g.getProfilePic() != null ? g.getProfilePic() : "");
            map.put("phone",   g.getContact() != null ? g.getContact() : "-");
            map.put("city",    g.getCity() != null ? g.getCity() : "-");
            map.put("status",  g.getActive());
            map.put("joined",  joinDates.getOrDefault(g.getGuestId(), "-"));
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // ══════════════════════════════════════════
    // Get Single Room — room + uski facility, edit form ke liye
    // ══════════════════════════════════════════
    @GetMapping("/rooms/{id}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        RoomMaster room = roomRepository.findById(id).orElse(null);
        if (room == null) return ResponseEntity.notFound().build();

        Map<String, Object> map = new HashMap<>();
        map.put("roomId",      room.getRoomId());
        map.put("roomNumber",  room.getRoomNumber());
        map.put("name",        room.getName());
        map.put("pricePerDay", room.getPricePerDay());
        map.put("description", room.getDescription());
        map.put("maxGuests",   room.getMaxGuests());
        map.put("areaSqm",     room.getAreaSqm());
        map.put("bedType",     room.getBedType());
        map.put("viewType",    room.getViewType());
        map.put("facilities",  room.getFacilities());
        map.put("status",      room.getStatus()   != null ? room.getStatus().name()   : "AVAILABLE");
        map.put("roomType",    room.getRoomType() != null ? room.getRoomType().name() : "SINGLE");

        // Is room se judi facility dhoondo (agar hai to)
        com.project.xansastays.FacilitiesMaster.FacilitiesMaster facility = facilitiesRepository.findAll()
                .stream()
                .filter(f -> id.equals(f.getRoomId()))
                .findFirst()
                .orElse(null);

        if (facility != null) {
            Map<String, Object> facMap = new HashMap<>();
            facMap.put("noOfBeds",          facility.getNoOfBeds());
            facMap.put("spa",               facility.getSpa());
            facMap.put("lift",              facility.getLift());
            facMap.put("emergencyService",  facility.getEmergencyService());
            facMap.put("roomService",       facility.getRoomService());
            facMap.put("gameZone",          facility.getGameZone());
            facMap.put("swimmingPool",      facility.getSwimmingPool());
            facMap.put("wifi",              facility.getWifi());
            facMap.put("parking",           facility.getParking());
            facMap.put("attachedBathroom",  facility.getAttachedBathroom());
            facMap.put("laundry",           facility.getLaundry());
            facMap.put("garden",            facility.getGarden());
            facMap.put("gym",               facility.getGym());
            facMap.put("dining",            facility.getDining()   != null ? facility.getDining().name()   : "");
            facMap.put("facilityRoomType",  facility.getRoomType() != null ? facility.getRoomType().name() : "");
            map.put("facility", facMap);
        }

        return ResponseEntity.ok(map);
    }

    // ══════════════════════════════════════════
    // Save Room — FIXED (images ab sahi save hongi)
    // ══════════════════════════════════════════
    @PostMapping("/rooms/save")
    @ResponseBody
    public ResponseEntity<?> saveRoom(
            @RequestParam("roomNumber")  String roomNumber,
            @RequestParam("name")        String name,
            @RequestParam("pricePerDay") Double pricePerDay,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "maxGuests",   required = false) Integer maxGuests,
            @RequestParam(value = "areaSqm",     required = false) Double areaSqm,
            @RequestParam(value = "bedType",     required = false) String bedType,
            @RequestParam(value = "viewType",    required = false) String viewType,
            @RequestParam(value = "facilities",  required = false) String facilities,
            @RequestParam(value = "status",      required = false, defaultValue = "AVAILABLE") String status,
            @RequestParam(value = "roomType",    required = false, defaultValue = "SINGLE")    String roomType,
            @RequestParam(value = "images",      required = false) List<MultipartFile> images,
            // ── Facility fields ──
            @RequestParam(value = "noOfBeds",         required = false, defaultValue = "1") Integer noOfBeds,
            @RequestParam(value = "spa",               required = false, defaultValue = "No") String spa,
            @RequestParam(value = "lift",              required = false, defaultValue = "No") String lift,
            @RequestParam(value = "emergencyService",  required = false, defaultValue = "No") String emergencyService,
            @RequestParam(value = "roomService",       required = false, defaultValue = "No") String roomService,
            @RequestParam(value = "gameZone",          required = false, defaultValue = "No") String gameZone,
            @RequestParam(value = "swimmingPool",      required = false, defaultValue = "No") String swimmingPool,
            @RequestParam(value = "wifi",              required = false, defaultValue = "No") String wifi,
            @RequestParam(value = "parking",           required = false, defaultValue = "No") String parking,
            @RequestParam(value = "attachedBathroom",  required = false, defaultValue = "No") String attachedBathroom,
            @RequestParam(value = "laundry",           required = false, defaultValue = "No") String laundry,
            @RequestParam(value = "garden",            required = false, defaultValue = "No") String garden,
            @RequestParam(value = "gym",               required = false, defaultValue = "No") String gym,
            @RequestParam(value = "dining",             required = false) String dining,
            @RequestParam(value = "facilityRoomType",   required = false) String facilityRoomType
    ) throws IOException {

        RoomMaster room = new RoomMaster();
        room.setRoomNumber(roomNumber);
        room.setName(name);
        room.setPricePerDay(pricePerDay);
        room.setDescription(description);
        room.setMaxGuests(maxGuests);
        room.setAreaSqm(areaSqm);
        room.setBedType(bedType);
        room.setViewType(viewType);
        room.setFacilities(facilities);

        try { room.setStatus(RoomMaster.Status.valueOf(status.toUpperCase())); }
        catch (Exception e) { room.setStatus(RoomMaster.Status.AVAILABLE); }

        try { room.setRoomType(RoomMaster.RoomType.valueOf(roomType.toUpperCase())); }
        catch (Exception e) { room.setRoomType(RoomMaster.RoomType.SINGLE); }

        RoomMaster savedRoom = roomRepository.save(room);

        // Facilities save karo — room se linked
        com.project.xansastays.FacilitiesMaster.FacilitiesMaster facility =
                new com.project.xansastays.FacilitiesMaster.FacilitiesMaster();
        facility.setRoomId(savedRoom.getRoomId());
        facility.setNoOfBeds(noOfBeds != null ? noOfBeds : 1);
        facility.setSpa(spa);
        facility.setLift(lift);
        facility.setEmergencyService(emergencyService);
        facility.setRoomService(roomService);
        facility.setGameZone(gameZone);
        facility.setSwimmingPool(swimmingPool);
        facility.setWifi(wifi);
        facility.setParking(parking);
        facility.setAttachedBathroom(attachedBathroom);
        facility.setLaundry(laundry);
        facility.setGarden(garden);
        facility.setGym(gym);

        if (dining != null) {
            try { facility.setDining(com.project.xansastays.FacilitiesMaster.FacilitiesMaster.Dining.valueOf(dining.toUpperCase())); }
            catch (Exception e) { /* dining optional, ignore invalid value */ }
        }
        if (facilityRoomType != null) {
            try { facility.setRoomType(com.project.xansastays.FacilitiesMaster.FacilitiesMaster.RoomType.valueOf(facilityRoomType.toUpperCase())); }
            catch (Exception e) { /* room type optional, ignore invalid value */ }
        }

        facilitiesRepository.save(facility);

        // Images save karo
        if (images != null) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    ImageMaster img = new ImageMaster();
                    img.setRoom(savedRoom);
                    img.setRoomImg(file.getBytes());
                    img.setContentType(file.getContentType());
                    img.setFileName(file.getOriginalFilename());
                    imageRepository.save(img);
                }
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "roomId", savedRoom.getRoomId()));
    }

    // ══════════════════════════════════════════
    // Update Room
    // ══════════════════════════════════════════
    @PostMapping("/rooms/update")
    @ResponseBody
    public ResponseEntity<?> updateRoom(
            @RequestParam("roomId")      Long roomId,
            @RequestParam("roomNumber")  String roomNumber,
            @RequestParam("name")        String name,
            @RequestParam("pricePerDay") Double pricePerDay,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "maxGuests",   required = false) Integer maxGuests,
            @RequestParam(value = "areaSqm",     required = false) Double areaSqm,
            @RequestParam(value = "bedType",     required = false) String bedType,
            @RequestParam(value = "viewType",    required = false) String viewType,
            @RequestParam(value = "facilities",  required = false) String facilities,
            @RequestParam(value = "status",      required = false) String status,
            @RequestParam(value = "roomType",    required = false) String roomType,
            @RequestParam(value = "images",      required = false) List<MultipartFile> images,
            // ── Facility fields (add-room form jaisa hi) ──
            @RequestParam(value = "noOfBeds",         required = false, defaultValue = "1") Integer noOfBeds,
            @RequestParam(value = "spa",               required = false, defaultValue = "No") String spa,
            @RequestParam(value = "lift",              required = false, defaultValue = "No") String lift,
            @RequestParam(value = "emergencyService",  required = false, defaultValue = "No") String emergencyService,
            @RequestParam(value = "roomService",       required = false, defaultValue = "No") String roomService,
            @RequestParam(value = "gameZone",          required = false, defaultValue = "No") String gameZone,
            @RequestParam(value = "swimmingPool",      required = false, defaultValue = "No") String swimmingPool,
            @RequestParam(value = "wifi",              required = false, defaultValue = "No") String wifi,
            @RequestParam(value = "parking",           required = false, defaultValue = "No") String parking,
            @RequestParam(value = "attachedBathroom",  required = false, defaultValue = "No") String attachedBathroom,
            @RequestParam(value = "laundry",           required = false, defaultValue = "No") String laundry,
            @RequestParam(value = "garden",            required = false, defaultValue = "No") String garden,
            @RequestParam(value = "gym",               required = false, defaultValue = "No") String gym,
            @RequestParam(value = "dining",             required = false) String dining,
            @RequestParam(value = "facilityRoomType",   required = false) String facilityRoomType
    ) throws IOException {

        RoomMaster existing = roomRepository.findById(roomId).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();

        existing.setRoomNumber(roomNumber);
        existing.setName(name);
        existing.setPricePerDay(pricePerDay);
        existing.setDescription(description);
        existing.setMaxGuests(maxGuests);
        existing.setAreaSqm(areaSqm);
        existing.setBedType(bedType);
        existing.setViewType(viewType);
        existing.setFacilities(facilities);

        if (status != null) {
            try { existing.setStatus(RoomMaster.Status.valueOf(status.toUpperCase())); }
            catch (Exception e) { existing.setStatus(RoomMaster.Status.AVAILABLE); }
        }
        if (roomType != null) {
            try { existing.setRoomType(RoomMaster.RoomType.valueOf(roomType.toUpperCase())); }
            catch (Exception e) { existing.setRoomType(RoomMaster.RoomType.SINGLE); }
        }

        RoomMaster updated = roomRepository.save(existing);

        com.project.xansastays.FacilitiesMaster.FacilitiesMaster facility = facilitiesRepository.findAll()
                .stream()
                .filter(f -> roomId.equals(f.getRoomId()))
                .findFirst()
                .orElseGet(com.project.xansastays.FacilitiesMaster.FacilitiesMaster::new);

        facility.setRoomId(roomId);
        facility.setNoOfBeds(noOfBeds != null ? noOfBeds : 1);
        facility.setSpa(spa);
        facility.setLift(lift);
        facility.setEmergencyService(emergencyService);
        facility.setRoomService(roomService);
        facility.setGameZone(gameZone);
        facility.setSwimmingPool(swimmingPool);
        facility.setWifi(wifi);
        facility.setParking(parking);
        facility.setAttachedBathroom(attachedBathroom);
        facility.setLaundry(laundry);
        facility.setGarden(garden);
        facility.setGym(gym);

        if (dining != null && !dining.isBlank()) {
            try { facility.setDining(com.project.xansastays.FacilitiesMaster.FacilitiesMaster.Dining.valueOf(dining.toUpperCase())); }
            catch (Exception e) { /* dining optional, ignore invalid value */ }
        }
        if (facilityRoomType != null && !facilityRoomType.isBlank()) {
            try { facility.setRoomType(com.project.xansastays.FacilitiesMaster.FacilitiesMaster.RoomType.valueOf(facilityRoomType.toUpperCase())); }
            catch (Exception e) { /* room type optional, ignore invalid value */ }
        }

        facilitiesRepository.save(facility);

        // Nayi images add karo
        if (images != null) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    ImageMaster img = new ImageMaster();
                    img.setRoom(updated);
                    img.setRoomImg(file.getBytes());
                    img.setContentType(file.getContentType());
                    img.setFileName(file.getOriginalFilename());
                    imageRepository.save(img);
                }
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    // ══════════════════════════════════════════
    // Delete Room
    // ══════════════════════════════════════════
    @PostMapping("/rooms/delete/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        if (!roomRepository.existsById(id)) return ResponseEntity.notFound().build();
        RoomMaster room = roomRepository.findById(id).orElse(null);
        if (room == null) return ResponseEntity.notFound().build();
        favouriteRepository.deleteByRoomId(id);

        roomRepository.delete(room);
        return ResponseEntity.ok("Room Deleted Successfully");
    }

    // ══════════════════════════════════════════
    // Get Room Image
    // ══════════════════════════════════════════
    @GetMapping("/rooms/image/{imageId}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        return imageRepository.findById(imageId)
                .map(img -> ResponseEntity.ok()
                        .header("Content-Type", img.getContentType())
                        .body(img.getRoomImg()))
                .orElse(ResponseEntity.notFound().build());
    }

    // ══════════════════════════════════════════
    // Delete Image
    // ══════════════════════════════════════════
    @PostMapping("/rooms/image/delete/{imageId}")
    @ResponseBody
    public ResponseEntity<String> deleteImage(@PathVariable Long imageId) {
        if (!imageRepository.existsById(imageId)) return ResponseEntity.notFound().build();
        imageRepository.deleteById(imageId);
        return ResponseEntity.ok("Image Deleted Successfully");
    }

    // ══════════════════════════════════════════
    // Reviews API — GET ALL (admin ke liye)
    // ══════════════════════════════════════════
    @GetMapping("/api/reviews")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getReviewsApi() {
        List<ReviewMaster> reviews = reviewRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ReviewMaster r : reviews) {
            Map<String, Object> map = new HashMap<>();
            map.put("reviewId", r.getReviewId());
            map.put("rating",   r.getRating());
            map.put("comment",  r.getReview());
            map.put("guestName", r.getGuest() != null ? r.getGuest().getName() : "Guest");
            map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // ══════════════════════════════════════════
    // Delete Review — admin kisi bhi guest ki review delete kar sakta hai
    // ══════════════════════════════════════════
    @PostMapping("/reviews/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        if (!reviewRepository.existsById(id)) return ResponseEntity.notFound().build();
        reviewRepository.deleteById(id);
        return ResponseEntity.ok("Review Deleted Successfully");
    }

    // ══════════════════════════════════════════
    // Admin Profile — GET current admin details
    // ══════════════════════════════════════════
    @GetMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication, HttpSession session) {
        GuestMaster admin = getCurrentAdmin(authentication, session);
        if (admin == null) return ResponseEntity.status(401).build();

        Map<String, Object> map = new HashMap<>();
        map.put("name",    admin.getName());
        map.put("email",   admin.getEmail());
        map.put("contact", admin.getContact() != null ? admin.getContact() : "");
        map.put("profilePic", admin.getProfilePic() != null ? admin.getProfilePic() : "");
        return ResponseEntity.ok(map);
    }

    // ══════════════════════════════════════════
    // Admin Profile — UPDATE (name, email, contact, password)
    // ══════════════════════════════════════════
    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> payload,
            Authentication authentication,
            HttpSession session
    ) {
        Map<String, Object> res = new HashMap<>();

        GuestMaster admin = getCurrentAdmin(authentication, session);
        if (admin == null) {
            res.put("success", false);
            res.put("message", "Session expire ho gaya, dubara login karo.");
            return ResponseEntity.status(401).body(res);
        }

        String name     = payload.get("name");
        String email    = payload.get("email");
        String contact  = payload.get("contact");
        String password = payload.get("password");

        if (name != null && !name.trim().isEmpty())   admin.setName(name.trim());
        if (email != null && !email.trim().isEmpty()) admin.setEmail(email.trim());
        if (contact != null)                          admin.setContact(contact.trim());

        // Password sirf tab update hoga jab admin ne kuch type kiya ho
        if (password != null && !password.trim().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(password.trim()));
        }

        guestRepository.save(admin);

        session.setAttribute("loggedGuest", admin);

        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    // Helper — Current logged-in Admin fetch

    private GuestMaster getCurrentAdmin(Authentication authentication, HttpSession session) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return guestRepository.findByEmail(authentication.getName()).orElse(null);
        }
        GuestMaster sessionGuest = (GuestMaster) session.getAttribute("loggedGuest");
        if (sessionGuest != null) {
            return guestRepository.findById(sessionGuest.getGuestId()).orElse(null);
        }
        return null;
    }

    // Coupons API — GET ALL (with usage stats)

    @GetMapping("/api/coupons")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getCouponsApi() {
        List<CouponMaster> coupons     = couponRepository.findAll();
        List<BookingMaster> allBookings = bookingRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (CouponMaster c : coupons) {
            Map<String, Object> map = new HashMap<>();
            map.put("couponId",      c.getCouponId());
            map.put("code",          c.getCode());
            map.put("discountType",  c.getDiscountType().name());
            map.put("discountValue", c.getDiscountValue());
            map.put("minRoomPrice",  c.getMinRoomPrice());
            map.put("maxRoomPrice",  c.getMaxRoomPrice());
            map.put("isActive",      c.getIsActive());
            map.put("expiryDate",    c.getExpiryDate() != null ? c.getExpiryDate().toString() : null);

            long usageCount = allBookings.stream()
                    .filter(b -> c.getCode().equalsIgnoreCase(b.getCouponCode()))
                    .count();
            double totalDiscount = allBookings.stream()
                    .filter(b -> c.getCode().equalsIgnoreCase(b.getCouponCode()))
                    .mapToDouble(b -> b.getDiscountAmount() != null ? b.getDiscountAmount() : 0)
                    .sum();

            map.put("usageCount",   usageCount);
            map.put("totalDiscount", totalDiscount);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // ══════════════════════════════════════════
    // Coupon — CREATE
    // ══════════════════════════════════════════
    @PostMapping("/coupons/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCoupon(@RequestBody Map<String, String> payload) {
        Map<String, Object> res = new HashMap<>();

        String code = payload.get("code");
        if (code == null || code.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Coupon code required!");
            return ResponseEntity.badRequest().body(res);
        }
        code = code.trim().toUpperCase();

        if (couponRepository.existsByCodeIgnoreCase(code)) {
            res.put("success", false);
            res.put("message", "This coupon code already exis!");
            return ResponseEntity.badRequest().body(res);
        }

        CouponMaster.DiscountType discountType;
        try {
            discountType = CouponMaster.DiscountType.valueOf(payload.get("discountType").toUpperCase());
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Invalid discount type!");
            return ResponseEntity.badRequest().body(res);
        }

        CouponMaster coupon = new CouponMaster();
        coupon.setCode(code);
        coupon.setDiscountType(discountType);

        try {
            coupon.setDiscountValue(Double.parseDouble(payload.get("discountValue")));
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "The discount value must be a valid number.!");
            return ResponseEntity.badRequest().body(res);
        }

        String minPrice = payload.get("minRoomPrice");
        String maxPrice = payload.get("maxRoomPrice");
        String expiry   = payload.get("expiryDate");

        if (minPrice != null && !minPrice.isBlank()) coupon.setMinRoomPrice(Double.parseDouble(minPrice));
        if (maxPrice != null && !maxPrice.isBlank()) coupon.setMaxRoomPrice(Double.parseDouble(maxPrice));
        if (expiry   != null && !expiry.isBlank())   coupon.setExpiryDate(java.time.LocalDate.parse(expiry));

        coupon.setIsActive(true);
        couponRepository.save(coupon);

        res.put("success", true);
        res.put("message", "Coupon ready!");
        return ResponseEntity.ok(res);
    }

    // ══════════════════════════════════════════
    // Coupon — TOGGLE enable/disable
    // ══════════════════════════════════════════
    @PostMapping("/coupons/toggle/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleCoupon(@PathVariable Long id) {
        Map<String, Object> res = new HashMap<>();
        Optional<CouponMaster> opt = couponRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        CouponMaster coupon = opt.get();
        coupon.setIsActive(!Boolean.TRUE.equals(coupon.getIsActive()));
        couponRepository.save(coupon);

        res.put("success", true);
        res.put("isActive", coupon.getIsActive());
        return ResponseEntity.ok(res);
    }

    // ══════════════════════════════════════════
    // Coupon — DELETE
    // ══════════════════════════════════════════
    @PostMapping("/coupons/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteCoupon(@PathVariable Long id) {
        if (!couponRepository.existsById(id)) return ResponseEntity.notFound().build();
        couponRepository.deleteById(id);
        return ResponseEntity.ok("Coupon Deleted Successfully");
    }

    // ══════════════════════════════════════════
    // Facility / Amenity — Add Page
    // ══════════════════════════════════════════
    @GetMapping("/facilities/addpage")
    public String addFacilityPage() {
        return "admin/addfacility";
    }

    // Room dropdown ke liye halka data (id + number + name)
    @GetMapping("/rooms/simple")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getRoomsSimple() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (RoomMaster room : roomRepository.findAll()) {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId",     room.getRoomId());
            map.put("roomNumber", room.getRoomNumber());
            map.put("name",       room.getName());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // ── Facility — Save (existing room ke liye add/update) ──
    @PostMapping("/facilities/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveFacilityStandalone(
            @RequestParam("roomId") Long roomId,
            @RequestParam(value = "noOfBeds",         required = false, defaultValue = "1") Integer noOfBeds,
            @RequestParam(value = "spa",               required = false, defaultValue = "No") String spa,
            @RequestParam(value = "lift",              required = false, defaultValue = "No") String lift,
            @RequestParam(value = "emergencyService",  required = false, defaultValue = "No") String emergencyService,
            @RequestParam(value = "roomService",       required = false, defaultValue = "No") String roomService,
            @RequestParam(value = "gameZone",          required = false, defaultValue = "No") String gameZone,
            @RequestParam(value = "swimmingPool",      required = false, defaultValue = "No") String swimmingPool,
            @RequestParam(value = "wifi",              required = false, defaultValue = "No") String wifi,
            @RequestParam(value = "parking",           required = false, defaultValue = "No") String parking,
            @RequestParam(value = "attachedBathroom",  required = false, defaultValue = "No") String attachedBathroom,
            @RequestParam(value = "laundry",           required = false, defaultValue = "No") String laundry,
            @RequestParam(value = "garden",            required = false, defaultValue = "No") String garden,
            @RequestParam(value = "gym",               required = false, defaultValue = "No") String gym,
            @RequestParam(value = "dining",             required = false) String dining,
            @RequestParam(value = "facilityRoomType",   required = false) String facilityRoomType
    ) {
        Map<String, Object> res = new HashMap<>();
        RoomMaster room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            res.put("success", false);
            res.put("message", "Room Not!");
            return ResponseEntity.badRequest().body(res);
        }

        com.project.xansastays.FacilitiesMaster.FacilitiesMaster facility = facilitiesRepository.findAll()
                .stream()
                .filter(f -> roomId.equals(f.getRoomId()))
                .findFirst()
                .orElseGet(com.project.xansastays.FacilitiesMaster.FacilitiesMaster::new);

        facility.setRoom(room);
        facility.setNoOfBeds(noOfBeds != null ? noOfBeds : 1);
        facility.setSpa(spa);
        facility.setLift(lift);
        facility.setEmergencyService(emergencyService);
        facility.setRoomService(roomService);
        facility.setGameZone(gameZone);
        facility.setSwimmingPool(swimmingPool);
        facility.setWifi(wifi);
        facility.setParking(parking);
        facility.setAttachedBathroom(attachedBathroom);
        facility.setLaundry(laundry);
        facility.setGarden(garden);
        facility.setGym(gym);

        if (dining != null && !dining.isBlank()) {
            try { facility.setDining(com.project.xansastays.FacilitiesMaster.FacilitiesMaster.Dining.valueOf(dining.toUpperCase())); }
            catch (Exception e) { /* ignore invalid */ }
        }
        if (facilityRoomType != null && !facilityRoomType.isBlank()) {
            try { facility.setRoomType(com.project.xansastays.FacilitiesMaster.FacilitiesMaster.RoomType.valueOf(facilityRoomType.toUpperCase())); }
            catch (Exception e) { /* ignore invalid */ }
        }

        facilitiesRepository.save(facility);
        res.put("success", true);
        res.put("message", "Facility save ho gayi!");
        return ResponseEntity.ok(res);
    }

    // ── Amenity — Save (naya extra paid amenity kisi room ke liye) ──
    @PostMapping("/amenities/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveAmenity(
            @RequestParam("roomId")        Long roomId,
            @RequestParam("amenitiesName") String amenitiesName,
            @RequestParam("price")         Double price
    ) {
        Map<String, Object> res = new HashMap<>();
        RoomMaster room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            res.put("success", false);
            res.put("message", "Room Not!");
            return ResponseEntity.badRequest().body(res);
        }

        com.project.xansastays.amenities.AmenitiesMaster amenity = new com.project.xansastays.amenities.AmenitiesMaster();
        amenity.setRoom(room);
        amenity.setAmenitiesName(amenitiesName);
        amenity.setPrice(price);
        amenitiesRepository.save(amenity);

        res.put("success", true);
        res.put("message", "Amenity add ho gayi!");
        return ResponseEntity.ok(res);
    }

    // ── Amenities API — GET ALL (dashboard listing ke liye) ──
    @GetMapping("/api/amenities")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAmenitiesApi() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (com.project.xansastays.amenities.AmenitiesMaster a : amenitiesRepository.findAll()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",             a.getId());
            map.put("amenitiesName",  a.getAmenitiesName());
            map.put("price",          a.getPrice());
            map.put("roomId",         a.getRoom() != null ? a.getRoom().getRoomId()     : null);
            map.put("roomNumber",     a.getRoom() != null ? a.getRoom().getRoomNumber() : "-");
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // ── Amenity — DELETE ──
    @PostMapping("/amenities/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteAmenity(@PathVariable Long id) {
        if (!amenitiesRepository.existsById(id)) return ResponseEntity.notFound().build();
        amenitiesRepository.deleteById(id);
        return ResponseEntity.ok("Amenity Deleted Successfully");
    }
}