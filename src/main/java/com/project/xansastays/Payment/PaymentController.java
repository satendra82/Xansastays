package com.project.xansastays.Payment;

import com.project.xansastays.BookingMaster.BookingMaster;
import com.project.xansastays.BookingMaster.BookingRepository;
import com.project.xansastays.RoomMaster.RoomMaster;
import com.project.xansastays.RoomMaster.RoomRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired private RazorpayClient    razorpayClient;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private RoomRepository    roomRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private static final double ADVANCE_PERCENT = 0.30;
    private static final double BALANCE_PERCENT = 0.70;

    // Step 1: Create Razorpay Order
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            Long bookingId = Long.valueOf(body.get("bookingId").toString());
            String paymentType = body.get("paymentType") != null
                    ? body.get("paymentType").toString().toUpperCase()
                    : "ADVANCE";

            Optional<BookingMaster> opt = bookingRepository.findById(bookingId);
            if (opt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not"));
            }
            BookingMaster booking = opt.get();

            double amountToCharge;

            if (paymentType.equals("ADVANCE")) {
                if (booking.getPaymentStatus() != BookingMaster.PaymentStatus.PENDING) {
                    return ResponseEntity.badRequest().body(Map.of("error", "The advance payment has been made, or the booking is not pending."));
                }
                amountToCharge = Math.round(booking.getTotalAmount() * ADVANCE_PERCENT);
            } else if (paymentType.equals("BALANCE")) {
                if (booking.getPaymentStatus() != BookingMaster.PaymentStatus.ADVANCE_PAID) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Complete the advance payment first."));
                }
                amountToCharge = Math.round(booking.getTotalAmount() * BALANCE_PERCENT);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid paymentType"));
            }

            int amountPaise = (int) Math.round(amountToCharge * 100);

            JSONObject options = new JSONObject();
            options.put("amount", amountPaise);
            options.put("currency", "INR");
            options.put("receipt", "booking_rcpt_" + bookingId + "_" + paymentType);
            options.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(options);

            booking.setRazorpayOrderId(order.get("id").toString());
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of(
                    "orderId", order.get("id").toString(),
                    "amount", order.get("amount").toString(),
                    "currency", order.get("currency").toString(),
                    "keyId", keyId,
                    "bookingId", bookingId,
                    "paymentType", paymentType
            ));
        } catch (RazorpayException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal error: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> body) {
        try {
            String orderId    = body.get("razorpay_order_id");
            String paymentId  = body.get("razorpay_payment_id");
            String signature  = body.get("razorpay_signature");
            Long   bookingId  = Long.valueOf(body.get("bookingId"));
            // "ADVANCE" or "BALANCE" — default to "ADVANCE" for backward compatibility
            String paymentType = body.get("paymentType") != null
                    ? body.get("paymentType").toUpperCase()
                    : "ADVANCE";

            JSONObject attrs = new JSONObject();
            attrs.put("razorpay_order_id", orderId);
            attrs.put("razorpay_payment_id", paymentId);
            attrs.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(attrs, keySecret);

            Optional<BookingMaster> opt = bookingRepository.findById(bookingId);
            if (opt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not"));
            }
            BookingMaster booking = opt.get();

            if (isValid) {
                booking.setRazorpayPaymentId(paymentId);

                if (paymentType.equals("ADVANCE")) {
                    booking.setRazorpayAdvancePaymentId(paymentId);

                    booking.setPaymentStatus(BookingMaster.PaymentStatus.ADVANCE_PAID);
                    booking.setStatus(BookingMaster.Status.CONFIRMED);
                    bookingRepository.save(booking);

                    RoomMaster room = booking.getRoom();
                    room.setStatus(RoomMaster.Status.BOOKED);
                    roomRepository.save(room);
                } else { // BALANCE
                    booking.setRazorpayBalancePaymentId(paymentId);

                    booking.setPaymentStatus(BookingMaster.PaymentStatus.PAID);
                    bookingRepository.save(booking);
                }

                return ResponseEntity.ok(Map.of("verified", true, "paymentId", paymentId, "paymentType", paymentType));
            } else {
                booking.setPaymentStatus(BookingMaster.PaymentStatus.FAILED);
                bookingRepository.save(booking);
                return ResponseEntity.status(400).body(Map.of("verified", false, "error", "Signature mismatch"));
            }
        } catch (RazorpayException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal error: " + e.getMessage()));
        }
    }
}