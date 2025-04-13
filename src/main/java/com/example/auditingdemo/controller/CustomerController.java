package com.example.auditingdemo.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * 批量創建顧客
     */
    @PostMapping("/batch")
    public List<Customer> createCustomers(
            @RequestBody List<Customer> customers,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            UserContext.setCurrentUser(token);
            
            List<Customer> savedCustomers = customerRepository.saveAll(customers);
            log.info("批量創建顧客成功，數量: {}", savedCustomers.size());
            
            return savedCustomers;
        } finally {
            UserContext.clear();
        }
    }
    
    /**
     * 更新顧客
     */
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customerDetails,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            
            UserContext.setCurrentUser(token);
            
            return customerRepository.findById(id)
                    .map(customer -> {
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
                        
                        Customer updatedCustomer = customerRepository.save(customer);
                        log.info("顧客更新成功，ID={}, 審計信息: modifiedBy={}", 
                                updatedCustomer.getId(), updatedCustomer.getModifiedBy().getUsername());
                        
                        return ResponseEntity.ok(updatedCustomer);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 刪除顧客
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        try {
            String token = extractToken(authHeader);
            log.info("從Authorization頭中提取到令牌: {}", token);
            UserContext.setCurrentUser(token);
            
            return customerRepository.findById(id)
                    .map(customer -> {
                        customerRepository.delete(customer);
                        log.info("顧客刪除成功，ID={}", id);
                        return ResponseEntity.ok().<Void>build();
                    })
                    .orElse(ResponseEntity.notFound().build());
        } finally {
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
                    auditInfo.put("createdCompany", customer.getCreatedCompany());
                    auditInfo.put("createdUnit", customer.getCreatedUnit());
                    auditInfo.put("createdName", customer.getCreatedName());
                    
                    // 修改者信息
                    auditInfo.put("modifiedBy", customer.getModifiedBy().getUsername());
                    auditInfo.put("modifiedByName", customer.getModifiedBy().getName());
                    auditInfo.put("modifiedTime", customer.getModifiedTime().atZone(zoneId).format(formatter));
                    auditInfo.put("modifiedCompany", customer.getModifiedCompany());
                    auditInfo.put("modifiedUnit", customer.getModifiedUnit());
                    auditInfo.put("modifiedName", customer.getModifiedName());
                    
                    return auditInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查詢特定時間範圍內修改的記錄
     */
    @GetMapping("/audit/modified")
    public List<Map<String, Object>> getModifiedInRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZoneId zoneId = ZoneId.of("Asia/Taipei");
        
        return customerRepository.findAll().stream()
                .filter(customer -> {
                    LocalDateTime modifiedTime = customer.getModifiedTime();
                    return !modifiedTime.isBefore(start) && !modifiedTime.isAfter(end);
                })
                .map(customer -> {
                    Map<String, Object> auditInfo = new HashMap<>();
                    auditInfo.put("customerId", customer.getId());
                    auditInfo.put("customerName", customer.getName());
                    auditInfo.put("modifiedBy", customer.getModifiedBy().getUsername());
                    auditInfo.put("modifiedTime", customer.getModifiedTime().atZone(zoneId).format(formatter));
                    auditInfo.put("modifiedCompany", customer.getModifiedCompany());
                    auditInfo.put("modifiedUnit", customer.getModifiedUnit());
                    auditInfo.put("modifiedName", customer.getModifiedName());
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