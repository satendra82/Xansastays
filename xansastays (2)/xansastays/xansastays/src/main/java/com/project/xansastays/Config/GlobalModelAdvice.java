package com.project.xansastays.Config;

import com.project.xansastays.GuestMaster.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private GuestRepository guestRepository;

    @ModelAttribute
    public void addUserInfo(Authentication authentication, Model model) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            guestRepository.findByEmail(authentication.getName()).ifPresent(guest -> {
                String name = guest.getName();
                model.addAttribute("guestName", name);
                model.addAttribute("guestEmail", guest.getEmail());
                model.addAttribute("guestInitial",
                        (name != null && !name.isEmpty())
                                ? String.valueOf(name.charAt(0)).toUpperCase() : "G");
                model.addAttribute("guestProfilePic", guest.getProfilePic());
            });
        }
    }
}