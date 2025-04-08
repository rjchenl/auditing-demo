package com.example.auditingdemo.model;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.auditingdemo.listener.UserAuditListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * User實體類
 * 使用Spring Data JPA審計功能自動填充審計欄位
 */
@Entity
@Table(name = "pf_user")
@EntityListeners({AuditingEntityListener.class, UserAuditListener.class})
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "cellphone")
    private String cellphone;
    
    @Column(name = "company_id")
    private String companyId;
    
    @Column(name = "status_id", nullable = false)
    private String statusId;
    
    @Column(name = "default_language")
    private String defaultLanguage;
    
    // === Spring Data JPA審計欄位 ===
    
    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_time")
    private Instant createdTime;
    
    @LastModifiedBy
    @Column(name = "modified_by")
    private String modifiedBy;
    
    @LastModifiedDate
    @Column(name = "modified_time")
    private Instant modifiedTime;
    
    // === 擴展審計欄位 ===
    
    @Column(name = "created_company")
    private String createdCompany;
    
    @Column(name = "created_unit")
    private String createdUnit;
    
    @Column(name = "created_name")
    private String createdName;
    
    @Column(name = "modified_company")
    private String modifiedCompany;
    
    @Column(name = "modified_unit")
    private String modifiedUnit;
    
    @Column(name = "modified_name")
    private String modifiedName;
} 