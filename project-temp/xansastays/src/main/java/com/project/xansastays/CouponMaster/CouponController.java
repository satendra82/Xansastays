package com.project.xansastays.CouponMaster;

import com.project.xansastays.RoomMaster.RoomMaster;
import com.project.xansastays.RoomMaster.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired private CouponRepository couponRepository;
    @Autowired private RoomRepository    roomRepository;

    // ─────────────────────────────────────────
    // POST /coupon/apply — Booking page se AJAX call
    // ─────────────────────────────────────────
    @PostMapping("/apply")
    public Map<String, Object> applyCoupon(
            @RequestParam("code")        String code,
            @RequestParam("roomId")      Long roomId,
            @RequestParam("totalAmount") Double totalAmount
    ) {
        Map<String, Object> res = new HashMap<>();

        Optional<RoomMaster> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            res.put("success", false);
            res.put("message", "Room nahi mili!");
            return res;
        }
        RoomMaster room = roomOpt.get();

        Optional<CouponMaster> couponOpt = couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(code.trim());
        if (couponOpt.isEmpty()) {
            res.put("success", false);
            res.put("message", "Invalid coupon code!");
            return res;
        }
        CouponMaster coupon = couponOpt.get();

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            res.put("success", false);
            res.put("message", "Ye coupon expire ho chuka hai!");
            return res;
        }

        double roomPrice = room.getPricePerDay();
        double minPrice  = coupon.getMinRoomPrice() != null ? coupon.getMinRoomPrice() : 0;
        double maxPrice  = coupon.getMaxRoomPrice() != null ? coupon.getMaxRoomPrice() : Double.MAX_VALUE;

        if (roomPrice < minPrice || roomPrice > maxPrice) {
            res.put("success", false);
            res.put("message", "Ye coupon is room price range ke liye valid nahi hai!");
            return res;
        }

        double discount;
        if (coupon.getDiscountType() == CouponMaster.DiscountType.PERCENTAGE) {
            discount = Math.round(totalAmount * (coupon.getDiscountValue() / 100.0) * 100.0) / 100.0;
        } else {
            discount = coupon.getDiscountValue();
        }
        if (discount > totalAmount) discount = totalAmount;

        double finalAmount = Math.round((totalAmount - discount) * 100.0) / 100.0;

        String label = coupon.getDiscountType() == CouponMaster.DiscountType.PERCENTAGE
                ? coupon.getDiscountValue().intValue() + "% off applied!"
                : "₹" + coupon.getDiscountValue().intValue() + " off applied!";

        res.put("success", true);
        res.put("message", label);
        res.put("discountAmount", discount);
        res.put("finalAmount", finalAmount);
        res.put("couponCode", coupon.getCode().toUpperCase());
        return res;
    }


    // ─────────────────────────────────────────
// GET /coupon/available?roomId=x — Room ke liye valid coupons list
// ─────────────────────────────────────────
    @GetMapping("/available")
    public List<Map<String, Object>> availableCoupons(@RequestParam("roomId") Long roomId) {
        List<Map<String, Object>> result = new ArrayList<>();

        Optional<RoomMaster> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) return result;

        double roomPrice = roomOpt.get().getPricePerDay();
        List<CouponMaster> coupons = couponRepository.findValidCouponsForPrice(roomPrice);

        for (CouponMaster c : coupons) {
            Map<String, Object> m = new HashMap<>();
            String label = c.getDiscountType() == CouponMaster.DiscountType.PERCENTAGE
                    ? c.getDiscountValue().intValue() + "% off"
                    : "₹" + c.getDiscountValue().intValue() + " off";

            m.put("code", c.getCode().toUpperCase());
            m.put("label", label);
            result.add(m);
        }
        return result;
    }
}