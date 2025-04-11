package com.example.auditingdemo.audit;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 嵌入式審計元數據類
 * 包含標準審計欄位（創建者、創建時間、修改者、修改時間）
 * 使用基本數據類型以避免外鍵約束問題
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditMetadata {
    /**
     * 創建者ID - 使用字符串類型避免外鍵約束
     */
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    /**
     * 創建時間
     */
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 最後修改者ID - 使用字符串類型避免外鍵約束
     */
    @LastModifiedBy
    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;

    /**
     * 最後修改時間
     */
    @LastModifiedDate
    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;
} 