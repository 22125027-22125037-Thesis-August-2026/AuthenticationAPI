package com.mhsa.backend.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.auth.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Tìm Parent bằng email để đăng nhập
    Optional<User> findByEmail(String email);

    // Kiểm tra email đã tồn tại chưa (khi đăng ký)
    Boolean existsByEmail(String email);

    // Lấy danh sách con của một Parent
    List<User> findByParentId(UUID parentId);
}
