package com.example.auditingdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auditingdemo.model.CustomerWithEmbedded;

/**
 * 帶嵌入式審計元數據的顧客資料訪問接口
 */
public interface CustomerWithEmbeddedRepository extends JpaRepository<CustomerWithEmbedded, Long> {
    
} 