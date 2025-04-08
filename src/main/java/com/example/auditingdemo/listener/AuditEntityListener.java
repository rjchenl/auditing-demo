package com.example.auditingdemo.listener;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.model.User;
import com.example.auditingdemo.model.Api;
import com.example.auditingdemo.service.TokenService;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用審計監聽器
 * 用於處理擴展審計欄位 (company, unit, name)
 * 這些欄位不在 Spring Data JPA 標準審計功能中，需要額外處理
 */
@Slf4j
@Component
@Configurable
public class AuditEntityListener {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 在實體持久化之前填充創建相關的擴展審計欄位
     * 只處理創建相關欄位，不處理修改相關欄位
     */
    @PrePersist
    public void prePersist(Object entity) {
        log.debug("實體創建前填充擴展審計欄位: {}", entity.getClass().getSimpleName());
        if (entity instanceof User) {
            fillAuditFields((User) entity, true);
        } else if (entity instanceof Api) {
            fillAuditFields((Api) entity, true);
        }
    }
    
    /**
     * 在實體更新之前填充修改相關的擴展審計欄位
     * 只處理修改相關欄位，不處理創建相關欄位
     */
    @PreUpdate
    public void preUpdate(Object entity) {
        log.debug("實體更新前填充擴展審計欄位: {}", entity.getClass().getSimpleName());
        if (entity instanceof User) {
            fillAuditFields((User) entity, false);
        } else if (entity instanceof Api) {
            fillAuditFields((Api) entity, false);
        }
    }
    
    /**
     * 通用方法：填充審計欄位
     * 
     * @param <T> 實體類型
     * @param entity 實體對象
     * @param isCreate 是否為創建操作 (true為創建，false為修改)
     */
    private <T> void fillAuditFields(T entity, boolean isCreate) {
        String token = UserContext.getCurrentUser();
        if (token != null && !token.isEmpty()) {
            try {
                TokenService tokenService = applicationContext.getBean(TokenService.class);
                Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
                
                if (userInfo != null) {
                    if (isCreate) {
                        setCreatedAuditFields(entity, userInfo);
                    } else {
                        setModifiedAuditFields(entity, userInfo);
                    }
                }
            } catch (Exception e) {
                log.error("處理審計信息時發生錯誤", e);
                if (isCreate) {
                    setDefaultCreatedInfo(entity);
                } else {
                    setDefaultModifiedInfo(entity);
                }
            }
        } else {
            if (isCreate) {
                setDefaultCreatedInfo(entity);
            } else {
                setDefaultModifiedInfo(entity);
            }
        }
    }
    
    /**
     * 根據實體類型設置創建相關審計欄位
     */
    private <T> void setCreatedAuditFields(T entity, Map<String, String> userInfo) {
        if (entity instanceof User) {
            User user = (User) entity;
            user.setCreatedCompany(userInfo.get("company"));
            user.setCreatedUnit(userInfo.get("unit"));
            user.setCreatedName(userInfo.get("name"));
        } else if (entity instanceof Api) {
            Api api = (Api) entity;
            api.setCreatedCompany(userInfo.get("company"));
            api.setCreatedUnit(userInfo.get("unit"));
            api.setCreatedName(userInfo.get("name"));
        }
    }
    
    /**
     * 根據實體類型設置修改相關審計欄位
     */
    private <T> void setModifiedAuditFields(T entity, Map<String, String> userInfo) {
        if (entity instanceof User) {
            User user = (User) entity;
            user.setModifiedCompany(userInfo.get("company"));
            user.setModifiedUnit(userInfo.get("unit"));
            user.setModifiedName(userInfo.get("name"));
        } else if (entity instanceof Api) {
            Api api = (Api) entity;
            api.setModifiedCompany(userInfo.get("company"));
            api.setModifiedUnit(userInfo.get("unit"));
            api.setModifiedName(userInfo.get("name"));
        }
    }
    
    /**
     * 設置默認創建審計信息
     */
    private <T> void setDefaultCreatedInfo(T entity) {
        if (entity instanceof User) {
            User user = (User) entity;
            user.setCreatedCompany("系統");
            user.setCreatedUnit("系統");
            user.setCreatedName("系統");
        } else if (entity instanceof Api) {
            Api api = (Api) entity;
            api.setCreatedCompany("系統");
            api.setCreatedUnit("系統");
            api.setCreatedName("系統");
        }
    }
    
    /**
     * 設置默認修改審計信息
     */
    private <T> void setDefaultModifiedInfo(T entity) {
        if (entity instanceof User) {
            User user = (User) entity;
            user.setModifiedCompany("系統");
            user.setModifiedUnit("系統");
            user.setModifiedName("系統");
        } else if (entity instanceof Api) {
            Api api = (Api) entity;
            api.setModifiedCompany("系統");
            api.setModifiedUnit("系統");
            api.setModifiedName("系統");
        }
    }
} 
