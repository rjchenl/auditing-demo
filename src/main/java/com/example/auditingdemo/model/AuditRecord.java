package com.example.auditingdemo.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 審計記錄實體
 * 示範使用實體關聯方式實現審計功能
 */
@Entity
@Table(name = "pf_audit_record")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String operation;
    
    @Column(nullable = false)
    private String targetType;
    
    @Column(nullable = false)
    private Long targetId;
    
    @Column(length = 1000)
    private String details;
    
    // 使用實體關聯方式的審計欄位
    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;
    
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "modified_by_id", nullable = false)
    private User modifiedBy;
    
    @LastModifiedDate
    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;
}