package com.example.auditingdemo.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.model.User;
import com.example.auditingdemo.model.UserInfo;
import com.example.auditingdemo.repository.UserInfoRepository;
import com.example.auditingdemo.repository.UserRepository;

/**
 * 審計Demo控制器
 * 專門用於展示審計信息和用戶信息之間的關係
 */
@RestController
@RequestMapping("/api/audit-demo")
public class AuditDemoController {
    
    private static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    /**
     * 獲取所有審計信息，包含關聯的用戶信息
     */
    @GetMapping("/audit-with-details")
    public List<Map<String, Object>> getAuditWithDetails() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (User user : users) {
            Map<String, Object> entry = new HashMap<>();
            
            // 基本用戶資訊
            entry.put("userId", user.getId());
            entry.put("username", user.getUsername());
            
            // 創建者資訊
            entry.put("createdBy", user.getCreatedBy());
            entry.put("createdTime", formatInstant(user.getCreatedTime()));
            
            // 創建者詳細信息
            UserInfo creatorInfo = userInfoRepository.findById(user.getCreatedBy()).orElse(null);
            if (creatorInfo != null) {
                Map<String, String> creatorDetails = new HashMap<>();
                creatorDetails.put("userId", creatorInfo.getUserId());
                creatorDetails.put("name", creatorInfo.getName());
                creatorDetails.put("company", creatorInfo.getCompany());
                creatorDetails.put("unit", creatorInfo.getUnit());
                entry.put("creatorDetails", creatorDetails);
            }
            
            // 修改者資訊
            entry.put("modifiedBy", user.getModifiedBy());
            entry.put("modifiedTime", formatInstant(user.getModifiedTime()));
            
            // 修改者詳細信息
            UserInfo modifierInfo = userInfoRepository.findById(user.getModifiedBy()).orElse(null);
            if (modifierInfo != null) {
                Map<String, String> modifierDetails = new HashMap<>();
                modifierDetails.put("userId", modifierInfo.getUserId());
                modifierDetails.put("name", modifierInfo.getName());
                modifierDetails.put("company", modifierInfo.getCompany());
                modifierDetails.put("unit", modifierInfo.getUnit());
                entry.put("modifierDetails", modifierDetails);
            }
            
            result.add(entry);
        }
        
        return result;
    }
    
    /**
     * 創建用戶，並展示審計流程
     */
    @PostMapping("/create-with-audit")
    public Map<String, Object> createWithAudit(
            @RequestBody User user,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        
        try {
            // 1. 設置當前用戶
            UserContext.setCurrentUser(userId);
            
            // 2. 查詢操作者信息
            UserInfo operatorInfo = userInfoRepository.findById(userId).orElse(null);
            
            // 3. 保存用戶 (這將觸發審計功能)
            User savedUser = userRepository.save(user);
            
            // 4. 組裝結果，展示審計過程
            Map<String, Object> result = new HashMap<>();
            
            // 基本信息
            result.put("userId", savedUser.getId());
            result.put("username", savedUser.getUsername());
            
            // 審計資訊
            Map<String, String> auditInfo = new HashMap<>();
            auditInfo.put("createdBy", savedUser.getCreatedBy());
            auditInfo.put("createdTime", formatInstant(savedUser.getCreatedTime()));
            auditInfo.put("createdCompany", savedUser.getCreatedCompany());
            auditInfo.put("createdUnit", savedUser.getCreatedUnit());
            auditInfo.put("createdName", savedUser.getCreatedName());
            result.put("auditInfo", auditInfo);
            
            // 操作者詳細信息
            if (operatorInfo != null) {
                Map<String, String> operatorDetails = new HashMap<>();
                operatorDetails.put("userId", operatorInfo.getUserId());
                operatorDetails.put("name", operatorInfo.getName());
                operatorDetails.put("company", operatorInfo.getCompany());
                operatorDetails.put("unit", operatorInfo.getUnit());
                result.put("operatorFromUserInfo", operatorDetails);
            }
            
            // 審計流程說明
            List<String> auditProcess = Arrays.asList(
                "1. 從HTTP頭獲取操作者ID: " + userId,
                "2. 保存到UserContext的ThreadLocal中",
                "3. SpringData JPA通過CustomAuditorAware獲取操作者ID並設置created_by",
                "4. UserAuditListener從User.createdBy獲取ID (" + savedUser.getCreatedBy() + ")",
                "5. 使用這個ID從UserInfo表查詢操作者詳細資料",
                "6. 填充擴展審計欄位: created_company, created_unit, created_name"
            );
            result.put("auditProcess", auditProcess);
            
            return result;
        } finally {
            UserContext.clear();
        }
    }
    
    /**
     * 獲取所有用戶信息表數據
     */
    @GetMapping("/user-info")
    public List<UserInfo> getAllUserInfo() {
        return userInfoRepository.findAll();
    }
    
    /**
     * 根據用戶ID獲取用戶信息
     */
    @GetMapping("/user-info/{userId}")
    public ResponseEntity<UserInfo> getUserInfoById(@PathVariable String userId) {
        return userInfoRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 將 Instant 格式化為台北時區的時間字串
     */
    private String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(TAIPEI_ZONE).format(DATE_FORMATTER);
    }
} 