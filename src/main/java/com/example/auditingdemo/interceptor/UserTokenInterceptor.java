package com.example.auditingdemo.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.auditingdemo.model.UserInfo;
import com.example.auditingdemo.repository.UserInfoRepository;
import com.example.auditingdemo.util.UserContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用戶Token拦截器
 * 負責從HTTP請求頭中提取用戶標識符並設置到ThreadLocal中
 * 在實際應用中會解析JWT Token獲取用戶ID
 */
@Component
public class UserTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserInfoRepository userInfoRepository;

    /**
     * 在請求處理之前執行
     * 從請求頭中獲取用戶ID並設置到UserContext中
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 從請求頭中獲取用戶ID
        // 實際應用中這裡可能是解析JWT Token
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            // 設置用戶ID
            UserContext.setCurrentUserId(userId);
            
            // 獲取並設置用戶詳細資訊
            userInfoRepository.findById(userId).ifPresent(userInfo -> {
                UserContext.setCurrentUserInfo(userInfo);
            });
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