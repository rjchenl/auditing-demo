package com.example.auditingdemo.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定義審計註解
 * 用於標記需要自動填充擴展審計欄位的實體類
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    /**
     * 創建公司欄位名稱
     */
    String createdCompanyField() default "createdCompany";
    
    /**
     * 創建部門欄位名稱
     */
    String createdUnitField() default "createdUnit";
    
    /**
     * 創建者姓名欄位名稱
     */
    String createdNameField() default "createdName";
    
    /**
     * 修改公司欄位名稱
     */
    String modifiedCompanyField() default "modifiedCompany";
    
    /**
     * 修改部門欄位名稱
     */
    String modifiedUnitField() default "modifiedUnit";
    
    /**
     * 修改者姓名欄位名稱
     */
    String modifiedNameField() default "modifiedName";
} 