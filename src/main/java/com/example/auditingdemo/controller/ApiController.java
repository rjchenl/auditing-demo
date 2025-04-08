package com.example.auditingdemo.controller;

import com.example.auditingdemo.model.Api;
import com.example.auditingdemo.repository.ApiRepository;
import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.service.TokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/apis")
public class ApiController {

    @Autowired
    private ApiRepository apiRepository;
    
    @Autowired
    private TokenService tokenService;

    /**
     * 獲取所有API
     */
    @GetMapping
    public List<Api> getAllApis() {
        return apiRepository.findAll();
    }

    /**
     * 根據ID獲取API
     */
    @GetMapping("/{id}")
    public ResponseEntity<Api> getApiById(@PathVariable Long id) {
        Optional<Api> api = apiRepository.findById(id);
        return api.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 創建新API
     * 使用 Authorization header 作為 token 獲取當前用戶信息
     */
    @PostMapping
    public ResponseEntity<Api> createApi(
            @RequestBody Api api,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            // 設置當前用戶 token
            UserContext.setCurrentUser(token);
            
            log.info("接收到建立API請求: {}", api);
            
            // 確保字段長度不超過限制
            validateApiFields(api);
            
            Api savedApi = apiRepository.save(api);
            log.info("API建立成功: {}, 審計信息: createdBy={}, createdCompany={}, createdUnit={}", 
                    savedApi, savedApi.getCreatedBy(), savedApi.getCreatedCompany(), savedApi.getCreatedUnit());
                    
            return new ResponseEntity<>(savedApi, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("建立API時發生錯誤: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }

    /**
     * 更新API
     * 使用 Authorization header 作為 token 獲取當前用戶信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Api> updateApi(
            @PathVariable Long id, 
            @RequestBody Api apiDetails,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            // 設置當前用戶 token
            UserContext.setCurrentUser(token);
            
            Optional<Api> optionalApi = apiRepository.findById(id);
            if (optionalApi.isPresent()) {
                Api existingApi = optionalApi.get();
                
                // 更新基本信息
                if (apiDetails.getApiname() != null) {
                    existingApi.setApiname(apiDetails.getApiname());
                }
                if (apiDetails.getDescription() != null) {
                    existingApi.setDescription(apiDetails.getDescription());
                }
                
                // 確保字段長度不超過限制
                validateApiFields(existingApi);
                
                // 保存更新後的API
                Api updatedApi = apiRepository.save(existingApi);
                log.info("API更新成功: {}, 審計信息: modifiedBy={}, modifiedCompany={}, modifiedUnit={}", 
                        updatedApi, updatedApi.getModifiedBy(), updatedApi.getModifiedCompany(), updatedApi.getModifiedUnit());
                return new ResponseEntity<>(updatedApi, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("更新API時發生錯誤: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }

    /**
     * 刪除API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteApi(@PathVariable Long id) {
        try {
            apiRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 驗證API字段長度
     */
    private void validateApiFields(Api api) {
        // 驗證 apiname 長度不超過 255
        if (api.getApiname() != null && api.getApiname().length() > 255) {
            api.setApiname(api.getApiname().substring(0, 255));
        }
        
        // 驗證 description 長度不超過 255
        if (api.getDescription() != null && api.getDescription().length() > 255) {
            api.setDescription(api.getDescription().substring(0, 255));
        }
        
        // 驗證其他審計字段長度不超過 100
        if (api.getCreatedBy() != null && api.getCreatedBy().length() > 100) {
            api.setCreatedBy(api.getCreatedBy().substring(0, 100));
        }
        
        if (api.getCreatedCompany() != null && api.getCreatedCompany().length() > 100) {
            api.setCreatedCompany(api.getCreatedCompany().substring(0, 100));
        }
        
        if (api.getCreatedUnit() != null && api.getCreatedUnit().length() > 100) {
            api.setCreatedUnit(api.getCreatedUnit().substring(0, 100));
        }
        
        if (api.getCreatedName() != null && api.getCreatedName().length() > 100) {
            api.setCreatedName(api.getCreatedName().substring(0, 100));
        }
        
        if (api.getModifiedBy() != null && api.getModifiedBy().length() > 100) {
            api.setModifiedBy(api.getModifiedBy().substring(0, 100));
        }
        
        if (api.getModifiedCompany() != null && api.getModifiedCompany().length() > 100) {
            api.setModifiedCompany(api.getModifiedCompany().substring(0, 100));
        }
        
        if (api.getModifiedUnit() != null && api.getModifiedUnit().length() > 100) {
            api.setModifiedUnit(api.getModifiedUnit().substring(0, 100));
        }
        
        if (api.getModifiedName() != null && api.getModifiedName().length() > 100) {
            api.setModifiedName(api.getModifiedName().substring(0, 100));
        }
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