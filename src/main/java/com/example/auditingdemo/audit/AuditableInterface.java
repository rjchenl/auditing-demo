package com.example.auditingdemo.audit;

import java.time.LocalDateTime;

/**
 * 審計介面
 * 用於實現擴展審計欄位的實體類
 */
public interface AuditableInterface {
    
    // 標準審計欄位 (由Spring Data JPA Auditing處理)
    String getCreatedBy();
    void setCreatedBy(String createdBy);
    
    LocalDateTime getCreatedTime();
    void setCreatedTime(LocalDateTime createdTime);
    
    String getModifiedBy();
    void setModifiedBy(String modifiedBy);
    
    LocalDateTime getModifiedTime();
    void setModifiedTime(LocalDateTime modifiedTime);
    
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