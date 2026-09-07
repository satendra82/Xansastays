package com.project.xansastays.RollMaster;

import jakarta.persistence.*;

@Entity
@Table(name="roll_master")
public class RollMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rollId;
    private String rollName;
    private String admin;
    private String guest;

    public RollMaster() {
    }

    public RollMaster(Long rollId, String rollName, String admin, String guest) {
        this.rollId = rollId;
        this.rollName = rollName;
        this.admin = admin;
        this.guest = guest;
    }

    public Long getRollId() {
        return rollId;
    }

    public void setRollId(Long rollId) {
        this.rollId = rollId;
    }

    public String getRollName() {
        return rollName;
    }

    public void setRollName(String rollName) {
        this.rollName = rollName;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getGuest() {
        return guest;
    }

    public void setGuest(String guest) {
        this.guest = guest;
    }
}
