package com.example.auditingdemo.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.auditingdemo.audit.UserAuditableInterface;
import com.example.auditingdemo.listener.AuditEntityListener;
import com.example.auditingdemo.model.base.BaseAuditEntity;

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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用戶實體類
 * 演示審計功能
 */
@Entity
@Table(name = "pf_user")
@EntityListeners({AuditingEntityListener.class, AuditEntityListener.class})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditEntity implements UserAuditableInterface {
    
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
    
    // 實現 UserAuditableInterface 的方法
    @Override
    public String getCreatedName() {
        return super.getCreatedName();
    }
    
    @Override
    public void setCreatedName(String createdName) {
        super.setCreatedName(createdName);
    }
    
    @Override
    public String getModifiedName() {
        return super.getModifiedName();
    }
    
    @Override
    public void setModifiedName(String modifiedName) {
        super.setModifiedName(modifiedName);
    }
    
    @Override
    public String getCreatedCompany() {
        return super.getCreatedCompany();
    }
    
    @Override
    public void setCreatedCompany(String createdCompany) {
        super.setCreatedCompany(createdCompany);
    }
    
    @Override
    public String getCreatedUnit() {
        return super.getCreatedUnit();
    }
    
    @Override
    public void setCreatedUnit(String createdUnit) {
        super.setCreatedUnit(createdUnit);
    }
    
    @Override
    public String getModifiedCompany() {
        return super.getModifiedCompany();
    }
    
    @Override
    public void setModifiedCompany(String modifiedCompany) {
        super.setModifiedCompany(modifiedCompany);
    }
    
    @Override
    public String getModifiedUnit() {
        return super.getModifiedUnit();
    }
    
    @Override
    public void setModifiedUnit(String modifiedUnit) {
        super.setModifiedUnit(modifiedUnit);
    }
} 