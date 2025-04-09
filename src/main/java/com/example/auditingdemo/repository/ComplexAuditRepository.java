package com.example.auditingdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auditingdemo.model.ComplexAudit;

/**
 * 複雜審計儲存庫接口
 */
@Repository
public interface ComplexAuditRepository extends JpaRepository<ComplexAudit, Long> {
    
} 