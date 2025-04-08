package com.example.auditingdemo.controller;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.example.auditingdemo.service.TokenService;

/**
 * 用戶控制器
 * 處理用戶CRUD操作，演示審計功能
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TokenService tokenService;
    
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
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 創建新用戶
     * 使用 Authorization header 中的 token 來獲取當前用戶信息
     */
    @PostMapping
    public User createUser(
            @RequestBody User user,
            @RequestHeader(value = "Authorization", required = true) String token) {
        try {
            // 設置當前用戶 token
            UserContext.setCurrentUser(token);
            
            // 保存用戶
            return userRepository.save(user);
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 更新用戶
     * 使用 Authorization header 中的 token 來獲取當前用戶信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User userDetails,
            @RequestHeader(value = "Authorization", required = true) String token) {
        try {
            // 設置當前用戶 token
            UserContext.setCurrentUser(token);
            
            return userRepository.findById(id)
                    .map(user -> {
                        // 更新用戶基本信息
                        if (userDetails.getName() != null) {
                            user.setName(userDetails.getName());
                        }
                        if (userDetails.getEmail() != null) {
                            user.setEmail(userDetails.getEmail());
                        }
                        if (userDetails.getDescription() != null) {
                            user.setDescription(userDetails.getDescription());
                        }
                        if (userDetails.getCellphone() != null) {
                            user.setCellphone(userDetails.getCellphone());
                        }
                        if (userDetails.getCompanyId() != null) {
                            user.setCompanyId(userDetails.getCompanyId());
                        }
                        if (userDetails.getStatusId() != null) {
                            user.setStatusId(userDetails.getStatusId());
                        }
                        if (userDetails.getDefaultLanguage() != null) {
                            user.setDefaultLanguage(userDetails.getDefaultLanguage());
                        }
                        
                        // 保存更新後的用戶
                        return ResponseEntity.ok(userRepository.save(user));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 獲取所有用戶的審計信息
     */
    @GetMapping("/audit")
    public List<Map<String, Object>> getAuditInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZoneId zoneId = ZoneId.of("Asia/Taipei");
        
        return userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> auditInfo = new HashMap<>();
                    auditInfo.put("userId", user.getId());
                    auditInfo.put("username", user.getUsername());
                    
                    // 創建者信息
                    auditInfo.put("createdBy", user.getCreatedBy());
                    auditInfo.put("createdTime", user.getCreatedTime().atZone(zoneId).format(formatter));
                    auditInfo.put("createdCompany", user.getCreatedCompany());
                    auditInfo.put("createdUnit", user.getCreatedUnit());
                    auditInfo.put("createdName", user.getCreatedName());
                    
                    // 修改者信息
                    auditInfo.put("modifiedBy", user.getModifiedBy());
                    auditInfo.put("modifiedTime", user.getModifiedTime().atZone(zoneId).format(formatter));
                    auditInfo.put("modifiedCompany", user.getModifiedCompany());
                    auditInfo.put("modifiedUnit", user.getModifiedUnit());
                    auditInfo.put("modifiedName", user.getModifiedName());
                    
                    return auditInfo;
                })
                .collect(Collectors.toList());
    }
} 