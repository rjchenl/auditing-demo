package com.example.auditingdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auditingdemo.model.UserInfo;

/**
 * 用戶資訊儲存庫
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    
} 