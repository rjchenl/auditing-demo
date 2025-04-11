package com.example.auditingdemo.audit;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.auditingdemo.model.User;
import com.example.auditingdemo.repository.UserRepository;
import com.example.auditingdemo.service.TokenService;

import lombok.extern.slf4j.Slf4j;

/**
 * 自定義審計者提供者
 * 實現 Spring Data JPA 審計功能所需的AuditorAware接口
 */
@Slf4j
@Component
public class CustomAuditorAware implements AuditorAware<User> {
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private UserRepository userRepository;
    
    // 系統管理員用戶ID - 用於解決循環依賴問題
    private static final Long SYSTEM_USER_ID = 1L;

    /**
     * 獲取當前操作用戶的物件
     * 從 UserContext 中獲取當前token，然後通過TokenService解析出用戶ID
     * 如果找不到用戶，則返回系統用戶
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getCurrentAuditor() {
        String token = UserContext.getCurrentUser();
        
        // 首先檢查是否已有系統用戶存在，這是解決循環依賴問題的關鍵
        Optional<User> systemUser = userRepository.findById(SYSTEM_USER_ID);
        if (systemUser.isPresent()) {
            // 如果有token，嘗試獲取實際用戶
            if (token != null && !token.isEmpty()) {
                Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
                if (userInfo != null && userInfo.containsKey("username")) {
                    Optional<User> userOpt = userRepository.findByUsername(userInfo.get("username"));
                    if (userOpt.isPresent()) {
                        log.debug("獲取審計者: {}", userOpt.get().getUsername());
                        return userOpt;
                    }
                }
            }
            // 使用系統用戶作為審計者
            log.debug("使用系統用戶作為審計者: {}", systemUser.get().getUsername());
            return systemUser;
        } else {
            // 如果系統用戶不存在，創建一個臨時用戶，但不保存到數據庫
            // 這是為了處理第一次啟動時的情況
            log.warn("系統用戶未找到，創建臨時用戶。這可能導致問題。請確保ID={}的系統用戶存在。", SYSTEM_USER_ID);
            User tempSystemUser = new User();
            tempSystemUser.setId(SYSTEM_USER_ID);
            tempSystemUser.setUsername("system");
            tempSystemUser.setName("系統用戶");
            tempSystemUser.setPassword("notimportant");
            tempSystemUser.setDescription("系統管理員");
            tempSystemUser.setStatusId("ACTIVE");
            
            return Optional.of(tempSystemUser);
        }
    }
} 