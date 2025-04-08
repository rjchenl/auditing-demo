package com.example.auditingdemo.audit;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.service.TokenService;

import lombok.extern.slf4j.Slf4j;

/**
 * 自定義審計者提供者
 * 實現 Spring Data JPA 審計功能所需的AuditorAware接口
 */
@Slf4j
@Component
public class CustomAuditorAware implements AuditorAware<String> {
    
    @Autowired
    private TokenService tokenService;

    /**
     * 獲取當前操作用戶的標識
     * 從 UserContext 中獲取當前token，然後通過TokenService解析出用戶ID
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        String token = UserContext.getCurrentUser();
        if (token != null && !token.isEmpty()) {
            Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
            if (userInfo != null && userInfo.containsKey("user")) {
                return Optional.of(userInfo.get("user"));
            }
        }
        
        // 未找到有效的token或用戶信息，使用系統用戶
        return Optional.of("system");
    }
} 