package com.example.auditingdemo.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 顧客實體類 - 使用嵌入式審計元數據
 * 展示如何將審計欄位封裝到一個嵌入式類中
 */
@Data
@Entity
@Table(name = "pf_customer_embedded")
@EntityListeners(AuditingEntityListener.class)
public class CustomerWithEmbedded {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "company")
    private String company;
    
    // 使用嵌入式審計元數據
    @Embedded
    private AuditMetadata auditMetadata = new AuditMetadata();
    
    /**
     * 嵌入式審計元數據類
     * 包含所有審計相關欄位
     */
    @Data
    @Embeddable
    public static class AuditMetadata {
        
        @CreatedBy
        @ManyToOne
        @JoinColumn(name = "created_by", nullable = false, updatable = false)
        private User createdBy;
        
        @CreatedDate
        @Column(name = "created_time", nullable = false, updatable = false)
        private Instant createdTime;
        
        @LastModifiedBy
        @ManyToOne
        @JoinColumn(name = "modified_by", nullable = false)
        private User modifiedBy;
        
        @LastModifiedDate
        @Column(name = "modified_time", nullable = false)
        private Instant modifiedTime;
    }
} 