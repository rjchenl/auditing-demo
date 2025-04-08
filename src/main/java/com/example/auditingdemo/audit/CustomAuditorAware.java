package com.example.auditingdemo.audit;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 自定義審計者提供者
 * 根據 Spring Data JPA 文檔實現 AuditorAware 接口
 * @see https://docs.spring.io/spring-data/jpa/reference/auditing.html#auditing.auditor-aware
 * 
 * 注意：現在直接返回令牌作為審計者標識，不再解析出用戶ID
 */
@Slf4j
@Component
public class CustomAuditorAware implements AuditorAware<String> {

    /**
     * 獲取當前操作用戶的令牌
     * 從 ThreadLocal 中獲取當前請求的令牌
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        String token = UserContext.getCurrentUser();
        if (token != null && !token.isEmpty()) {
            log.debug("從 ThreadLocal 中獲取到當前用戶令牌: {}", token);
            return Optional.of(token);
        }
        log.debug("未找到當前用戶令牌，使用預設值");
        return Optional.of("system");
    }
} 