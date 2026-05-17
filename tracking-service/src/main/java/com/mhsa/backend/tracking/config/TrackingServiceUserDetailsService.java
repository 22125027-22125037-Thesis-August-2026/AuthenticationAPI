package com.mhsa.backend.tracking.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TrackingServiceUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return User.builder()
                .username(username)
                .password("")
                .authorities(new java.util.ArrayList<>())
                .accountLocked(false)
                .credentialsExpired(false)
                .accountExpired(false)
                .disabled(false)
                .build();
    }
}
