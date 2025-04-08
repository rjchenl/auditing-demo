package com.example.auditingdemo.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Token服務
 * 簡單的key-value方式模擬token與用戶信息的映射
 */
@Slf4j
@Service
public class TokenService {
    
    // 單例實例
    private static TokenService instance;
    
    // 模擬token到用戶信息的映射
    private static final Map<String, Map<String, String>> TOKEN_USER_MAP = new HashMap<>();
    
    static {
        // 初始化模擬數據
        Map<String, String> kenbaiInfo = new HashMap<>();
        kenbaiInfo.put("userId", "kenbai");
        kenbaiInfo.put("name", "肯白");
        kenbaiInfo.put("company", "拓連科技");
        kenbaiInfo.put("unit", "行銷部");
        TOKEN_USER_MAP.put("kenbai", kenbaiInfo);
        
        Map<String, String> peterInfo = new HashMap<>();
        peterInfo.put("userId", "peter");
        peterInfo.put("name", "彼得");
        peterInfo.put("company", "拓連科技");
        peterInfo.put("unit", "研發部");
        TOKEN_USER_MAP.put("peter", peterInfo);
        
        Map<String, String> shawnInfo = new HashMap<>();
        shawnInfo.put("userId", "shawn");
        shawnInfo.put("name", "肖恩");
        shawnInfo.put("company", "拓連科技");
        shawnInfo.put("unit", "產品部");
        TOKEN_USER_MAP.put("shawn", shawnInfo);
        
        Map<String, String> systemInfo = new HashMap<>();
        systemInfo.put("userId", "system");
        systemInfo.put("name", "系統");
        systemInfo.put("company", "系統");
        systemInfo.put("unit", "系統");
        TOKEN_USER_MAP.put("system", systemInfo);
    }
    
    /**
     * 獲取TokenService的單例實例
     */
    public static synchronized TokenService getInstance() {
        if (instance == null) {
            instance = new TokenService();
        }
        return instance;
    }
    
    /**
     * 從token中獲取用戶信息
     */
    public Map<String, String> getUserInfoFromToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("Token為空，無法獲取用戶信息");
            return null;
        }
        
        // 如果token中包含Bearer前綴，則去除
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 直接根據token獲取用戶信息
        Map<String, String> userInfo = TOKEN_USER_MAP.get(token.toLowerCase());
        
        if (userInfo == null) {
            log.warn("未找到token [{}] 對應的用戶信息", token);
            return null;
        }
        
        return userInfo;
    }
    
    /**
     * 獲取指定用戶的令牌
     */
    public String getToken(String userId) {
        return userId.toLowerCase(); // 簡化版中，令牌就是用戶ID本身
    }
    
    /**
     * 添加自定義用戶令牌
     */
    public void addCustomToken(String token, Map<String, String> userInfo) {
        if (token != null && !token.isEmpty() && userInfo != null) {
            TOKEN_USER_MAP.put(token.toLowerCase(), new HashMap<>(userInfo));
        }
    }
} 