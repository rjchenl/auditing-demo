package com.example.auditingdemo.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.model.User;
import com.example.auditingdemo.repository.UserRepository;

/**
 * 用戶控制器
 * 處理用戶CRUD操作，演示審計功能
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 獲取所有用戶
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 根據ID獲取用戶
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    /**
     * 創建新用戶
     * 使用X-User-Id頭部來模擬當前用戶
     */
    @PostMapping
    public User createUser(@RequestBody User user, 
                          @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            // 設置當前用戶ID，這在實際應用中通常由安全框架完成
            UserContext.setCurrentUser(userId);
            
            // 保存用戶
            return userRepository.save(user);
        } finally {
            // 重要：清除ThreadLocal以防止內存洩漏
            UserContext.clear();
        }
    }
    
    /**
     * 更新用戶
     * 使用X-User-Id頭部來模擬當前用戶
     */
    @PutMapping("/{id}")
    public User updateUser(@PathVariable UUID id, 
                          @RequestBody User userDetails,
                          @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            // 設置當前用戶ID
            UserContext.setCurrentUser(userId);
            
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            
            // 更新用戶基本信息
            if (userDetails.getEmail() != null) {
                user.setEmail(userDetails.getEmail());
            }
            if (userDetails.getCellphone() != null) {
                user.setCellphone(userDetails.getCellphone());
            }
            if (userDetails.getCompanyId() != null) {
                user.setCompanyId(userDetails.getCompanyId());
            }
            
            // 保存更新後的用戶
            return userRepository.save(user);
        } finally {
            // 清除ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 獲取所有用戶的審計信息
     * 輸出格式: [用戶名, 創建者ID, 創建者公司, 創建者部門, 創建者姓名, 創建時間, 修改者ID, 修改者公司, 修改者部門, 修改者姓名, 修改時間]
     */
    @GetMapping("/audit")
    public List<Object[]> getAuditInfo() {
        List<User> users = userRepository.findAll();
        
        return users.stream()
                .map(user -> new Object[] {
                        user.getUsername(),
                        user.getCreatedBy(),
                        user.getCreatedCompany(),
                        user.getCreatedUnit(),
                        user.getCreatedName(),
                        user.getCreatedTime(),
                        user.getModifiedBy(),
                        user.getModifiedCompany(),
                        user.getModifiedUnit(),
                        user.getModifiedName(),
                        user.getModifiedTime()
                })
                .collect(Collectors.toList());
    }
} 