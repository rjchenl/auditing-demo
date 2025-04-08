package com.example.auditingdemo.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.auditingdemo.audit.EnvironmentAuditableInterface;
import com.example.auditingdemo.listener.AuditEntityListener;
import com.example.auditingdemo.listener.EnvironmentAuditListener;
import com.fasterxml.jackson.annotation.JsonFormat;

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
 * 環境配置實體類
 * 用於測試不同的擴充審計欄位組合
 */
@Entity
@Table(name = "pf_environment")
@EntityListeners({AuditingEntityListener.class, EnvironmentAuditListener.class, AuditEntityListener.class})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Environment implements EnvironmentAuditableInterface {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String type;
    
    @Column(name = "config_value")
    private String configValue;
    
    // 標準審計欄位 - 由Spring Data JPA自動處理
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;
    
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @LastModifiedBy
    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;
    
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;
    
    // 環境配置特定的擴充審計欄位
    @Column(name = "reviewed_by")
    private String reviewedBy;
    
    @Column(name = "reviewed_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedTime;
    
    @Column(name = "deployed_by")
    private String deployedBy;
    
    @Column(name = "deployed_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deployedTime;
    
    @Column(name = "version")
    private String version;
    
    @Column(nullable = false)
    private Integer status; // 0:草稿, 1:審核中, 2:已審核, 3:已部署
    
    // =====擴展審計欄位=====
    
    @Column(name = "created_company", updatable = false)
    private String createdCompany;
    
    @Column(name = "created_unit", updatable = false)
    private String createdUnit;
    
    @Column(name = "modified_company")
    private String modifiedCompany;
    
    @Column(name = "modified_unit")
    private String modifiedUnit;
    
    // =====環境特有審計欄位=====
    
    @Column(name = "reviewed_company")
    private String reviewedCompany;
    
    @Column(name = "reviewed_unit")
    private String reviewedUnit;
    
    @Column(name = "deployed_company")
    private String deployedCompany;
    
    @Column(name = "deployed_unit")
    private String deployedUnit;
    
    // =====新增的審核相關欄位=====
    
    @Column(name = "reviewer_name")
    private String reviewerName;
    
    @Column(name = "review_status")
    private String reviewStatus;
    
    @Column(name = "review_comment")
    private String reviewComment;
    
    // =====新增的部署相關欄位=====
    
    @Column(name = "deployer_name")
    private String deployerName;
    
    @Column(name = "deploy_status")
    private String deployStatus;
    
    @Column(name = "deploy_comment")
    private String deployComment;
} 