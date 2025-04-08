package com.example.auditingdemo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.example.auditingdemo.listener.EnvironmentAuditListener;
import com.example.auditingdemo.model.Environment;
import com.example.auditingdemo.repository.EnvironmentRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 環境配置控制器
 * 用於管理環境配置的CRUD及審核、部署操作
 */
@Slf4j
@RestController
@RequestMapping("/api/environments")
public class EnvironmentController {

    @Autowired
    private EnvironmentRepository environmentRepository;
    
    @Autowired
    private EnvironmentAuditListener environmentAuditListener;
    
    /**
     * 獲取所有環境配置
     */
    @GetMapping
    public List<Environment> getAllEnvironments() {
        return environmentRepository.findAll();
    }
    
    /**
     * 根據ID獲取環境配置
     */
    @GetMapping("/{id}")
    public ResponseEntity<Environment> getEnvironmentById(@PathVariable Long id) {
        return environmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 創建新環境配置
     */
    @PostMapping
    public ResponseEntity<Environment> createEnvironment(
            @RequestBody Environment environment,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            // 設置當前用戶token
            UserContext.setCurrentUser(token);
            
            // 驗證必要欄位
            if (environment.getName() == null || environment.getType() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // 檢查是否已存在相同名稱的配置
            if (environmentRepository.findByName(environment.getName()).isPresent()) {
                log.warn("已存在相同名稱的配置: name={}", environment.getName());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            
            // 保存環境配置
            Environment savedEnvironment = environmentRepository.save(environment);
            log.info("環境配置創建成功，ID={}, name={}, type={}, version={}",
                    savedEnvironment.getId(), savedEnvironment.getName(), 
                    savedEnvironment.getType(), savedEnvironment.getVersion());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEnvironment);
        } catch (Exception e) {
            log.error("創建環境配置時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            // 清除ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 更新環境配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<Environment> updateEnvironment(
            @PathVariable Long id,
            @RequestBody Environment environmentDetails,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            // 設置當前用戶token
            UserContext.setCurrentUser(token);
            
            return environmentRepository.findById(id)
                    .map(environment -> {
                        // 更新環境配置內容
                        if (environmentDetails.getConfigValue() != null) {
                            environment.setConfigValue(environmentDetails.getConfigValue());
                        }
                        
                        if (environmentDetails.getDescription() != null) {
                            environment.setDescription(environmentDetails.getDescription());
                        }
                        
                        if (environmentDetails.getStatus() != null) {
                            environment.setStatus(environmentDetails.getStatus());
                        }
                        
                        // 保存更新
                        Environment updatedEnvironment = environmentRepository.save(environment);
                        log.info("環境配置更新成功，ID={}, name={}",
                                updatedEnvironment.getId(), updatedEnvironment.getName());
                        
                        return ResponseEntity.ok(updatedEnvironment);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("更新環境配置時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            // 清除ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 審核環境配置
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<Environment> reviewEnvironment(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            // 設置當前用戶token
            UserContext.setCurrentUser(token);
            
            return environmentRepository.findById(id)
                    .map(environment -> {
                        // 執行審核操作
                        environmentAuditListener.performReview(environment, token);
                        
                        // 保存更新
                        Environment reviewedEnvironment = environmentRepository.save(environment);
                        log.info("環境配置審核成功，ID={}, reviewedBy={}", 
                                reviewedEnvironment.getId(), reviewedEnvironment.getReviewedBy());
                        
                        return ResponseEntity.ok(reviewedEnvironment);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("審核環境配置時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            // 清除ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 部署環境配置
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Environment> deployEnvironment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> deployDetails,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            // 設置當前用戶token
            UserContext.setCurrentUser(token);
            
            // 獲取版本號參數
            String newVersion = deployDetails != null ? deployDetails.get("version") : null;
            
            return environmentRepository.findById(id)
                    .map(environment -> {
                        // 檢查是否已審核
                        if (environment.getReviewedBy() == null) {
                            log.warn("環境配置未經審核，無法部署, ID={}", environment.getId());
                            ResponseEntity<Environment> error = ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
                            return error;
                        }
                        
                        // 執行部署操作
                        environmentAuditListener.performDeploy(environment, token, newVersion);
                        
                        // 保存更新
                        Environment deployedEnvironment = environmentRepository.save(environment);
                        log.info("環境配置部署成功，ID={}, deployedBy={}, version={}", 
                                deployedEnvironment.getId(), deployedEnvironment.getDeployedBy(),
                                deployedEnvironment.getVersion());
                        
                        return ResponseEntity.ok(deployedEnvironment);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("部署環境配置時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            // 清除ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 獲取已審核但未部署的環境配置
     */
    @GetMapping("/pending-deploy")
    public List<Environment> getPendingDeployEnvironments() {
        return environmentRepository.findByReviewedByIsNotNullAndDeployedByIsNull();
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