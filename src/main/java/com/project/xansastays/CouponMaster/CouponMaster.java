package com.project.xansastays.CouponMaster;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_master")
public class CouponMaster {

    public enum DiscountType { PERCENTAGE, FLAT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    @Column(name = "min_room_price")
    private Double minRoomPrice = 0.0;

    @Column(name = "max_room_price")
    private Double maxRoomPrice;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CouponMaster() {}

    // ── Getters & Setters ──
    public Long getCouponId()                          { return couponId; }
    public void setCouponId(Long couponId)             { this.couponId = couponId; }

    public String getCode()                            { return code; }
    public void setCode(String code)                   { this.code = code; }

    public DiscountType getDiscountType()               { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public Double getDiscountValue()                    { return discountValue; }
    public void setDiscountValue(Double discountValue)  { this.discountValue = discountValue; }

    public Double getMinRoomPrice()                     { return minRoomPrice; }
    public void setMinRoomPrice(Double minRoomPrice)    { this.minRoomPrice = minRoomPrice; }

    public Double getMaxRoomPrice()                     { return maxRoomPrice; }
    public void setMaxRoomPrice(Double maxRoomPrice)    { this.maxRoomPrice = maxRoomPrice; }

    public Boolean getIsActive()                        { return isActive; }
    public void setIsActive(Boolean isActive)           { this.isActive = isActive; }

    public LocalDate getExpiryDate()                    { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate)     { this.expiryDate = expiryDate; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
}