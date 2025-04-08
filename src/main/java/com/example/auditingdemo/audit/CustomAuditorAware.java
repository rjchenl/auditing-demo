package com.example.auditingdemo.audit;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.service.TokenService;

/**
 * 自定義審計者提供者
 * 根據 Spring Data JPA 文檔實現 AuditorAware 接口
 * @see https://docs.spring.io/spring-data/jpa/reference/auditing.html#auditing.auditor-aware
 */
@Component
public class CustomAuditorAware implements AuditorAware<String> {
    
    @Autowired
    private TokenService tokenService;

    /**
     * 獲取當前操作用戶
     * 從 ThreadLocal 中獲取當前請求的 token，然後從 token 獲取用戶 ID
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        String token = UserContext.getCurrentUser();
        if (token != null) {
            try {
                return Optional.of(tokenService.getUserInfoFromToken(token).getUserId());
            } catch (Exception e) {
                return Optional.of("system");
            }
        }
        return Optional.of("anonymousUser");
    }
} 