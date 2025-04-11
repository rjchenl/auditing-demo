package com.example.auditingdemo.audit;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

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

    /**
     * 獲取當前操作用戶的物件
     * 從 UserContext 中獲取當前token，然後通過TokenService解析出用戶ID，最後從數據庫獲取用戶
     */
    @Override
    public Optional<User> getCurrentAuditor() {
        String token = UserContext.getCurrentUser();
        if (token != null && !token.isEmpty()) {
            Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
            if (userInfo != null && userInfo.containsKey("userId")) {
                String userId = userInfo.get("userId");
                log.debug("獲取審計者ID: {}", userId);
                
                // 嘗試從用戶名查詢用戶
                if (userInfo.containsKey("username")) {
                    return userRepository.findByUsername(userInfo.get("username"));
                }
                
                // 從數據庫查找用戶 - 這裡需要有一個根據userId查找用戶的方法
                // 如果用戶不存在，則創建一個臨時的用戶對象
                return Optional.of(User.builder()
                        .username(userId)
                        .name(userInfo.getOrDefault("name", "系統用戶"))
                        .build());
            }
        }
        
        // 未找到有效的token或用戶信息，使用系統用戶
        log.debug("使用默認審計者: system");
        return Optional.of(User.builder()
                .username("system")
                .name("系統用戶")
                .build());
    }
} 