package com.example.auditingdemo.model;

import com.example.auditingdemo.model.base.BaseAuditEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 顧客實體類
 * 使用Spring Data JPA標準審計功能
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pf_customer")
public class Customer extends BaseAuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String email;
    
    private String phone;
    
    private String address;
    
    private String company;
} 