package com.example.auditingdemo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auditingdemo.service.TokenService;

import lombok.extern.slf4j.Slf4j;

/**
 * Token控制器
 * 提供獲取JWT令牌的API接口，方便測試
 */
@Slf4j
@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    @Autowired
    private TokenService tokenService;
    
    /**
     * 獲取指定用戶的JWT令牌
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, String>> getToken(@PathVariable String userId) {
        String token = tokenService.getJwtToken(userId);
        
        if (token == null) {
            log.warn("未找到用戶 {} 的令牌", userId);
            return ResponseEntity.notFound().build();
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("tokenHeader", "Bearer " + token);
        response.put("curlExample", getCurlExample(userId, token));
        
        log.info("為用戶 {} 生成JWT令牌", userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 獲取所有可用的JWT令牌
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTokens() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> tokens = tokenService.getAllJwtTokens();
        
        response.put("availableUsers", tokens.keySet());
        
        Map<String, Map<String, String>> userExamples = new HashMap<>();
        for (String userId : tokens.keySet()) {
            Map<String, String> examples = new HashMap<>();
            examples.put("token", tokens.get(userId));
            examples.put("tokenHeader", "Bearer " + tokens.get(userId));
            examples.put("curlExample", getCurlExample(userId, tokens.get(userId)));
            userExamples.put(userId, examples);
        }
        
        response.put("userExamples", userExamples);
        
        log.info("提供所有可用JWT令牌");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 生成curl示例命令
     */
    private String getCurlExample(String userId, String token) {
        return "curl -X POST http://localhost:8080/api/apis -H \"Content-Type: application/json\" -H \"Authorization: Bearer " 
                + token + "\" -d '{\"apiname\": \"" + userId + "-api\", \"description\": \"" + userId + "的API\"}'";
    }
} 