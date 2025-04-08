package com.example.auditingdemo.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.auditingdemo.audit.UserAuditableInterface;
import com.example.auditingdemo.listener.AuditEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用戶實體類
 * 演示審計功能
 */
@Entity
@Table(name = "pf_user")
@EntityListeners({AuditingEntityListener.class, AuditEntityListener.class})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserAuditableInterface {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String email;
    
    private String description;
    
    @Column(unique = true)
    private String username;
    
    private String password;
    
    private String cellphone;
    
    private String companyId;
    
    private String statusId;
    
    private String defaultLanguage;
    
    // 標準審計欄位 - 由Spring Data JPA自動處理
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @LastModifiedBy
    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;
    
    @LastModifiedDate
    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;
    
    // 擴展審計欄位 - 由自定義的AuditEntityListener處理
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