package com.example.auditingdemo.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 標記實體類需要自動填充環境擴充審計欄位
 * 包含特殊的審計欄位如 reviewed_by, reviewed_time, deployed_by, deployed_time 以及 version
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnvironmentAuditable {

    /**
     * 審核者欄位名稱
     */
    String reviewedByFieldName() default "reviewedBy";

    /**
     * 審核時間欄位名稱
     */
    String reviewedTimeFieldName() default "reviewedTime";

    /**
     * 部署者欄位名稱
     */
    String deployedByFieldName() default "deployedBy";

    /**
     * 部署時間欄位名稱
     */
    String deployedTimeFieldName() default "deployedTime";

    /**
     * 版本欄位名稱
     */
    String versionFieldName() default "version";
} 