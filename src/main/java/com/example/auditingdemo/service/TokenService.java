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
        
        // 添加用於測試的簡單token
        Map<String, String> user111Info = new HashMap<>();
        user111Info.put("userId", "111");
        user111Info.put("name", "測試用戶111");
        user111Info.put("company", "測試公司");
        user111Info.put("unit", "測試部門");
        TOKEN_USER_MAP.put("111", user111Info);
        
        Map<String, String> user222Info = new HashMap<>();
        user222Info.put("userId", "222");
        user222Info.put("name", "測試用戶222");
        user222Info.put("company", "測試公司");
        user222Info.put("unit", "研發部門");
        TOKEN_USER_MAP.put("222", user222Info);
        
        Map<String, String> user333Info = new HashMap<>();
        user333Info.put("userId", "333");
        user333Info.put("name", "測試用戶333");
        user333Info.put("company", "測試公司");
        user333Info.put("unit", "管理部門");
        TOKEN_USER_MAP.put("333", user333Info);
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
        // 首先檢查TOKEN_USER_MAP中是否有對應的用戶信息
        if (token != null && TOKEN_USER_MAP.containsKey(token)) {
            log.info("找到預設的token映射: {}", token);
            return TOKEN_USER_MAP.get(token);
        }
        
        // 以下是原有的邏輯，作為備選
        Map<String, String> userInfo = new HashMap<>();
        
        // 模擬不同的用戶令牌返回不同的用戶信息
        if (token.equals("test-token")) {
            userInfo.put("userId", "1001");
            userInfo.put("username", "test.user");
            userInfo.put("name", "測試使用者");
            userInfo.put("email", "test.user@example.com");
            userInfo.put("company", "測試公司");
            userInfo.put("unit", "研發部門");
            userInfo.put("roles", "ADMIN,USER");
        } else if (token.equals("admin-token")) {
            userInfo.put("userId", "1002");
            userInfo.put("username", "admin.user");
            userInfo.put("name", "管理員");
            userInfo.put("email", "admin@example.com");
            userInfo.put("company", "測試公司");
            userInfo.put("unit", "管理部門");
            userInfo.put("roles", "SUPER_ADMIN");
        } else {
            // 默認用戶信息
            userInfo.put("userId", "0");
            userInfo.put("username", "system");
            userInfo.put("name", "系統用戶");
            userInfo.put("email", "system@example.com");
            userInfo.put("roles", "SYSTEM");
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
            log.info("添加自定義令牌 {}: {}", token, userInfo);
        }
    }
} 