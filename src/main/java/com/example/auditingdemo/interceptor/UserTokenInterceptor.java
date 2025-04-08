package com.example.auditingdemo.interceptor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 用戶Token拦截器
 * 負責從HTTP請求頭中提取用戶令牌並設置到ThreadLocal中
 * 驗證JWT Token並提取用戶信息
 */
@Slf4j
@Component
public class UserTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    /**
     * 在請求處理之前執行
     * 從請求頭中獲取用戶Token並設置到UserContext中
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 從請求頭中獲取授權令牌
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && !authHeader.isEmpty()) {
            // 處理Bearer格式的token
            String token = authHeader;
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            // 如果是簡單的用戶ID（非JWT格式），使用原始token
            if (!token.contains(".") && tokenService.getToken(token) != null) {
                String userId = token;
                token = tokenService.getToken(token);
                log.debug("用戶ID: {} -> 令牌: {}", userId, token);
            }
            
            // 設置當前用戶令牌
            UserContext.setCurrentUser(token);
            
            // 從令牌中獲取用戶信息
            Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
            if (userInfo != null) {
                String userId = userInfo.get("userId");
                String userName = userInfo.get("name");
                log.debug("令牌有效，用戶資訊: ID={}, 姓名={}", userId, userName);
            } else {
                log.warn("令牌無效，無法獲取用戶信息");
            }
        } else {
            log.debug("請求中沒有找到授權令牌");
        }
        
        // 返回true表示繼續處理請求
        return true;
    }
    
    /**
     * 在請求完成後執行
     * 清理ThreadLocal避免內存洩漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // 清理ThreadLocal，避免內存洩漏
        UserContext.clear();
    }
    
    /**
     * 掩碼處理JWT令牌，只顯示部分內容，用於日誌記錄
     */
    private String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        
        // 對於JWT格式的令牌，只顯示開頭和結尾部分
        if (token.contains(".")) {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                return parts[0] + ".xxxx." + parts[2].substring(0, Math.min(parts[2].length(), 6)) + "...";
            }
        }
        
        // 對於普通令牌，保留頭部和尾部字符
        if (token.length() <= 8) {
            return token;
        }
        
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
} 