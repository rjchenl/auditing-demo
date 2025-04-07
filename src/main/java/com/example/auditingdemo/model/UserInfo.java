package com.example.auditingdemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 用戶信息實體類
 * 用於存儲用戶的詳細信息，如公司、部門、姓名等
 */
@Entity
@Table(name = "pf_user_info")
@Data
public class UserInfo {
    
    @Id
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "company")
    private String company;
    
    @Column(name = "unit")
    private String unit;
    
    @Column(name = "name")
    private String name;
    
    // 明確加入setter和getter方法
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }
} 