package com.mhsa.backend.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.auth.model.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    // Tìm account bằng email để đăng nhập
    Optional<Profile> findByEmail(String email);

    // Kiểm tra email đã tồn tại chưa (khi đăng ký)
    Boolean existsByEmail(String email);
}
