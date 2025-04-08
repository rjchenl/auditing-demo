package com.example.auditingdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA審計功能配置類
 * 啟用Spring Data JPA的審計功能
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
    
    /**
     * 設定審計者提供者
     * @return AuditorAware<String> 審計者提供者
     */
    @Bean
    @Primary
    public AuditorAware<String> auditorProvider() {
        return new CustomAuditorAware();
    }
} 