package com.example.auditingdemo.listener;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.audit.AuditableInterface;
import com.example.auditingdemo.audit.UserAuditableInterface;
import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.service.TokenService;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用審計監聽器
 * 使用介面方式處理審計欄位
 * 支援層次化的審計介面結構
 */
@Slf4j
@Component
@Configurable
public class AuditEntityListener {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 在實體持久化之前填充創建相關的擴展審計欄位
     */
    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof AuditableInterface) {
            log.debug("實體創建前填充擴展審計欄位: {}", entity.getClass().getSimpleName());
            processAuditFieldsWithInterface((AuditableInterface) entity, true);
            
            // 處理特定於用戶的審計欄位
            if (entity instanceof UserAuditableInterface) {
                processUserAuditFields((UserAuditableInterface) entity, true);
            }
        }
    }
    
    /**
     * 在實體更新之前填充修改相關的擴展審計欄位
     */
    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof AuditableInterface) {
            log.debug("實體更新前填充擴展審計欄位: {}", entity.getClass().getSimpleName());
            processAuditFieldsWithInterface((AuditableInterface) entity, false);
            
            // 處理特定於用戶的審計欄位
            if (entity instanceof UserAuditableInterface) {
                processUserAuditFields((UserAuditableInterface) entity, false);
            }
        }
    }
    
    /**
     * 使用介面方式處理審計欄位
     * 
     * @param entity 實現審計介面的實體
     * @param isCreate 是否為創建操作
     */
    private void processAuditFieldsWithInterface(AuditableInterface entity, boolean isCreate) {
        String token = UserContext.getCurrentUser();
        
        try {
            // 獲取審計信息
            Map<String, String> userInfo = null;
            if (token != null && !token.isEmpty()) {
                TokenService tokenService = applicationContext.getBean(TokenService.class);
                userInfo = tokenService.getUserInfoFromToken(token);
            }
            
            if (isCreate) {
                // 設置創建相關審計欄位
                if (userInfo != null) {
                    entity.setCreatedCompany(userInfo.get("company"));
                    entity.setCreatedUnit(userInfo.get("unit"));
                    
                    // 在創建時也設置初始的修改者資訊，使其與創建者一致
                    entity.setModifiedCompany(userInfo.get("company"));
                    entity.setModifiedUnit(userInfo.get("unit"));
                } else {
                    entity.setCreatedCompany("系統");
                    entity.setCreatedUnit("系統");
                    
                    // 在創建時也設置初始的修改者資訊，使其與創建者一致
                    entity.setModifiedCompany("系統");
                    entity.setModifiedUnit("系統");
                }
            } else {
                // 設置修改相關審計欄位 - 僅在更新操作時設置
                if (userInfo != null) {
                    entity.setModifiedCompany(userInfo.get("company"));
                    entity.setModifiedUnit(userInfo.get("unit"));
                } else {
                    entity.setModifiedCompany("系統");
                    entity.setModifiedUnit("系統");
                }
            }
        } catch (Exception e) {
            log.error("使用介面處理審計欄位時發生錯誤: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 處理特定於用戶的審計欄位 - 使用介面方式
     * 
     * @param entity 實現UserAuditableInterface的實體
     * @param isCreate 是否為創建操作
     */
    private void processUserAuditFields(UserAuditableInterface entity, boolean isCreate) {
        String token = UserContext.getCurrentUser();
        
        try {
            // 獲取審計信息
            Map<String, String> userInfo = null;
            if (token != null && !token.isEmpty()) {
                TokenService tokenService = applicationContext.getBean(TokenService.class);
                userInfo = tokenService.getUserInfoFromToken(token);
            }
            
            if (isCreate) {
                // 設置創建相關審計欄位
                if (userInfo != null) {
                    entity.setCreatedName(userInfo.get("name"));
                    
                    // 在創建時也設置初始的修改者資訊，使其與創建者一致
                    entity.setModifiedName(userInfo.get("name"));
                } else {
                    entity.setCreatedName("系統");
                    entity.setModifiedName("系統");
                }
            } else {
                // 設置修改相關審計欄位 - 僅在更新操作時設置
                if (userInfo != null) {
                    entity.setModifiedName(userInfo.get("name"));
                } else {
                    entity.setModifiedName("系統");
                }
            }
        } catch (Exception e) {
            log.error("處理用戶審計欄位時發生錯誤: {}", e.getMessage(), e);
        }
    }
} 
