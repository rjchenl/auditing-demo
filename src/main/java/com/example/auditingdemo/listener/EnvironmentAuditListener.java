package com.example.auditingdemo.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.audit.EnvironmentAuditableInterface;
import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.model.Environment;
import com.example.auditingdemo.service.TokenService;

import lombok.extern.slf4j.Slf4j;

/**
 * 環境實體審計監聽器
 * 專門處理環境相關的審計欄位
 * 標準審計欄位(創建者/修改者和時間)由Spring Data JPA的註解處理，本監聽器只處理環境特有的擴展審計欄位
 */
@Slf4j
@Component
@Configurable
public class EnvironmentAuditListener {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 執行審核操作並填充審核相關的擴展審計欄位
     * 
     * @param entity 環境審計介面
     * @param reviewStatus 審核狀態
     * @param reviewComment 審核評論
     */
    public void performReview(EnvironmentAuditableInterface entity, String reviewStatus, String reviewComment) {
        try {
            log.debug("執行審核操作並填充審計欄位: {} - 狀態: {}", entity.getClass().getSimpleName(), reviewStatus);
            
            // 獲取當前用戶
            String token = UserContext.getCurrentUser();
            String reviewerName = "系統";
            String reviewedCompany = "系統";
            String reviewedUnit = "系統";
            
            if (token != null && !token.isEmpty()) {
                try {
                    TokenService tokenService = applicationContext.getBean(TokenService.class);
                    var userInfo = tokenService.getUserInfoFromToken(token);
                    reviewerName = userInfo.get("name");
                    reviewedCompany = userInfo.get("company");
                    reviewedUnit = userInfo.get("unit");
                } catch (Exception e) {
                    log.error("從令牌獲取用戶信息時出錯: {}", e.getMessage(), e);
                }
            }
            
            // 設置審核相關欄位
            entity.setReviewerName(reviewerName);
            entity.setReviewStatus(reviewStatus);
            entity.setReviewComment(reviewComment);
            
            // 設置標準審核欄位
            String userId = token;
            if (token != null && !token.isEmpty()) {
                TokenService tokenService = applicationContext.getBean(TokenService.class);
                var userInfo = tokenService.getUserInfoFromToken(token);
                userId = userInfo.get("userId");
            }
            entity.setReviewedBy(userId);
            entity.setReviewedTime(java.time.LocalDateTime.now());
            
            // 設置審核者公司和部門
            entity.setReviewedCompany(reviewedCompany);
            entity.setReviewedUnit(reviewedUnit);
            
        } catch (Exception e) {
            log.error("執行審核操作時發生錯誤: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 執行部署操作並填充部署相關的擴展審計欄位
     * 
     * @param entity 環境審計介面
     * @param deployStatus 部署狀態
     * @param deployComment 部署評論
     */
    public void performDeploy(EnvironmentAuditableInterface entity, String deployStatus, String deployComment) {
        try {
            log.debug("執行部署操作並填充審計欄位: {} - 狀態: {}", entity.getClass().getSimpleName(), deployStatus);
            
            // 獲取當前用戶
            String token = UserContext.getCurrentUser();
            String deployerName = "系統";
            String deployedCompany = "系統";
            String deployedUnit = "系統";
            
            if (token != null && !token.isEmpty()) {
                try {
                    TokenService tokenService = applicationContext.getBean(TokenService.class);
                    var userInfo = tokenService.getUserInfoFromToken(token);
                    deployerName = userInfo.get("name");
                    deployedCompany = userInfo.get("company");
                    deployedUnit = userInfo.get("unit");
                } catch (Exception e) {
                    log.error("從令牌獲取用戶信息時出錯: {}", e.getMessage(), e);
                }
            }
            
            // 設置部署相關欄位
            entity.setDeployerName(deployerName);
            entity.setDeployStatus(deployStatus);
            entity.setDeployComment(deployComment);
            
            // 設置標準部署欄位
            String userId = token;
            if (token != null && !token.isEmpty()) {
                TokenService tokenService = applicationContext.getBean(TokenService.class);
                var userInfo = tokenService.getUserInfoFromToken(token);
                userId = userInfo.get("userId");
            }
            entity.setDeployedBy(userId);
            entity.setDeployedTime(java.time.LocalDateTime.now());
            
            // 設置部署者公司和部門
            entity.setDeployedCompany(deployedCompany);
            entity.setDeployedUnit(deployedUnit);
            
            // 設置狀態為已部署
            entity.setStatus(3); // 3: 已部署
            
        } catch (Exception e) {
            log.error("執行部署操作時發生錯誤: {}", e.getMessage(), e);
        }
    }
} 