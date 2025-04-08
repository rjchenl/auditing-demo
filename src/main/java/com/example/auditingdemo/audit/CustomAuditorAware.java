package com.example.auditingdemo.audit;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

/**
 * 自定義審計用戶提供者
 * 實現AuditorAware接口，提供當前操作用戶的ID
 * 實際生產環境中通常從SecurityContext或JWT Token獲取用戶信息
 */
@Component
public class CustomAuditorAware implements AuditorAware<String> {

    /**
     * 取得當前操作用戶ID
     * SecurityContextHolder或JWT Token中提取用戶ID
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        // 獲取當前用戶ID
        String currentUser = UserContext.getCurrentUser();
        return Optional.ofNullable(currentUser).or(() -> Optional.of("system"));
    }
} 