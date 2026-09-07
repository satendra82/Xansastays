package com.project.xansastays.CheckoutMaster;

import com.project.xansastays.BookingMaster.BookingMaster;
import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.RoomserviceMaster.RoomserviceMaster;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name="checkout_master")
public class CheckoutMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkoutId;
    private Date checkoutDate;

    public enum Status {
        PAID,
        PENDING
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Payment {
        CASH,
        PENDING
    }

    @Enumerated(EnumType.STRING)
    private Payment payment;


    public CheckoutMaster() {
    }

    public CheckoutMaster(Long checkoutId, Date checkoutDate, Status status, Payment payment) {
        this.checkoutId = checkoutId;
        this.checkoutDate = checkoutDate;
        this.status = status;
        this.payment = payment;
    }

    public Long getCheckoutId() {
        return checkoutId;
    }

    public void setCheckoutId(Long checkoutId) {
        this.checkoutId = checkoutId;
    }

    public Date getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(Date checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
