package com.mhsa.backend.auth.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mhsa.backend.auth.model.Profile;
import com.mhsa.backend.auth.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm profile trong DB
        Profile profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Trả về đối tượng User của Spring Security
        return new org.springframework.security.core.userdetails.User(
                profile.getEmail(),
                profile.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + profile.getRole().name()))
        );
    }
}
