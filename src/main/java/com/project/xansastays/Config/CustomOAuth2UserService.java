package com.project.xansastays.Config;

import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.GuestMaster.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private GuestRepository guestRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name  = oAuth2User.getAttribute("name");

        Optional<GuestMaster> existingGuest = guestRepository.findByEmail(email);

        if (existingGuest.isEmpty()) {
            GuestMaster guest = new GuestMaster();
            guest.setEmail(email);
            guest.setName(name);
            guest.setRole(GuestMaster.Role.GUEST);
            guest.setPassword("GOOGLE_AUTH");
            guest.setActive(true);
            guest.setDoj(new Date());
            guestRepository.save(guest);
        } else {
            // Agar name update hua Google pe toh DB bhi update karo
            GuestMaster guest = existingGuest.get();
            if (guest.getName() == null || guest.getName().isBlank()) {
                guest.setName(name);
                guestRepository.save(guest);
            }
        }

        return oAuth2User;
    }
}