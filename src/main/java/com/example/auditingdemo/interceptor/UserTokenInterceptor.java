package com.example.auditingdemo.interceptor;

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
 * 在實際應用中會驗證JWT Token
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
        String token = request.getHeader("Authorization");
        if (token != null && !token.isEmpty()) {
            // 設置當前用戶令牌
            UserContext.setCurrentUser(token);
            
            // 記錄當前令牌信息
            log.info("設置當前用戶令牌: {}", token);
            
            // 檢查令牌是否有效
            if (tokenService.getUserInfoFromToken(token) != null) {
                log.info("令牌有效，用戶信息獲取成功");
            } else {
                log.warn("令牌無效，無法獲取用戶信息");
            }
        } else {
            log.warn("請求中沒有找到授權令牌");
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
} 