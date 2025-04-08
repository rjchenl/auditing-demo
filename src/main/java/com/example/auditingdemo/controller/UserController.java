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

import lombok.extern.slf4j.Slf4j;

/**
 * 用戶控制器
 * 處理用戶CRUD操作，演示審計功能
 */
@Slf4j
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
     * 使用 Authorization header 作為 token 獲取當前用戶信息
     */
    @PostMapping
    public User createUser(
            @RequestBody User user,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            // 設置當前用戶 token
            UserContext.setCurrentUser(token);
            
            // 保存用戶
            User savedUser = userRepository.save(user);
            log.info("用戶創建成功，ID={}, 審計信息: createdBy={}, createdCompany={}, createdUnit={}",
                    savedUser.getId(), savedUser.getCreatedBy(), 
                    savedUser.getCreatedCompany(), savedUser.getCreatedUnit());
            
            return savedUser;
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 更新用戶
     * 使用 Authorization header 作為 token 獲取當前用戶信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User userDetails,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
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
                        User updatedUser = userRepository.save(user);
                        log.info("用戶更新成功，ID={}, 審計信息: modifiedBy={}, modifiedCompany={}, modifiedUnit={}",
                                updatedUser.getId(), updatedUser.getModifiedBy(), 
                                updatedUser.getModifiedCompany(), updatedUser.getModifiedUnit());
                        
                        return ResponseEntity.ok(updatedUser);
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
    
    /**
     * 提供示例CURL命令
     */
    @GetMapping("/curl-example")
    public Map<String, String> getCurlExample() {
        Map<String, String> result = new HashMap<>();
        
        String curlCmd = "curl -X POST http://localhost:8080/api/users \\\n" +
                         "-H \"Content-Type: application/json\" \\\n" +
                         "-H \"Authorization: kenbai\" \\\n" +
                         "-d '{\n" +
                         "  \"name\": \"測試用戶\", \n" +
                         "  \"description\": \"測試描述\", \n" +
                         "  \"email\": \"test@example.com\", \n" +
                         "  \"username\": \"testuser\", \n" +
                         "  \"password\": \"password123\", \n" +
                         "  \"statusId\": \"1\"\n" +
                         "}'";
        
        result.put("createUserExample", curlCmd);
        
        String updateCmd = "curl -X PUT http://localhost:8080/api/users/1 \\\n" +
                          "-H \"Content-Type: application/json\" \\\n" +
                          "-H \"Authorization: peter\" \\\n" +
                          "-d '{\n" +
                          "  \"name\": \"已更新的用戶\", \n" +
                          "  \"description\": \"已更新的描述\"\n" +
                          "}'";
        
        result.put("updateUserExample", updateCmd);
        
        return result;
    }
    
    /**
     * 從 Authorization 頭中提取令牌
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
} 