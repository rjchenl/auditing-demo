package com.example.auditingdemo.model;

import com.example.auditingdemo.listener.AuditEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * API實體類
 * 用於測試審計功能
 */
@Entity
@Table(name = "pf_api")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class, AuditEntityListener.class})
public class Api {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String apiname;

    private String description;

    // 標準審計欄位 - 由 Spring Data JPA 自動處理
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

    // 擴展審計欄位 - 由自定義的 AuditEntityListener 處理
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