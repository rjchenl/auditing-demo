package com.example.auditingdemo.audit;

import java.time.LocalDateTime;

/**
 * 用戶和API審計介面
 * 擴展基本審計介面，添加用戶相關的審計欄位
 * 用於User和API等實體
 */
public interface UserAuditableInterface extends AuditableInterface {
    
    // 用戶相關的擴展審計欄位
    String getCreatedName();
    void setCreatedName(String createdName);
    
    String getModifiedName();
    void setModifiedName(String modifiedName);
} 