package com.example.auditingdemo.service;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Token服務
 * 用於從JWT令牌中獲取用戶信息
 */
@Slf4j
@Service
public class TokenService {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 模擬token到用戶信息的映射，實際環境下會解析JWT
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
     * 從JWT令牌中獲取用戶信息
     * 
     * @param token JWT令牌
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
        
        try {
            // 嘗試解析JWT令牌
            Map<String, String> jwtInfo = parseJwt(token);
            if (jwtInfo != null && !jwtInfo.isEmpty()) {
                log.info("從JWT令牌獲取到用戶 [{}] 的信息", jwtInfo.get("userId"));
                return jwtInfo;
            }
        } catch (Exception e) {
            log.debug("JWT解析失敗，嘗試使用模擬數據: {}", e.getMessage());
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
     * 解析JWT令牌（簡化版，僅用於演示）
     * 實際應用中應使用專門的JWT庫如jjwt
     */
    private Map<String, String> parseJwt(String token) {
        try {
            // 分割JWT
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            
            // 解碼payload部分
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payloadJson = objectMapper.readTree(payload);
            
            // 提取用戶信息
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userId", getStringValue(payloadJson, "sub"));
            userInfo.put("name", getStringValue(payloadJson, "name"));
            userInfo.put("company", getStringValue(payloadJson, "company"));
            userInfo.put("unit", getStringValue(payloadJson, "unit"));
            
            return userInfo;
        } catch (Exception e) {
            log.error("JWT解析錯誤: {}", e.getMessage());
            return null;
        }
    }
    
    private String getStringValue(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText() : null;
    }
    
    /**
     * 獲取所有模擬令牌數據
     * 
     * @return 所有模擬令牌到用戶信息的映射
     */
    public Map<String, Map<String, String>> getAllMockTokens() {
        return Collections.unmodifiableMap(MOCK_TOKEN_USER_MAP);
    }
    
    /**
     * 創建示例JWT令牌（僅用於演示）
     * 包含預設的用戶信息
     */
    public String createSampleJwt(String userId) {
        Map<String, String> userInfo = MOCK_TOKEN_USER_MAP.get(userId);
        if (userInfo == null) {
            return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzeXN0ZW0iLCJuYW1lIjoi57O757Wx" +
                   "IiwiY29tcGFueSI6Iuezu-e1sSIsInVuaXQiOiLns7vntbEiLCJleHAiOjE3" +
                   "MzIxMDQzODAsImlhdCI6MTczMjEwMDc4MH0.XYZ";
        }
        
        String header = "eyJhbGciOiJIUzI1NiJ9"; // {"alg":"HS256"}
        
        // 創建payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userInfo.get("userId"));
        payload.put("name", userInfo.get("name"));
        payload.put("company", userInfo.get("company"));
        payload.put("unit", userInfo.get("unit"));
        payload.put("exp", 1732104380);
        payload.put("iat", 1732100780);
        
        try {
            String payloadEncoded = Base64.getUrlEncoder().encodeToString(
                objectMapper.writeValueAsBytes(payload));
            
            // 為簡化起見，使用固定簽名
            String signature = "XYZ_SIGNATURE_" + userId;
            
            return header + "." + payloadEncoded + "." + signature;
        } catch (Exception e) {
            log.error("創建JWT失敗: {}", e.getMessage());
            return null;
        }
    }
} 