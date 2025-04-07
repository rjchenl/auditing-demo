package com.example.auditingdemo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auditingdemo.model.User;

/**
 * 用戶仓库接口
 * 用於訪問用戶表
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * 根據用戶名查詢用戶
     */
    User findByUsername(String username);
} 