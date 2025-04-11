package com.example.auditingdemo.model.base;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.auditingdemo.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditBase {
    
    // 標準審計欄位
    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @JsonIgnore
    private User createdBy;
    
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = false)
    @JsonIgnore
    private User modifiedBy;
    
    @LastModifiedDate
    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;
    
    // 擴展審計欄位
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