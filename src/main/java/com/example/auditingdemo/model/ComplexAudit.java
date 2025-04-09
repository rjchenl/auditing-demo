package com.example.auditingdemo.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 複雜審計示範實體
 * 使用實體關聯方式實現審計，直接關聯到User實體
 */
@Entity
@Table(name = "pf_demo_complex_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplexAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column
    private String description;
    
    // 使用實體關聯方式的審計字段
    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdByUser;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @ManyToOne
    @JoinColumn(name = "last_modified_by_id", nullable = false)
    private User lastModifiedByUser;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_modified_time", nullable = false)
    private LocalDateTime lastModifiedTime;
    
    @Column(nullable = false)
    private Integer version;
} 