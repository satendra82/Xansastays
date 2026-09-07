package com.project.xansastays.GuestMaster;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Optional;

@Controller
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final long OTP_VALIDITY_MS = 5 * 60 * 1000;


    @PostMapping("/send-otp")
    public String sendOtp(@ModelAttribute("guest") GuestMaster guest,
                          @RequestParam("confirmPassword") String confirmPassword,
                          HttpSession session,
                          Model model) {

        Optional<GuestMaster> existing = guestRepository.findByEmail(guest.getEmail());
        if (existing.isPresent()) {
            model.addAttribute("guest", guest);
            model.addAttribute("emailError", "Email already exists");
            return "signup";
        }

        if (!guest.getPassword().equals(confirmPassword)) {
            model.addAttribute("guest", guest);
            model.addAttribute("confirmPassword", "Passwords do not match");
            return "signup";
        }

        String otp = otpService.generateOTP();

        session.setAttribute("otp", otp);
        session.setAttribute("Users", guest);
        session.setAttribute("otpTime", System.currentTimeMillis());

        System.out.println("==========================================");
        System.out.println("OTP for " + guest.getEmail() + " is : " + otp);
        System.out.println("Valid for 5 minutes");
        System.out.println("==========================================");

        emailService.sendOtp(guest.getEmail(), otp);

        model.addAttribute("otpJustSent", true);
        model.addAttribute("maskedEmail", maskEmail(guest.getEmail()));

        return "otpVerification";
    }


    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            Model model) {

        String savedOtp = (String) session.getAttribute("otp");
        Long otpTime = (Long) session.getAttribute("otpTime");
        GuestMaster guest = (GuestMaster) session.getAttribute("Users");

        if (guest != null) {
            model.addAttribute("maskedEmail", maskEmail(guest.getEmail()));
        }

        if (otpTime == null || System.currentTimeMillis() - otpTime > OTP_VALIDITY_MS) {
            model.addAttribute("otpError", "OTP has expired. Please request a new OTP.");
            return "otpVerification";
        }

        if (savedOtp != null && savedOtp.equals(otp)) {

            // Encode password
            guest.setPassword(passwordEncoder.encode(guest.getPassword()));

            // Assign default role using GuestMaster's own Role enum
            guest.setRole(GuestMaster.Role.GUEST);

            // Set date of joining
            guest.setDoj(new Date());

            guestRepository.save(guest);

            session.removeAttribute("otp");
            session.removeAttribute("Users");
            session.removeAttribute("otpTime");

            model.addAttribute("verifiedSuccess", true);
            return "otpVerification";
        }

        model.addAttribute("otpError", "Invalid OTP. Please try again.");
        return "otpVerification";
    }


    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;

        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.substring(0, 2) + "***@" + domain;
    }
}