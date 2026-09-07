package com.project.xansastays.Config;

import com.project.xansastays.GuestMaster.EmailService;
import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.GuestMaster.GuestRepository;
import com.project.xansastays.GuestMaster.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class PasswordResetController {

    @Autowired private GuestRepository guestRepository;
    @Autowired private EmailService emailService;
    @Autowired private OtpService otpService;
    @Autowired private PasswordEncoder  passwordEncoder;

    private static final long OTP_VALIDITY_MS = 5 * 60 * 1000; // 5 min

    private static final String SESS_OTP      = "resetOtp";
    private static final String SESS_EMAIL    = "resetEmail";
    private static final String SESS_TIME     = "resetOtpTime";
    private static final String SESS_VERIFIED = "resetVerified";

    // ── STEP 1: GET /forgot-password — email entry page ──────
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    // ── STEP 1: POST /forgot-password — send OTP, go to OTP page ──
    @PostMapping("/forgot-password")
    public String sendResetOtp(@RequestParam("email") String email,
                               HttpSession session,
                               Model model) {

        Optional<GuestMaster> opt = guestRepository.findByEmail(email.trim().toLowerCase());

        if (opt.isEmpty()) {
            model.addAttribute("emailError", "No account found with this email address.");
            return "forgot-password";
        }

        GuestMaster guest = opt.get();
        String otp = otpService.generateOTP();

        session.setAttribute(SESS_OTP,  otp);
        session.setAttribute(SESS_EMAIL, guest.getEmail());
        session.setAttribute(SESS_TIME, System.currentTimeMillis());
        session.removeAttribute(SESS_VERIFIED);

        System.out.println("==========================================");
        System.out.println("FORGOT-PASSWORD OTP for " + guest.getEmail() + " is : " + otp);
        System.out.println("Valid for 5 minutes");
        System.out.println("==========================================");

        emailService.sendResetOtp(guest.getEmail(), otp);

        model.addAttribute("otpJustSent", true);
        model.addAttribute("maskedEmail", maskEmail(guest.getEmail()));
        return "forgotPasswordOtp";
    }

    // ── STEP 2: GET /forgot-password/verify-otp — OTP entry page ──
    @GetMapping("/forgot-password/verify-otp")
    public String showOtpPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute(SESS_EMAIL);
        if (email == null) return "redirect:/forgot-password";

        model.addAttribute("maskedEmail", maskEmail(email));
        return "forgotPasswordOtp";
    }

    // ── STEP 2: POST /forgot-password/verify-otp — check OTP, go to new-password page ──
    @PostMapping("/forgot-password/verify-otp")
    public String verifyResetOtp(@RequestParam("otp") String otp,
                                 HttpSession session,
                                 Model model) {

        String savedOtp = (String) session.getAttribute(SESS_OTP);
        Long   otpTime  = (Long)   session.getAttribute(SESS_TIME);
        String email    = (String) session.getAttribute(SESS_EMAIL);

        if (email != null) {
            model.addAttribute("maskedEmail", maskEmail(email));
        }

        if (otpTime == null || System.currentTimeMillis() - otpTime > OTP_VALIDITY_MS) {
            model.addAttribute("otpError", "OTP expired. Please request a new one.");
            return "forgotPasswordOtp";
        }

        if (savedOtp == null || !savedOtp.equals(otp)) {
            model.addAttribute("otpError", "Invalid OTP. Please try again.");
            return "forgotPasswordOtp";
        }

        session.setAttribute(SESS_VERIFIED, true);
        session.removeAttribute(SESS_OTP);
        session.removeAttribute(SESS_TIME);

        return "redirect:/reset-password";
    }

    // ── STEP 3: GET /reset-password — new password entry page ──
    @GetMapping("/reset-password")
    public String showResetPasswordPage(HttpSession session) {
        Boolean verified = (Boolean) session.getAttribute(SESS_VERIFIED);
        if (verified == null || !verified) return "redirect:/forgot-password";
        return "reset-password";
    }

    // ── STEP 3: POST /reset-password — save new password ──
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("newPassword")     String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                HttpSession session,
                                Model model) {

        Boolean verified = (Boolean) session.getAttribute(SESS_VERIFIED);
        String  email    = (String)  session.getAttribute(SESS_EMAIL);

        if (verified == null || !verified || email == null) {
            return "redirect:/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("otpError", "Passwords do not match.");
            return "reset-password";
        }

        Optional<GuestMaster> opt = guestRepository.findByEmail(email);
        if (opt.isPresent()) {
            GuestMaster guest = opt.get();
            guest.setPassword(passwordEncoder.encode(newPassword));
            guestRepository.save(guest);
        }

        session.removeAttribute(SESS_EMAIL);
        session.removeAttribute(SESS_VERIFIED);

        model.addAttribute("resetDone", true);
        return "reset-password";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) return name.charAt(0) + "***@" + domain;
        return name.substring(0, 2) + "***@" + domain;
    }
}