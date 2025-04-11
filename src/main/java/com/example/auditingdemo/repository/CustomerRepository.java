package com.example.auditingdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auditingdemo.model.Customer;

/**
 * 顧客資料訪問接口
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
} 