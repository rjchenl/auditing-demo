package com.example.auditingdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auditingdemo.model.AuditRecord;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {
    // 你可以添加自定義查詢方法
}