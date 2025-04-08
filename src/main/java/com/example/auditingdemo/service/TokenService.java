package com.example.auditingdemo.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Token服務
 * 用於從token中獲取用戶信息
 * 
 * 注意：這只是一個模擬實現，實際應用中應該從JWT或其他令牌中解析用戶信息
 */
@Slf4j
@Service
public class TokenService {
    
    // 模擬token到用戶信息的映射
    private static final Map<String, Map<String, String>> MOCK_TOKEN_USER_MAP = new HashMap<>();
    
    static {
        // 初始化模擬數據
        Map<String, String> kenbaiInfo = new HashMap<>();
        kenbaiInfo.put("userId", "kenbai");
        kenbaiInfo.put("name", "肯白");
        kenbaiInfo.put("company", "拓連科技");
        kenbaiInfo.put("unit", "行銷部");
        MOCK_TOKEN_USER_MAP.put("kenbai", kenbaiInfo);
        
        Map<String, String> peterInfo = new HashMap<>();
        peterInfo.put("userId", "peter");
        peterInfo.put("name", "彼得");
        peterInfo.put("company", "拓連科技");
        peterInfo.put("unit", "研發部");
        MOCK_TOKEN_USER_MAP.put("peter", peterInfo);
        
        Map<String, String> shawnInfo = new HashMap<>();
        shawnInfo.put("userId", "shawn");
        shawnInfo.put("name", "肖恩");
        shawnInfo.put("company", "拓連科技");
        shawnInfo.put("unit", "產品部");
        MOCK_TOKEN_USER_MAP.put("shawn", shawnInfo);
        
        Map<String, String> systemInfo = new HashMap<>();
        systemInfo.put("userId", "system");
        systemInfo.put("name", "系統");
        systemInfo.put("company", "系統");
        systemInfo.put("unit", "系統");
        MOCK_TOKEN_USER_MAP.put("system", systemInfo);
    }
    
    /**
     * 從token中獲取用戶信息
     * 
     * @param token 用戶令牌
     * @return 用戶信息，若未找到則返回null
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
        
        // 從模擬數據中獲取用戶信息
        Map<String, String> userInfo = MOCK_TOKEN_USER_MAP.get(token);
        
        if (userInfo == null) {
            log.warn("未找到token [{}] 對應的用戶信息", token);
            return null;
        }
        
        log.info("從token [{}] 獲取到用戶 [{}] 的信息", token, userInfo.get("userId"));
        return userInfo;
    }
    
    /**
     * 獲取所有模擬令牌數據
     * 
     * @return 所有模擬令牌到用戶信息的映射
     */
    public Map<String, Map<String, String>> getAllMockTokens() {
        return Collections.unmodifiableMap(MOCK_TOKEN_USER_MAP);
    }
} 