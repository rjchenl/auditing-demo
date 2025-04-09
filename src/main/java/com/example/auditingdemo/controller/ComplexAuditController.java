package com.example.auditingdemo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import com.example.auditingdemo.model.ComplexAudit;
import com.example.auditingdemo.model.User;
import com.example.auditingdemo.repository.ComplexAuditRepository;
import com.example.auditingdemo.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 複雜審計控制器
 * 演示使用實體關聯方式的審計
 */
@Slf4j
@RestController
@RequestMapping("/api/demo-complex-audit")
public class ComplexAuditController {

    @Autowired
    private ComplexAuditRepository complexAuditRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 獲取所有複雜審計記錄
     */
    @GetMapping
    public List<ComplexAudit> getAllComplexAudits() {
        return complexAuditRepository.findAll();
    }
    
    /**
     * 根據ID獲取複雜審計記錄
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComplexAudit> getComplexAudit(@PathVariable Long id) {
        return complexAuditRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 創建複雜審計記錄
     * 手動處理審計字段
     */
    @PostMapping
    public ComplexAudit createComplexAudit(
            @RequestBody ComplexAudit complexAudit,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            // 設置請求頭中的用戶令牌到UserContext
            setUserContext(authHeader);
            
            // 獲取當前審計用戶
            User currentUser = getCurrentAuditor();
            
            // 設置創建時間和更新時間
            LocalDateTime now = LocalDateTime.now();
            
            // 設置初始版本號
            complexAudit.setVersion(0);
            
            // 設置審計字段
            complexAudit.setCreatedByUser(currentUser);
            complexAudit.setCreatedTime(now);
            complexAudit.setLastModifiedByUser(currentUser);
            complexAudit.setLastModifiedTime(now);
            
            // 保存記錄
            ComplexAudit savedAudit = complexAuditRepository.save(complexAudit);
            
            log.info("創建複雜審計記錄: id={}, name={}, 創建者={}", 
                    savedAudit.getId(), savedAudit.getName(), currentUser.getUsername());
            
            return savedAudit;
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 更新複雜審計記錄
     * 手動處理審計字段
     */
    @PutMapping("/{id}")
    public ResponseEntity<ComplexAudit> updateComplexAudit(
            @PathVariable Long id,
            @RequestBody ComplexAudit complexAudit,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            // 設置請求頭中的用戶令牌到UserContext
            setUserContext(authHeader);
            
            // 獲取當前審計用戶
            User currentUser = getCurrentAuditor();
            
            return complexAuditRepository.findById(id)
                    .map(existingAudit -> {
                        // 更新數據
                        existingAudit.setName(complexAudit.getName());
                        existingAudit.setDescription(complexAudit.getDescription());
                        
                        // 更新版本號
                        existingAudit.setVersion(existingAudit.getVersion() + 1);
                        
                        // 更新審計字段
                        existingAudit.setLastModifiedByUser(currentUser);
                        existingAudit.setLastModifiedTime(LocalDateTime.now());
                        
                        // 保存記錄
                        ComplexAudit updatedAudit = complexAuditRepository.save(existingAudit);
                        
                        log.info("更新複雜審計記錄: id={}, name={}, 修改者={}", 
                                updatedAudit.getId(), updatedAudit.getName(), currentUser.getUsername());
                        
                        return ResponseEntity.ok(updatedAudit);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 獲取當前審計用戶
     */
    private User getCurrentAuditor() {
        // 從UserContext獲取當前token
        String token = UserContext.getCurrentUser();
        log.debug("獲取ComplexAudit審計用戶，當前token: {}", token);
        
        // 首先嘗試從token獲取用戶
        if (token != null && !token.isEmpty()) {
            // 直接使用token作為用戶名查詢（簡化處理）
            Optional<User> user = userRepository.findByUsername(token);
            if (user.isPresent()) {
                log.debug("根據token找到用戶: {}", user.get().getUsername());
                return user.get();
            }
        }
        
        // 查找系統用戶
        log.debug("未找到對應用戶，使用系統用戶");
        Optional<User> systemUser = userRepository.findByUsername("system");
        if (systemUser.isPresent()) {
            return systemUser.get();
        }
        
        // 找不到任何用戶，拋出異常
        throw new IllegalStateException("找不到有效的審計用戶");
    }
    
    /**
     * 設置UserContext
     */
    private void setUserContext(String authHeader) {
        if (authHeader != null && !authHeader.isEmpty()) {
            String token = authHeader;
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            // 設置當前用戶令牌到ThreadLocal
            UserContext.setCurrentUser(token);
            log.debug("設置用戶上下文: {}", token);
        }
    }
} 