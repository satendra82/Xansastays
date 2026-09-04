package com.project.xansastays.GuestMaster;

import com.project.xansastays.ReviewMaster.ReviewMaster;
import com.project.xansastays.ReviewMaster.ReviewRepository;
import com.project.xansastays.RollMaster.RollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class GuestController {
    private static final Long   DEFAULT_ROLL_ID    = 2L;

    private static final Long   HOMEPAGE_HOTEL_ID  = 1L;
    /**
     * Injected from application.properties: app.upload.dir
     * Points to an absolute folder on disk OUTSIDE the JAR/classpath,
     * e.g.  C:/xansa-uploads/profiles
     * WebConfig registers this folder as a static-resource location
     * so files are served at  /uploads/profiles/<filename>
     */
    @Value("${app.upload.dir}")
    private String uploadDir;
    @Autowired
    GuestRepository guestRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    RollRepository  rollRepository;


    @GetMapping("/home")
    public String home(Model model) {
        // Homepage "What Our Guests Say" section ke liye latest 5 reviews
        List<ReviewMaster> latestReviews =
                reviewRepository.findTop6ByHotel_HotelIdOrderByReviewIdDesc(HOMEPAGE_HOTEL_ID);
        model.addAttribute("latestReviews", latestReviews);

        // review avg in home page near pic
        Double avgRating = reviewRepository.findAverageRatingByHotelId(1L);

        List<ReviewMaster> reviews = reviewRepository.findByHotel_HotelId(1L);

        model.addAttribute("avgRating",
                avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

        model.addAttribute("totalReviews", reviews.size());

        return "home";
    }

    @GetMapping("/login")
    public String login() {

        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {

        model.addAttribute("guest",new GuestMaster());
        return "signup";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("guest")GuestMaster guest, Model model,
                       @RequestParam("confirmPassword")String confirmPassword,  RedirectAttributes redirectAttributes ) {

        System.out.println("SAVE METHOD CALLED");
        System.out.println("Name = " + guest.getName());
        System.out.println("Email = " + guest.getEmail());

        Optional<GuestMaster>guestMaster=guestRepository.findByEmail(guest.getEmail());

        if(guestMaster.isPresent()) {
            model.addAttribute("guest",guest);
            model.addAttribute("emailError","Email already exist");

            return "signup";
        }


        if (!guest.getPassword().equals(confirmPassword)) {
            model.addAttribute("guest", guest);   // ← ADD THIS LINE
            model.addAttribute("confirmPassword", "Passwords do not match");
            return "signup";
        }


        BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();
        guest.setPassword(bCryptPasswordEncoder.encode(guest.getPassword()));

        redirectAttributes.addFlashAttribute("success", true);


        guestRepository.save(guest);
        return "redirect:/login";
    }

    @PostMapping("/login/save")
    public String loginSave(@RequestParam("email")String email, @RequestParam("password")String password,
                            RedirectAttributes redirectAttributes) {

        Optional<GuestMaster>guestMaster=guestRepository.findByEmail(email);
        BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();

        if(guestMaster.isPresent() && !guestMaster.get().getStatus()) {
            redirectAttributes.addFlashAttribute("deactivated","Your account has been deactivated");

            return "redirect:/login";
        }

        if(guestMaster.isPresent() && bCryptPasswordEncoder.matches(password,guestMaster.get().getPassword())) {
            return "dashboard";
        }

        redirectAttributes.addFlashAttribute("invalid",true);
            return "redirect:/login";

    }



}
