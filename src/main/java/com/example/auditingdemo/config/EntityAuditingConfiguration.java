package com.example.auditingdemo.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.model.User;
import com.example.auditingdemo.repository.UserRepository;

/**
 * 實體關聯審計配置類
 * 提供 AuditorAware<User> 實現，用於實體關聯方式的審計
 * 注意：此配置類與主要的 JpaAuditingConfiguration 不會衝突，
 * 因為這裡只定義了 Bean，沒有啟用 @EnableJpaAuditing
 */
@Configuration
public class EntityAuditingConfiguration {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 提供以實體關聯方式的審計者
     */
    @Bean
    public AuditorAware<User> entityAuditorProvider() {
        return new CustomUserEntityAuditorAware(userRepository);
    }
    
    /**
     * 自定義以User實體為審計者的提供者
     */
    public static class CustomUserEntityAuditorAware implements AuditorAware<User> {
        
        private final UserRepository userRepository;
        
        public CustomUserEntityAuditorAware(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        public Optional<User> getCurrentAuditor() {
            String token = UserContext.getCurrentUser();
            if (token != null && !token.isEmpty()) {
                // 根據token獲取用戶名
                String username = token; // 簡化示例，實際可能需要從token中解析
                return userRepository.findByUsername(username);
            }
            return userRepository.findByUsername("system");
        }
    }
}