package com.example.auditingdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auditingdemo.model.UserInfo;

/**
 * 用戶信息仓库接口
 * 用於訪問用戶詳細信息表
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    
} 