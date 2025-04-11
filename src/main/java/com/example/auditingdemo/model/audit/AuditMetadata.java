package com.example.auditingdemo.model.audit;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.example.auditingdemo.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class AuditMetadata {
    
    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;
    
    @CreatedDate
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "modified_by")
    @JsonIgnore
    private User modifiedBy;
    
    @LastModifiedDate
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;
    
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