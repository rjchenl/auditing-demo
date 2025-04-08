package com.example.auditingdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.example.auditingdemo.audit.CustomAuditorAware;

/**
 * JPA審計配置類
 * 根據 Spring Data JPA 文檔配置審計功能
 * @see https://docs.spring.io/spring-data/jpa/reference/auditing.html
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration {
    
    /**
     * 配置審計者提供者
     * 用於獲取當前操作用戶的ID
     */
    @Bean
    @Primary
    public AuditorAware<String> auditorProvider() {
        return new CustomAuditorAware();
    }
} 