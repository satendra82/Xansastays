package com.project.xansastays.BookingMaster;

import com.project.xansastays.CouponMaster.CouponMaster;
import com.project.xansastays.CouponMaster.CouponRepository;
import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.GuestMaster.GuestRepository;
import com.project.xansastays.RoomMaster.RoomMaster;
import com.project.xansastays.RoomMaster.RoomRepository;
import com.razorpay.RazorpayClient;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class BookingController {

    @Autowired private RoomRepository    roomRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private GuestRepository   guestRepository;
    @Autowired private CouponRepository  couponRepository;
    @Autowired private RazorpayClient    razorpayClient;

    // 24h cancellation window (booking create hone ke time se)
    private static final long CANCELLATION_WINDOW_HOURS = 24;

    // ─────────────────────────────────────────
    // GET /rooms/{roomId} — Room Detail Page
    // ─────────────────────────────────────────
    @GetMapping("/rooms/{roomId}")
    public String roomDetailPage(
            @PathVariable Long roomId,
            Authentication authentication,
            HttpSession session,
            Model model
    ) {
        Optional<RoomMaster> opt = roomRepository.findById(roomId);
        if (opt.isEmpty()) return "redirect:/rooms/viewrooms";

        model.addAttribute("room", opt.get());

        GuestMaster guest = getGuest(authentication, session);
        if (guest != null) {
            model.addAttribute("guestEmail",  guest.getEmail());
            model.addAttribute("guestInitial",
                    guest.getName() != null && !guest.getName().isEmpty()
                            ? String.valueOf(guest.getName().charAt(0)).toUpperCase() : "U");
        } else {
            model.addAttribute("guestEmail",  "");
            model.addAttribute("guestInitial","U");
        }

        return "Rooms/room-detail.html";
    }

    // ─────────────────────────────────────────
    // GET /booking?roomId=x — Booking Form
    // ─────────────────────────────────────────
    @GetMapping("/booking")
    public String bookingPage(
            @RequestParam("roomId") Long roomId,
            Authentication authentication,
            HttpSession session,
            Model model
    ) {
        GuestMaster guest = getGuest(authentication, session);

        if (guest == null) {
            return "redirect:/login?next=/booking?roomId=" + roomId;
        }

        Optional<RoomMaster> opt = roomRepository.findById(roomId);
        if (opt.isEmpty()) return "redirect:/rooms";

        RoomMaster room = opt.get();

        if (room.getStatus() == RoomMaster.Status.BOOKED) {
            return "redirect:/rooms";
        }

        model.addAttribute("room",        room);
        model.addAttribute("guestName",   guest.getName());
        model.addAttribute("guestEmail",  guest.getEmail());
        model.addAttribute("guestPhone",  guest.getContact() != null ? guest.getContact() : "");
        model.addAttribute("guestInitial",
                guest.getName() != null && !guest.getName().isEmpty()
                        ? String.valueOf(guest.getName().charAt(0)).toUpperCase() : "U");

        return "Rooms/booking";
    }

    // ─────────────────────────────────────────
    // POST /booking — Submit
    // ─────────────────────────────────────────
    @PostMapping("/booking")
    public String submitBooking(
            @RequestParam("roomId")       Long   roomId,
            @RequestParam("checkInDate")  String checkInDate,
            @RequestParam("checkInTime")  String checkInTime,
            @RequestParam("checkOutDate") String checkOutDate,
            @RequestParam("guests")       Integer guests,
            @RequestParam(value = "specialRequests", required = false) String specialRequests,
            @RequestParam("totalAmount")  Double totalAmount,
            @RequestParam(value = "couponCode", required = false) String couponCode,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttr
    ) {
        try {
            GuestMaster guest = getGuest(authentication, session);
            if (guest == null) return "redirect:/login";

            Optional<RoomMaster> roomOpt = roomRepository.findById(roomId);
            if (roomOpt.isEmpty()) {
                redirectAttr.addFlashAttribute("error", "Room not found!");
                return "redirect:/rooms";
            }
            RoomMaster room = roomOpt.get();

            LocalDate ciDate = LocalDate.parse(checkInDate);
            LocalDate coDate = LocalDate.parse(checkOutDate);
            int noOfDays = (int) Math.max(1, ChronoUnit.DAYS.between(ciDate, coDate));

            LocalTime ciTime = LocalTime.parse(checkInTime);
            LocalDateTime checkin = LocalDateTime.of(ciDate, ciTime);

            LocalDateTime checkout = checkin.plusDays(noOfDays);

            double discountAmount = 0.0;
            String appliedCoupon  = null;

            if (couponCode != null && !couponCode.trim().isEmpty()) {
                Optional<CouponMaster> couponOpt =
                        couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(couponCode.trim());

                if (couponOpt.isPresent()) {
                    CouponMaster coupon = couponOpt.get();

                    boolean notExpired = coupon.getExpiryDate() == null
                            || !coupon.getExpiryDate().isBefore(LocalDate.now());

                    double roomPrice = room.getPricePerDay();
                    double minPrice  = coupon.getMinRoomPrice() != null ? coupon.getMinRoomPrice() : 0;
                    double maxPrice  = coupon.getMaxRoomPrice() != null ? coupon.getMaxRoomPrice() : Double.MAX_VALUE;
                    boolean priceOk  = roomPrice >= minPrice && roomPrice <= maxPrice;

                    if (notExpired && priceOk) {
                        if (coupon.getDiscountType() == CouponMaster.DiscountType.PERCENTAGE) {
                            discountAmount = Math.round(totalAmount * (coupon.getDiscountValue() / 100.0) * 100.0) / 100.0;
                        } else {
                            discountAmount = coupon.getDiscountValue();
                        }
                        if (discountAmount > totalAmount) discountAmount = totalAmount;
                        appliedCoupon = coupon.getCode().toUpperCase();
                    }
                }
            }

            double originalAmount = totalAmount;
            double finalAmount    = Math.round((totalAmount - discountAmount) * 100.0) / 100.0;

            double advance = Math.round(finalAmount * 0.30 * 100.0) / 100.0;
            double balance = Math.round((finalAmount - advance) * 100.0) / 100.0;

            BookingMaster booking = new BookingMaster();
            booking.setRoom(room);
            booking.setGuest(guest);
            booking.setBookingDate(new Date());
            booking.setCheckin(checkin);
            booking.setCheckout(checkout);
            booking.setNoOfDays(noOfDays);
            booking.setGuests(guests);
            booking.setOriginalAmount(originalAmount);
            booking.setDiscountAmount(discountAmount);
            booking.setCouponCode(appliedCoupon);
            booking.setTotalAmount(finalAmount);
            booking.setAdvanceAmount(advance);
            booking.setBalanceAmount(balance);
            booking.setSpecialRequests(specialRequests);
            booking.setStatus(BookingMaster.Status.PENDING);


            BookingMaster saved = bookingRepository.save(booking);
            return "redirect:/booking/confirm/" + saved.getBookingId();

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttr.addFlashAttribute("error", "Booking failed: " + e.getMessage());
            return "redirect:/booking?roomId=" + roomId;
        }
    }


    @PostMapping("/booking/pay-advance/{id}")
    public String payAdvance(
            @PathVariable Long id,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttr
    ) {
        GuestMaster guest = getGuest(authentication, session);
        if (guest == null) return "redirect:/login";

        Optional<BookingMaster> opt = bookingRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/rooms";

        BookingMaster booking = opt.get();

        if (!booking.getGuest().getGuestId().equals(guest.getGuestId())) {
            return "redirect:/rooms";
        }

        if (booking.getStatus() == BookingMaster.Status.CONFIRMED) {
            return "redirect:/booking/confirm/" + id;
        }

        booking.setStatus(BookingMaster.Status.CONFIRMED);
        bookingRepository.save(booking);

        RoomMaster room = booking.getRoom();
        room.setStatus(RoomMaster.Status.BOOKED);
        roomRepository.save(room);

        redirectAttr.addFlashAttribute("success", "Advance payment successful! Your booking is confirmed.");
        return "redirect:/booking/confirm/" + id;
    }

    // ─────────────────────────────────────────
    // GET /checkout/{id} — Guest Checkout Page
    // ─────────────────────────────────────────
    @GetMapping("/checkout/{id}")
    public String checkoutPage(
            @PathVariable Long id,
            Authentication authentication,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttr
    ) {
        GuestMaster guest = getGuest(authentication, session);
        if (guest == null) return "redirect:/login";

        Optional<BookingMaster> opt = bookingRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/mybookings";

        BookingMaster booking = opt.get();

        if (!booking.getGuest().getGuestId().equals(guest.getGuestId())) {
            return "redirect:/mybookings";
        }

        if (booking.getStatus() == BookingMaster.Status.CANCELLED) {
            redirectAttr.addFlashAttribute("error",
                    "This booking has been cancelled. Checkout is not possible.");
            return "redirect:/mybookings";
        }

        if (booking.getStatus() == BookingMaster.Status.COMPLETED) {
            redirectAttr.addFlashAttribute("info",
                    "Checkout for this booking has already been completed.");
            return "redirect:/mybookings";
        }

        model.addAttribute("booking",     booking);
        model.addAttribute("guestName",   guest.getName());
        model.addAttribute("guestEmail",  guest.getEmail());
        model.addAttribute("guestInitial",
                guest.getName() != null && !guest.getName().isEmpty()
                        ? String.valueOf(guest.getName().charAt(0)).toUpperCase() : "U");

        return "Rooms/checkout";
    }

    // ─────────────────────────────────────────
    // GET /booking/confirm/{id}
    // ─────────────────────────────────────────
    @GetMapping("/booking/confirm/{id}")
    public String confirmPage(
            @PathVariable Long id,
            Authentication authentication,
            HttpSession session,
            Model model
    ) {
        GuestMaster guest = getGuest(authentication, session);
        if (guest == null) return "redirect:/login";

        Optional<BookingMaster> opt = bookingRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/rooms";

        BookingMaster booking = opt.get();

        if (!booking.getGuest().getGuestId().equals(guest.getGuestId())) {
            return "redirect:/rooms";
        }

        long nights = 1;
        if (booking.getCheckin() != null && booking.getCheckout() != null) {
            nights = Math.max(1,
                    ChronoUnit.HOURS.between(booking.getCheckin(), booking.getCheckout()) / 24);
        }

        model.addAttribute("booking",     booking);
        model.addAttribute("nights",      nights);
        model.addAttribute("guestInitial",
                guest.getName() != null && !guest.getName().isEmpty()
                        ? String.valueOf(guest.getName().charAt(0)).toUpperCase() : "U");
        model.addAttribute("guestEmail",  guest.getEmail());

        return "Rooms/booking-confirm";
    }

    // ─────────────────────────────────────────
    // GET /mybookings
    // ─────────────────────────────────────────
    @GetMapping("/mybookings")
    public String myBookings(
            Authentication authentication,
            HttpSession session,
            Model model
    ) {
        GuestMaster guest = getGuest(authentication, session);
        if (guest == null) return "redirect:/login";

        var bookings = bookingRepository.findByGuestOrderByCreatedAtDesc(guest);

        model.addAttribute("bookings",    bookings);
        model.addAttribute("guestName",   guest.getName());
        model.addAttribute("guestEmail",  guest.getEmail());
        model.addAttribute("guestInitial",
                guest.getName() != null && !guest.getName().isEmpty()
                        ? String.valueOf(guest.getName().charAt(0)).toUpperCase() : "U");

        return "Rooms/Mybookings";
    }

    // ─────────────────────────────────────────
    // POST /booking/cancel/{id} — Guest cancels within 24h of booking creation
    // ─────────────────────────────────────────
    @PostMapping("/booking/cancel/{id}")
    public String cancelBooking(
            @PathVariable Long id,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttr
    ) {
        GuestMaster guest = getGuest(authentication, session);
        if (guest == null) return "redirect:/login";

        Optional<BookingMaster> opt = bookingRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/mybookings";

        BookingMaster booking = opt.get();

        // Security check — ye booking isi guest ki honi chahiye
        if (!booking.getGuest().getGuestId().equals(guest.getGuestId())) {
            return "redirect:/mybookings";
        }

        // Already cancelled/completed booking dobara cancel nahi ho sakti
        if (booking.getStatus() == BookingMaster.Status.CANCELLED
                || booking.getStatus() == BookingMaster.Status.COMPLETED) {
            redirectAttr.addFlashAttribute("error", "This booking cannot be cancelled.");
            return "redirect:/mybookings";
        }

        // ── 24h window check — booking CREATE hone ke time se ──
        LocalDateTime createdAt = booking.getCreatedAt();
        if (createdAt == null
                || ChronoUnit.HOURS.between(createdAt, LocalDateTime.now()) >= CANCELLATION_WINDOW_HOURS) {
            redirectAttr.addFlashAttribute("error",
                    "Cancellation window (24 hours) has expired. This booking can no longer be cancelled.");
            return "redirect:/mybookings";
        }

        // ── Refund process karo — advance aur balance dono ko UNKE APNE payment ID se refund karo ──
        // (Ek payment ID sirf utna hi amount refund kar sakti hai jitna usme actually capture hua tha)
        double totalRefunded = 0;
        StringBuilder refundErrors = new StringBuilder();

        boolean advancePaid = booking.getPaymentStatus() == BookingMaster.PaymentStatus.ADVANCE_PAID
                || booking.getPaymentStatus() == BookingMaster.PaymentStatus.PAID;
        boolean balancePaid = booking.getPaymentStatus() == BookingMaster.PaymentStatus.PAID;

        if (advancePaid && booking.getRazorpayAdvancePaymentId() != null
                && booking.getAdvanceAmount() != null && booking.getAdvanceAmount() > 0) {
            try {
                int amountPaise = (int) Math.round(booking.getAdvanceAmount() * 100);
                JSONObject refundRequest = new JSONObject();
                refundRequest.put("amount", amountPaise);
                razorpayClient.payments.refund(booking.getRazorpayAdvancePaymentId(), refundRequest);
                totalRefunded += booking.getAdvanceAmount();
            } catch (Exception e) {
                e.printStackTrace();
                refundErrors.append("Advance refund failed: ").append(e.getMessage()).append(" ");
            }
        }

        if (balancePaid && booking.getRazorpayBalancePaymentId() != null
                && booking.getBalanceAmount() != null && booking.getBalanceAmount() > 0) {
            try {
                int amountPaise = (int) Math.round(booking.getBalanceAmount() * 100);
                JSONObject refundRequest = new JSONObject();
                refundRequest.put("amount", amountPaise);
                razorpayClient.payments.refund(booking.getRazorpayBalancePaymentId(), refundRequest);
                totalRefunded += booking.getBalanceAmount();
            } catch (Exception e) {
                e.printStackTrace();
                refundErrors.append("Balance refund failed: ").append(e.getMessage()).append(" ");
            }
        }

        if (totalRefunded > 0) {
            booking.setPaymentStatus(BookingMaster.PaymentStatus.REFUNDED);
        }

        booking.setStatus(BookingMaster.Status.CANCELLED);
        bookingRepository.save(booking);

        // ── Room ko wapas AVAILABLE karo ──
        RoomMaster room = booking.getRoom();
        if (room.getStatus() == RoomMaster.Status.BOOKED) {
            room.setStatus(RoomMaster.Status.AVAILABLE);
            roomRepository.save(room);
        }

        String msg = "Booking has been cancelled";
        if (totalRefunded > 0) {
            msg += " and a refund of ₹" + totalRefunded + " has been initiated. It will reflect in your account within 2-3 working days.";
        } else {
            msg += ".";
        }
        if (refundErrors.length() > 0) {
            msg += " Note: Some refunds could not be processed (" + refundErrors + "). Please contact support.";
        }

        redirectAttr.addFlashAttribute("success", msg);
        return "redirect:/mybookings";
    }

    // ─────────────────────────────────────────
    // POST /checkout/complete/{id} — Guest Checkout Complete
    // ─────────────────────────────────────────
    @PostMapping("/checkout/complete/{id}")
    public String completeCheckout(
            @PathVariable Long id,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttr
    ) {
        GuestMaster guest = getGuest(authentication, session);
        if (guest == null) return "redirect:/login";

        Optional<BookingMaster> opt = bookingRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/mybookings";

        BookingMaster booking = opt.get();

        // Security check
        if (!booking.getGuest().getGuestId().equals(guest.getGuestId())) {
            return "redirect:/mybookings";
        }

        // Cancelled booking ka checkout nahi ho sakta
        if (booking.getStatus() == BookingMaster.Status.CANCELLED) {
            redirectAttr.addFlashAttribute("error", "This booking has already been cancelled.");
            return "redirect:/mybookings";
        }

        // Pehle se completed hai
        if (booking.getStatus() == BookingMaster.Status.COMPLETED) {
            redirectAttr.addFlashAttribute("info", "Checkout for this booking has already been completed.");
            return "redirect:/mybookings";
        }

        // Booking COMPLETED mark karo
        booking.setStatus(BookingMaster.Status.COMPLETED);
        bookingRepository.save(booking);

        // Room wapas AVAILABLE karo
        RoomMaster room = booking.getRoom();
        if (room != null && room.getStatus() == RoomMaster.Status.BOOKED) {
            room.setStatus(RoomMaster.Status.AVAILABLE);
            roomRepository.save(room);
        }

        redirectAttr.addFlashAttribute("success",
                "Checkout completed successfully! We hope you enjoyed your stay at Xansa.");
        return "redirect:/mybookings";
    }

    // ─────────────────────────────────────────
    // AUTO-CHECKOUT — Har 5 minute me chalega
    // CONFIRMED booking jinka checkout time nikal chuka hai
    // unko COMPLETED mark karo aur room ko AVAILABLE karo
    // ─────────────────────────────────────────
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes
    public void autoCheckoutExpiredBookings() {
        List<BookingMaster> dueBookings =
                bookingRepository.findByStatusAndCheckoutBefore(
                        BookingMaster.Status.CONFIRMED, LocalDateTime.now());

        for (BookingMaster booking : dueBookings) {
            booking.setStatus(BookingMaster.Status.COMPLETED);
            bookingRepository.save(booking);

            RoomMaster room = booking.getRoom();
            if (room.getStatus() == RoomMaster.Status.BOOKED) {
                room.setStatus(RoomMaster.Status.AVAILABLE);
                roomRepository.save(room);
            }
        }
    }

    // ─────────────────────────────────────────
    // Helper — Session ya Spring Security dono se Users fetch
    // ─────────────────────────────────────────
    private GuestMaster getGuest(Authentication authentication, HttpSession session) {
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
}