package com.example.auditingdemo.listener;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.model.Environment;
import com.example.auditingdemo.service.TokenService;
import com.example.auditingdemo.audit.EnvironmentAuditable;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.lang.reflect.Field;

/**
 * 處理環境特殊審計欄位的監聽器
 * 用於填充 reviewed_by, reviewed_time, deployed_by, deployed_time 以及 version 欄位
 */
@Component
public class EnvironmentAuditListener {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentAuditListener.class);
    
    private static TokenService tokenService;
    
    @Autowired
    public void setTokenService(TokenService tokenService) {
        EnvironmentAuditListener.tokenService = tokenService;
    }
    
    /**
     * 實體創建後處理特殊環境審計欄位
     * 
     * @param entity 實體對象
     */
    @PostPersist
    public void postPersist(Object entity) {
        if (entity instanceof Environment) {
            // 直接調用針對特定實體的擴展欄位填充方法
            fillExtendedAuditFields((Environment) entity, true);
        } else {
            processAuditFields(entity, true);
        }
    }
    
    /**
     * 實體更新後處理特殊環境審計欄位
     * 
     * @param entity 實體對象
     */
    @PostUpdate
    public void postUpdate(Object entity) {
        if (entity instanceof Environment) {
            // 直接調用針對特定實體的擴展欄位填充方法
            fillExtendedAuditFields((Environment) entity, false);
        } else {
            processAuditFields(entity, false);
        }
    }
    
    /**
     * 處理環境審計欄位
     * 
     * @param entity 實體對象
     * @param isCreation 是否為創建操作
     */
    private void processAuditFields(Object entity, boolean isCreation) {
        // 檢查實體是否有環境審計註解
        Class<?> entityClass = entity.getClass();
        EnvironmentAuditable auditable = entityClass.getAnnotation(EnvironmentAuditable.class);
        
        if (auditable == null) {
            return;
        }
        
        try {
            // 獲取當前用戶信息
            String currentUser = UserContext.getCurrentUser();
            Map<String, String> userInfo = null;
            
            if (currentUser != null) {
                try {
                    userInfo = tokenService.getUserInfoFromToken(currentUser);
                } catch (Exception e) {
                    logger.error("獲取用戶信息時發生錯誤", e);
                }
            }
            
            String username = (userInfo != null) ? userInfo.get("name") : "System";
            
            // 只在新增時設置version為1.0
            if (isCreation) {
                setFieldValue(entity, auditable.versionFieldName(), "1.0");
            }
            
            // 模擬根據業務場景設置審核和部署信息
            // 在實際應用中，這些值應該來自特定業務操作而不是自動填充
            if (!isCreation && shouldSetReviewInfo()) {
                setFieldValue(entity, auditable.reviewedByFieldName(), username);
                setFieldValue(entity, auditable.reviewedTimeFieldName(), LocalDateTime.now());
            }
            
            if (!isCreation && shouldSetDeployInfo()) {
                setFieldValue(entity, auditable.deployedByFieldName(), username);
                setFieldValue(entity, auditable.deployedTimeFieldName(), LocalDateTime.now());
                
                // 部署時更新版本號
                String currentVersion = (String) getFieldValue(entity, auditable.versionFieldName());
                if (currentVersion != null) {
                    String newVersion = incrementVersion(currentVersion);
                    setFieldValue(entity, auditable.versionFieldName(), newVersion);
                }
            }
            
        } catch (Exception e) {
            logger.error("處理環境審計欄位時發生錯誤", e);
        }
    }
    
    /**
     * 模擬決定是否設置審核信息的邏輯
     * 在實際應用中，這個應該由業務操作決定
     */
    private boolean shouldSetReviewInfo() {
        // 示例：假設系統隨機決定是否需要設置審核信息
        return Math.random() > 0.5;
    }
    
    /**
     * 模擬決定是否設置部署信息的邏輯
     * 在實際應用中，這個應該由業務操作決定
     */
    private boolean shouldSetDeployInfo() {
        // 示例：假設系統隨機決定是否需要設置部署信息
        return Math.random() > 0.7;
    }
    
    /**
     * 遞增版本號
     * 
     * @param version 當前版本號
     * @return 遞增後的版本號
     */
    private String incrementVersion(String version) {
        try {
            String[] parts = version.split("\\.");
            if (parts.length >= 2) {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                minor++;
                return major + "." + minor;
            }
            return version + ".1";
        } catch (Exception e) {
            logger.error("遞增版本號時發生錯誤", e);
            return version;
        }
    }
    
    /**
     * 通過反射設置欄位值
     * 
     * @param entity 實體對象
     * @param fieldName 欄位名稱
     * @param value 欄位值
     */
    private void setFieldValue(Object entity, String fieldName, Object value) {
        try {
            Field field = findField(entity.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(entity, value);
            }
        } catch (Exception e) {
            logger.error("設置欄位 '{}' 的值時發生錯誤", fieldName, e);
        }
    }
    
    /**
     * 通過反射獲取欄位值
     * 
     * @param entity 實體對象
     * @param fieldName 欄位名稱
     * @return 欄位值
     */
    private Object getFieldValue(Object entity, String fieldName) {
        try {
            Field field = findField(entity.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(entity);
            }
        } catch (Exception e) {
            logger.error("獲取欄位 '{}' 的值時發生錯誤", fieldName, e);
        }
        return null;
    }
    
    /**
     * 在類層次結構中查找欄位
     * 
     * @param clazz 類
     * @param fieldName 欄位名稱
     * @return 欄位對象或null
     */
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
    
    /**
     * 填充擴展審計欄位
     */
    private void fillExtendedAuditFields(Environment environment, boolean isCreation) {
        try {
            String token = UserContext.getCurrentUser();
            Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
            
            if (userInfo != null) {
                if (isCreation) {
                    // 創建時填充創建者擴展資訊
                    environment.setCreatedCompany(userInfo.get("company"));
                    environment.setCreatedUnit(userInfo.get("unit"));
                    
                    // 在創建時也設置初始的修改者資訊，使其與創建者一致
                    environment.setModifiedCompany(userInfo.get("company"));
                    environment.setModifiedUnit(userInfo.get("unit"));
                } else {
                    // 僅在更新操作時設置修改者擴展資訊
                    environment.setModifiedCompany(userInfo.get("company"));
                    environment.setModifiedUnit(userInfo.get("unit"));
                }
            } else {
                // 找不到使用者資訊時，使用預設值
                setDefaultAuditInfo(environment, isCreation);
            }
        } catch (Exception e) {
            logger.error("填充擴展審計欄位時發生錯誤: {}", e.getMessage());
            setDefaultAuditInfo(environment, isCreation);
        }
    }
    
    /**
     * 填充審核相關欄位
     */
    private void fillReviewAuditFields(Environment environment) {
        try {
            String token = UserContext.getCurrentUser();
            Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
            
            if (userInfo != null) {
                environment.setReviewedBy(userInfo.get("userId"));
                environment.setReviewedTime(LocalDateTime.now());
                environment.setReviewedCompany(userInfo.get("company"));
                environment.setReviewedUnit(userInfo.get("unit"));
                
                // 在審核時更新修改者信息
                environment.setModifiedBy(userInfo.get("userId"));
                environment.setModifiedTime(LocalDateTime.now());
                environment.setModifiedCompany(userInfo.get("company"));
                environment.setModifiedUnit(userInfo.get("unit"));
            } else {
                // 找不到使用者資訊時，使用預設值
                environment.setReviewedBy("System");
                environment.setReviewedTime(LocalDateTime.now());
                environment.setReviewedCompany("系統");
                environment.setReviewedUnit("系統");
                
                // 在審核時更新修改者信息
                environment.setModifiedBy("System");
                environment.setModifiedTime(LocalDateTime.now());
                environment.setModifiedCompany("系統");
                environment.setModifiedUnit("系統");
            }
        } catch (Exception e) {
            logger.error("填充審核欄位時發生錯誤: {}", e.getMessage());
            environment.setReviewedBy("System");
            environment.setReviewedTime(LocalDateTime.now());
            environment.setReviewedCompany("系統");
            environment.setReviewedUnit("系統");
            
            // 在審核時更新修改者信息
            environment.setModifiedBy("System");
            environment.setModifiedTime(LocalDateTime.now());
            environment.setModifiedCompany("系統");
            environment.setModifiedUnit("系統");
        }
    }
    
    /**
     * 填充部署相關欄位
     */
    private void fillDeployAuditFields(Environment environment) {
        try {
            String token = UserContext.getCurrentUser();
            Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
            
            if (userInfo != null) {
                environment.setDeployedBy(userInfo.get("userId"));
                environment.setDeployedTime(LocalDateTime.now());
                environment.setDeployedCompany(userInfo.get("company"));
                environment.setDeployedUnit(userInfo.get("unit"));
                
                // 在部署時更新修改者信息
                environment.setModifiedBy(userInfo.get("userId"));
                environment.setModifiedTime(LocalDateTime.now());
                environment.setModifiedCompany(userInfo.get("company"));
                environment.setModifiedUnit(userInfo.get("unit"));
            } else {
                // 找不到使用者資訊時，使用預設值
                environment.setDeployedBy("System");
                environment.setDeployedTime(LocalDateTime.now());
                environment.setDeployedCompany("系統");
                environment.setDeployedUnit("系統");
                
                // 在部署時更新修改者信息
                environment.setModifiedBy("System");
                environment.setModifiedTime(LocalDateTime.now());
                environment.setModifiedCompany("系統");
                environment.setModifiedUnit("系統");
            }
        } catch (Exception e) {
            logger.error("填充部署欄位時發生錯誤: {}", e.getMessage());
            environment.setDeployedBy("System");
            environment.setDeployedTime(LocalDateTime.now());
            environment.setDeployedCompany("系統");
            environment.setDeployedUnit("系統");
            
            // 在部署時更新修改者信息
            environment.setModifiedBy("System");
            environment.setModifiedTime(LocalDateTime.now());
            environment.setModifiedCompany("系統");
            environment.setModifiedUnit("系統");
        }
    }
    
    /**
     * 設置預設的審計資訊
     */
    private void setDefaultAuditInfo(Environment environment, boolean isCreation) {
        if (isCreation) {
            // 使用系統默認值設定創建者擴展資訊
            environment.setCreatedCompany("System");
            environment.setCreatedUnit("System");
            
            // 在創建時也設置初始的修改者資訊，使其與創建者一致
            environment.setModifiedCompany("System");
            environment.setModifiedUnit("System");
        } else {
            // 僅在更新操作時設置修改者擴展資訊
            environment.setModifiedCompany("System");
            environment.setModifiedUnit("System");
        }
    }
    
    /**
     * 執行審核操作
     * 
     * @param environment 環境配置實體
     * @param token 用戶token
     */
    public void performReview(Environment environment, String token) {
        try {
            UserContext.setCurrentUser(token);
            
            // 設置修改相關審計欄位
            Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
            if (userInfo != null) {
                environment.setModifiedBy(userInfo.get("userId"));
                environment.setModifiedCompany(userInfo.get("company"));
                environment.setModifiedUnit(userInfo.get("unit"));
            } else {
                environment.setModifiedBy("System");
                environment.setModifiedCompany("系統");
                environment.setModifiedUnit("系統");
            }
            
            // 設置審核相關資訊
            fillReviewAuditFields(environment);
            environment.setStatus(2);  // 設置狀態為已審核
        } finally {
            UserContext.clear();
        }
    }
    
    /**
     * 執行部署操作
     * 
     * @param environment 環境配置實體
     * @param token 用戶token
     * @param newVersion 新版本號 (可選)
     */
    public void performDeploy(Environment environment, String token, String newVersion) {
        try {
            UserContext.setCurrentUser(token);
            
            // 設置修改相關審計欄位
            Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
            if (userInfo != null) {
                environment.setModifiedBy(userInfo.get("userId"));
                environment.setModifiedCompany(userInfo.get("company"));
                environment.setModifiedUnit(userInfo.get("unit"));
            } else {
                environment.setModifiedBy("System");
                environment.setModifiedCompany("系統");
                environment.setModifiedUnit("系統");
            }
            
            // 設置部署相關資訊
            fillDeployAuditFields(environment);
            
            // 如果提供了新版本號，則使用新版本號
            if (newVersion != null && !newVersion.trim().isEmpty()) {
                environment.setVersion(newVersion);
            } else {
                // 否則自動遞增版本號
                String currentVersion = environment.getVersion();
                environment.setVersion(incrementVersion(currentVersion));
            }
            
            environment.setStatus(3);  // 設置狀態為已部署
        } finally {
            UserContext.clear();
        }
    }
} 