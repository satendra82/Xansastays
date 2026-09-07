package com.project.xansastays.Config;

import com.project.xansastays.GuestMaster.GuestMaster;
import com.project.xansastays.GuestMaster.GuestRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final GuestRepository guestRepository;

    public CustomUserDetailsService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        GuestMaster guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String authority = (guest.getRole() != null)
                ? "ROLE_" + guest.getRole().name()
                : "ROLE_GUEST";

        // NULL isActive (e.g. older rows from before this column existed) is treated as active,
        // so it never blocks login. Only an explicit false disables the account.
        boolean isActive = !Boolean.FALSE.equals(guest.getActive());

        return User.builder()
                .username(guest.getEmail())
                .password(guest.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .disabled(!isActive) // deactivated guests can no longer log in
                .build();
    }
}