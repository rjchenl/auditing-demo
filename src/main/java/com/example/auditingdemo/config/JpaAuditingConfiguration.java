package com.example.auditingdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.example.auditingdemo.audit.CustomAuditorAware;
import com.example.auditingdemo.model.User;

/**
 * JPA審計配置類
 * 啟用Spring Data JPA審計功能
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration {
    
    /**
     * 審計者提供者bean
     */
    @Bean
    public AuditorAware<User> auditorProvider() {
        return new CustomAuditorAware();
    }
} 