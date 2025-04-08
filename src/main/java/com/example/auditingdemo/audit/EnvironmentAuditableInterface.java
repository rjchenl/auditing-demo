package com.example.auditingdemo.audit;

import java.time.LocalDateTime;

/**
 * 環境配置特有的審計介面
 * 擴展基本審計介面，添加環境特有的審計欄位
 */
public interface EnvironmentAuditableInterface extends AuditableInterface {
    
    // 環境配置特有審計欄位
    String getReviewedBy();
    void setReviewedBy(String reviewedBy);
    
    LocalDateTime getReviewedTime();
    void setReviewedTime(LocalDateTime reviewedTime);
    
    String getReviewedCompany();
    void setReviewedCompany(String reviewedCompany);
    
    String getReviewedUnit();
    void setReviewedUnit(String reviewedUnit);
    
    String getDeployedBy();
    void setDeployedBy(String deployedBy);
    
    LocalDateTime getDeployedTime();
    void setDeployedTime(LocalDateTime deployedTime);
    
    String getDeployedCompany();
    void setDeployedCompany(String deployedCompany);
    
    String getDeployedUnit();
    void setDeployedUnit(String deployedUnit);
    
    String getVersion();
    void setVersion(String version);
    
    Integer getStatus();
    void setStatus(Integer status);
    
    // 新增的審核相關欄位
    String getReviewerName();
    void setReviewerName(String reviewerName);
    
    String getReviewStatus();
    void setReviewStatus(String reviewStatus);
    
    String getReviewComment();
    void setReviewComment(String reviewComment);
    
    // 新增的部署相關欄位
    String getDeployerName();
    void setDeployerName(String deployerName);
    
    String getDeployStatus();
    void setDeployStatus(String deployStatus);
    
    String getDeployComment();
    void setDeployComment(String deployComment);
} 