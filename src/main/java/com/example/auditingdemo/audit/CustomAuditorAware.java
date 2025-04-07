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
     * 獲取當前操作用戶ID
     * 在實際應用中，應從SecurityContextHolder或JWT Token中提取用戶ID
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        // 在真實環境中，這裡應該從安全上下文或ThreadLocal獲取當前登錄用戶
        // 為了演示目的，我們先使用ThreadLocal來模擬
        String currentUser = UserContext.getCurrentUser();
        return Optional.ofNullable(currentUser).or(() -> Optional.of("system"));
    }
} 