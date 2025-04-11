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
import com.example.auditingdemo.model.Customer;
import com.example.auditingdemo.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 顧客控制器
 * 使用Spring Data JPA標準審計功能
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * 獲取所有顧客
     */
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    /**
     * 根據ID獲取顧客
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 創建新顧客
     * 使用 Authorization header 作為 token 獲取當前用戶信息
     */
    @PostMapping
    public Customer createCustomer(
            @RequestBody Customer customer,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            // 設置當前用戶 token
            UserContext.setCurrentUser(token);
            
            // 保存顧客
            Customer savedCustomer = customerRepository.save(customer);
            log.info("顧客創建成功，ID={}, 審計信息: createdBy={}", 
                    savedCustomer.getId(), savedCustomer.getCreatedBy().getUsername());
            
            return savedCustomer;
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 更新顧客
     * 使用 Authorization header 作為 token 獲取當前用戶信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customerDetails,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            // 提取token
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            // 設置當前用戶 token
            UserContext.setCurrentUser(token);
            
            return customerRepository.findById(id)
                    .map(customer -> {
                        // 更新顧客基本信息
                        if (customerDetails.getName() != null) {
                            customer.setName(customerDetails.getName());
                        }
                        if (customerDetails.getEmail() != null) {
                            customer.setEmail(customerDetails.getEmail());
                        }
                        if (customerDetails.getPhone() != null) {
                            customer.setPhone(customerDetails.getPhone());
                        }
                        if (customerDetails.getAddress() != null) {
                            customer.setAddress(customerDetails.getAddress());
                        }
                        if (customerDetails.getCompany() != null) {
                            customer.setCompany(customerDetails.getCompany());
                        }
                        
                        // 保存更新後的顧客
                        Customer updatedCustomer = customerRepository.save(customer);
                        log.info("顧客更新成功，ID={}, 審計信息: modifiedBy={}", 
                                updatedCustomer.getId(), updatedCustomer.getModifiedBy().getUsername());
                        
                        return ResponseEntity.ok(updatedCustomer);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 獲取所有顧客的審計信息
     */
    @GetMapping("/audit")
    public List<Map<String, Object>> getAuditInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZoneId zoneId = ZoneId.of("Asia/Taipei");
        
        return customerRepository.findAll().stream()
                .map(customer -> {
                    Map<String, Object> auditInfo = new HashMap<>();
                    auditInfo.put("customerId", customer.getId());
                    auditInfo.put("customerName", customer.getName());
                    
                    // 創建者信息
                    auditInfo.put("createdBy", customer.getCreatedBy().getUsername());
                    auditInfo.put("createdByName", customer.getCreatedBy().getName());
                    auditInfo.put("createdTime", customer.getCreatedTime().atZone(zoneId).format(formatter));
                    
                    // 修改者信息
                    auditInfo.put("modifiedBy", customer.getModifiedBy().getUsername());
                    auditInfo.put("modifiedByName", customer.getModifiedBy().getName());
                    auditInfo.put("modifiedTime", customer.getModifiedTime().atZone(zoneId).format(formatter));
                    
                    return auditInfo;
                })
                .collect(Collectors.toList());
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