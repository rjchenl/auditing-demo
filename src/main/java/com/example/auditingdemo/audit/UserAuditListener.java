package com.example.auditingdemo.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.model.User;
import com.example.auditingdemo.model.UserInfo;
import com.example.auditingdemo.repository.UserInfoRepository;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * 用戶審計監聽器
 * 負責在實體保存和更新時填充用戶的公司、單位和姓名等詳細信息
 */
@Component
public class UserAuditListener {
    
    // 使用靜態變量存儲仓库，因為JPA實體監聽器無法直接自動注入依賴
    private static UserInfoRepository userInfoRepository;
    
    @Autowired
    public void setUserInfoRepository(UserInfoRepository userInfoRepository) {
        UserAuditListener.userInfoRepository = userInfoRepository;
    }
    
    /**
     * 在實體保存前填充創建者詳細信息
     */
    @PrePersist
    public void prePersist(User user) {
        fillUserDetails(user, user.getCreatedBy(), true);
    }
    
    /**
     * 在實體更新前填充修改者詳細信息
     */
    @PreUpdate
    public void preUpdate(User user) {
        fillUserDetails(user, user.getModifiedBy(), false);
    }
    
    /**
     * 填充用戶詳細信息
     * @param user 用戶實體
     * @param userId 用戶ID（創建者或修改者）
     * @param isCreated 是否為創建操作
     */
    private void fillUserDetails(User user, String userId, boolean isCreated) {
        if (userId == null || userInfoRepository == null) {
            return;
        }
        
        UserInfo userInfo = userInfoRepository.findById(userId).orElse(null);
        if (userInfo != null) {
            if (isCreated) {
                user.setCreatedCompany(userInfo.getCompany());
                user.setCreatedUnit(userInfo.getUnit());
                user.setCreatedName(userInfo.getName());
            } else {
                user.setModifiedCompany(userInfo.getCompany());
                user.setModifiedUnit(userInfo.getUnit());
                user.setModifiedName(userInfo.getName());
            }
        }
    }
} 