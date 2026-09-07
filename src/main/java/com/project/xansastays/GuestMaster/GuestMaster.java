package com.project.xansastays.GuestMaster;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name="guest_master")
public class GuestMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestId;
    private String email;
    private Byte aadharPhoto;
    private String password;
    private String name;
    private String contact;
    private String location;
    private String city;
    private String address;
    private String state;
    private String country;
    private Date doj;

    // FIX: was uninitialized (defaults to null). New guests would have a null
    // status, and Boolean.TRUE.equals(null) -> false, so brand-new accounts
    // looked "inactive" everywhere (admin stats, login checks, etc).
    private Boolean isActive = true;

    @Column(name = "profile_pic")
    private String profilePic;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    public enum Role {
        ADMIN,
        GUEST
    }

    public GuestMaster() {
    }

    public GuestMaster(Long guestId, String email, Byte aadharPhoto, String password, String name, String contact, String location, String city, Date doj, Boolean isActive, String profilePic, Role role) {
        this.guestId = guestId;
        this.email = email;
        this.aadharPhoto = aadharPhoto;
        this.password = password;
        this.name = name;
        this.contact = contact;
        this.location = location;
        this.city = city;
        this.doj = doj;
        this.isActive = isActive;
        this.profilePic = profilePic;
        this.role = role;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Byte getAadharPhoto() {
        return aadharPhoto;
    }

    public void setAadharPhoto(Byte aadharPhoto) {
        this.aadharPhoto = aadharPhoto;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Date getDoj() {
        return doj;
    }

    public void setDoj(Date doj) {
        this.doj = doj;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    // FIX: this used to be hardcoded "return false" — every guest showed as
    // inactive no matter what, silently breaking login's deactivated-account
    // check and the admin dashboard's active/inactive counts. Now it reflects
    // the real isActive field instead.
    public Boolean getStatus() {
        return isActive != null && isActive;
    }
}