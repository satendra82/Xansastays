package com.project.xansastays.CouponMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<CouponMaster, Long> {

    Optional<CouponMaster> findByCodeIgnoreCaseAndIsActiveTrue(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("SELECT c FROM CouponMaster c WHERE c.isActive = true " +
            "AND c.minRoomPrice <= :price " +
            "AND (c.maxRoomPrice IS NULL OR c.maxRoomPrice >= :price) " +
            "AND (c.expiryDate IS NULL OR c.expiryDate >= CURRENT_DATE) " +
            "ORDER BY c.discountValue DESC")
    List<CouponMaster> findValidCouponsForPrice(@Param("price") Double price);
}