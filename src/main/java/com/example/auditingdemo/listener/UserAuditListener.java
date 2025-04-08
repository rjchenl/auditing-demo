package com.example.auditingdemo.listener;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.model.User;
import com.example.auditingdemo.service.TokenService;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * 用戶審計監聽器
 * 用於處理擴展審計欄位 (company, unit, name)
 * 這些欄位不在 Spring Data JPA 標準審計功能中，需要額外處理
 * 
 * 注意：改用 PostPersist 和 PostUpdate 處理，確保在實體持久化後處理
 */
@Slf4j
@Component
@Configurable
public class UserAuditListener {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 在實體持久化後填充擴展審計欄位
     */
    @PostPersist
    public void postPersist(User user) {
        log.info("實體持久化後填充創建審計欄位，token: {}", UserContext.getCurrentUser());
        fillExtendedAuditFields(user, true);
    }
    
    /**
     * 在實體更新後填充擴展審計欄位
     */
    @PostUpdate
    public void postUpdate(User user) {
        log.info("實體更新後填充更新審計欄位，token: {}", UserContext.getCurrentUser());
        fillExtendedAuditFields(user, false);
    }
    
    /**
     * 填充擴展審計欄位
     * @param user 用戶實體
     * @param isCreate 是否為創建操作
     */
    private void fillExtendedAuditFields(User user, boolean isCreate) {
        String token = UserContext.getCurrentUser();
        if (token != null && !token.isEmpty()) {
            try {
                TokenService tokenService = applicationContext.getBean(TokenService.class);
                Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
                log.info("從 token [{}] 獲取到用戶 [{}] 的信息：公司={}, 部門={}, 姓名={}",
                        token, userInfo.get("userId"), userInfo.get("company"), userInfo.get("unit"), userInfo.get("name"));
                
                if (isCreate) {
                    user.setCreatedCompany(userInfo.get("company"));
                    user.setCreatedUnit(userInfo.get("unit"));
                    user.setCreatedName(userInfo.get("name"));
                }
                user.setModifiedCompany(userInfo.get("company"));
                user.setModifiedUnit(userInfo.get("unit"));
                user.setModifiedName(userInfo.get("name"));
            } catch (Exception e) {
                log.error("處理用戶審計信息時發生錯誤: {}", e.getMessage(), e);
                setDefaultAuditInfo(user, isCreate);
            }
        } else {
            log.warn("沒有找到當前用戶 token，使用默認值");
            setDefaultAuditInfo(user, isCreate);
        }
    }
    
    /**
     * 設置默認審計信息
     */
    private void setDefaultAuditInfo(User user, boolean isCreate) {
        if (isCreate) {
            user.setCreatedCompany("系統");
            user.setCreatedUnit("系統");
            user.setCreatedName("系統");
        }
        user.setModifiedCompany("系統");
        user.setModifiedUnit("系統");
        user.setModifiedName("系統");
    }
} 