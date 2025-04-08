package com.example.auditingdemo.listener;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.audit.Auditable;
import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.model.User;
import com.example.auditingdemo.model.Api;
import com.example.auditingdemo.service.TokenService;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用審計監聽器
 * 使用反射機制處理帶有 @Auditable 註解的實體
 * 自動填充擴展審計欄位
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
        if (isAuditableEntity(entity)) {
            log.debug("實體創建前填充擴展審計欄位: {}", entity.getClass().getSimpleName());
            processAuditFields(entity, true);
        }
    }
    
    /**
     * 在實體更新之前填充修改相關的擴展審計欄位
     */
    @PreUpdate
    public void preUpdate(Object entity) {
        if (isAuditableEntity(entity)) {
            log.debug("實體更新前填充擴展審計欄位: {}", entity.getClass().getSimpleName());
            processAuditFields(entity, false);
        }
    }
    
    /**
     * 檢查實體是否帶有 @Auditable 註解
     */
    private boolean isAuditableEntity(Object entity) {
        return entity.getClass().isAnnotationPresent(Auditable.class);
    }
    
    /**
     * 處理審計欄位
     * 
     * @param entity 實體對象
     * @param isCreate 是否為創建操作
     */
    private void processAuditFields(Object entity, boolean isCreate) {
        String token = UserContext.getCurrentUser();
        
        try {
            // 獲取審計信息
            Map<String, String> userInfo = null;
            if (token != null && !token.isEmpty()) {
                TokenService tokenService = applicationContext.getBean(TokenService.class);
                userInfo = tokenService.getUserInfoFromToken(token);
            }
            
            // 獲取註解信息
            Auditable auditableAnnotation = entity.getClass().getAnnotation(Auditable.class);
            
            if (isCreate) {
                // 設置創建相關審計欄位
                if (userInfo != null) {
                    setFieldValue(entity, auditableAnnotation.createdCompanyField(), userInfo.get("company"));
                    setFieldValue(entity, auditableAnnotation.createdUnitField(), userInfo.get("unit"));
                    setFieldValue(entity, auditableAnnotation.createdNameField(), userInfo.get("name"));
                } else {
                    setFieldValue(entity, auditableAnnotation.createdCompanyField(), "系統");
                    setFieldValue(entity, auditableAnnotation.createdUnitField(), "系統");
                    setFieldValue(entity, auditableAnnotation.createdNameField(), "系統");
                }
            } else {
                // 設置修改相關審計欄位
                if (userInfo != null) {
                    setFieldValue(entity, auditableAnnotation.modifiedCompanyField(), userInfo.get("company"));
                    setFieldValue(entity, auditableAnnotation.modifiedUnitField(), userInfo.get("unit"));
                    setFieldValue(entity, auditableAnnotation.modifiedNameField(), userInfo.get("name"));
                } else {
                    setFieldValue(entity, auditableAnnotation.modifiedCompanyField(), "系統");
                    setFieldValue(entity, auditableAnnotation.modifiedUnitField(), "系統");
                    setFieldValue(entity, auditableAnnotation.modifiedNameField(), "系統");
                }
            }
        } catch (Exception e) {
            log.error("處理審計欄位時發生錯誤: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 使用反射設置欄位值
     */
    private void setFieldValue(Object entity, String fieldName, String value) {
        try {
            // 構建setter方法名稱 (例如: 欄位名 createdCompany -> setCreatedCompany)
            String setterMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            
            // 獲取setter方法
            Method setterMethod = entity.getClass().getMethod(setterMethodName, String.class);
            
            // 調用setter方法設置值
            setterMethod.invoke(entity, value);
        } catch (Exception e) {
            log.warn("設置欄位 {} 值時發生錯誤: {}", fieldName, e.getMessage());
        }
    }
} 
