package com.example.auditingdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.example.auditingdemo.audit.CustomAuditorAware;

/**
 * JPA審計功能配置類
 * 啟用Spring Data JPA的審計功能
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
    
    /**
     * 配置審計功能的AuditorAware實現
     * 用於獲取當前操作用戶的ID
     */
    @Bean
    @Primary
    public AuditorAware<String> auditorProvider() {
        return new CustomAuditorAware();
    }
} 