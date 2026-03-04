package com.mhsa.backend.auth.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mhsa.backend.auth.dto.AuthResponse;
import com.mhsa.backend.auth.dto.LoginRequest;
import com.mhsa.backend.auth.dto.RegisterRequest;
import com.mhsa.backend.auth.dto.UserResponse;
import com.mhsa.backend.auth.model.User;
import com.mhsa.backend.auth.model.UserRole;
import com.mhsa.backend.auth.repository.UserRepository;
import com.mhsa.backend.auth.utils.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest request) {
        // 1. Check email trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        // 2. Tạo User mới
        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Mã hóa pass
                .phoneNumber(request.getPhoneNumber())
                .dob(request.getDob())
                .role(UserRole.MANAGER) // Mặc định đăng ký là Parent (Manager)
                .build();

        userRepository.save(user);
        return "User registered successfully!";
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Xác thực username/password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Tìm user để lấy thông tin
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // 3. Tạo Token
        var token = jwtUtils.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public UserResponse getCurrentUser() {
        // 1. Lấy userId từ Security Context (Do JwtFilter đã set vào trước đó)
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName());

        // 2. Query DB
        var user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Convert sang DTO (Không trả về password!)
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dob(user.getDob())
                .role(user.getRole().name())
                .creditsBalance(user.getCreditsBalance())
                .build();
    }
}
