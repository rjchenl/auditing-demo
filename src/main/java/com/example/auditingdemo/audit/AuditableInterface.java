package com.example.auditingdemo.audit;

import java.time.LocalDateTime;

/**
 * 審計介面
 * 用於實現擴展審計欄位的實體類
 * 標準審計欄位由 Spring Data JPA 的 @CreatedBy, @CreatedDate, @LastModifiedBy, @LastModifiedDate 註解處理
 */
public interface AuditableInterface {
    
    // 移除標準審計欄位，將由 Spring Data JPA 的註解處理
    
    // 擴展審計欄位
    String getCreatedCompany();
    void setCreatedCompany(String createdCompany);
    
    String getCreatedUnit();
    void setCreatedUnit(String createdUnit);
    
    String getModifiedCompany();
    void setModifiedCompany(String modifiedCompany);
    
    String getModifiedUnit();
    void setModifiedUnit(String modifiedUnit);
} 