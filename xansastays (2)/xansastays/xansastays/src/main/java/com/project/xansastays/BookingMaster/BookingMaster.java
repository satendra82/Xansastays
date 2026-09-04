package com.project.xansastays.BookingMaster;

import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.HotelMaster.HotelMaster;
import com.project.xansastays.RoomMaster.RoomMaster;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "booking_master")
public class BookingMaster {

    public enum Status { PENDING, CONFIRMED, CANCELLED, COMPLETED }
    public enum PaymentStatus { PENDING, ADVANCE_PAID, PAID, FAILED, REFUNDED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    private Integer noOfDays;
    private Date bookingDate;

    private LocalDateTime checkin;
    private LocalDateTime checkout;

    private Integer guests;
    private Double totalAmount;
    private String specialRequests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    // ── Payment fields ──
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "advance_amount")
    private Double advanceAmount;

    @Column(name = "balance_amount")
    private Double balanceAmount;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId; // legacy — last payment (backward compatibility)

    @Column(name = "razorpay_advance_payment_id")
    private String razorpayAdvancePaymentId;

    @Column(name = "razorpay_balance_payment_id")
    private String razorpayBalancePaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // ── Coupon fields (NAYE) ──
    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "original_amount")
    private Double originalAmount; // discount se pehle ka amount

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Relations ──
    @ManyToOne
    @JoinColumn(name = "user_id")
    private GuestMaster guest;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private HotelMaster hotel;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private RoomMaster room;

    public BookingMaster() {}

    // ── Getters & Setters ──
    public Long getBookingId()                        { return bookingId; }
    public void setBookingId(Long bookingId)          { this.bookingId = bookingId; }

    public Integer getNoOfDays()                      { return noOfDays; }
    public void setNoOfDays(Integer noOfDays)         { this.noOfDays = noOfDays; }

    public Date getBookingDate()                      { return bookingDate; }
    public void setBookingDate(Date bookingDate)      { this.bookingDate = bookingDate; }

    public LocalDateTime getCheckin()                  { return checkin; }
    public void setCheckin(LocalDateTime checkin)      { this.checkin = checkin; }

    public LocalDateTime getCheckout()                 { return checkout; }
    public void setCheckout(LocalDateTime checkout)    { this.checkout = checkout; }

    public Integer getGuests()                        { return guests; }
    public void setGuests(Integer guests)             { this.guests = guests; }

    public Double getTotalAmount()                    { return totalAmount; }
    public void setTotalAmount(Double totalAmount)    { this.totalAmount = totalAmount; }

    public String getSpecialRequests()                { return specialRequests; }
    public void setSpecialRequests(String s)          { this.specialRequests = s; }

    public Status getStatus()                         { return status; }
    public void setStatus(Status status)              { this.status = status; }

    public String getRazorpayOrderId()                 { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public Double getAdvanceAmount()                   { return advanceAmount; }
    public void setAdvanceAmount(Double advanceAmount) { this.advanceAmount = advanceAmount; }

    public Double getBalanceAmount()                   { return balanceAmount; }
    public void setBalanceAmount(Double balanceAmount) { this.balanceAmount = balanceAmount; }

    public String getRazorpayPaymentId()               { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpayAdvancePaymentId()                { return razorpayAdvancePaymentId; }
    public void setRazorpayAdvancePaymentId(String id)         { this.razorpayAdvancePaymentId = id; }

    public String getRazorpayBalancePaymentId()                { return razorpayBalancePaymentId; }
    public void setRazorpayBalancePaymentId(String id)         { this.razorpayBalancePaymentId = id; }

    public PaymentStatus getPaymentStatus()            { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getCouponCode()                      { return couponCode; }
    public void setCouponCode(String couponCode)       { this.couponCode = couponCode; }

    public Double getDiscountAmount()                   { return discountAmount; }
    public void setDiscountAmount(Double discountAmount){ this.discountAmount = discountAmount; }

    public Double getOriginalAmount()                   { return originalAmount; }
    public void setOriginalAmount(Double originalAmount){ this.originalAmount = originalAmount; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public GuestMaster getGuest()                     { return guest; }
    public void setGuest(GuestMaster guest)           { this.guest = guest; }

    public HotelMaster getHotel()                     { return hotel; }
    public void setHotel(HotelMaster hotel)           { this.hotel = hotel; }

    public RoomMaster getRoom()                       { return room; }
    public void setRoom(RoomMaster room)              { this.room = room; }
}