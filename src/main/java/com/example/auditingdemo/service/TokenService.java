package com.example.auditingdemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.auditingdemo.model.UserInfo;
import com.example.auditingdemo.repository.UserInfoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenService {
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    /**
     * 從 token 中獲取用戶信息
     * 這裡簡化實現，實際應該解析 JWT token
     */
    public UserInfo getUserInfoFromToken(String token) {
        // 在實際應用中，這裡應該：
        // 1. 驗證 token 的有效性
        // 2. 解析 JWT token
        // 3. 從 token 中提取用戶 ID
        // 4. 使用用戶 ID 查詢完整的用戶信息
        
        log.info("嘗試從 token [{}] 獲取用戶信息", token);
        
        // 這裡簡化處理，直接用 token 作為用戶 ID
        UserInfo userInfo = userInfoRepository.findById(token)
                .orElseThrow(() -> {
                    log.error("找不到 token [{}] 對應的用戶信息", token);
                    return new RuntimeException("Invalid token: " + token);
                });
                
        log.info("成功獲取用戶信息: userId={}, company={}, unit={}, name={}", 
                userInfo.getUserId(), userInfo.getCompany(), userInfo.getUnit(), userInfo.getName());
                
        return userInfo;
    }
} 