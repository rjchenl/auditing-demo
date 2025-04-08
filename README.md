# Spring Boot JPA 審計功能示範

這個項目是Spring Data JPA審計功能的示範應用，專注於實現產品化的審計欄位設計，包含標準審計欄位與擴展審計欄位。

## 功能概述

- 基本審計功能：記錄創建者/修改者ID及時間
- 擴展審計功能：記錄創建者/修改者的詳細信息（公司、部門、姓名）
- 環境配置審計功能：除基本審計外，還包含審核與部署相關的審計欄位

## 最近更新

### 混合式審計架構

我們最近對審計功能進行了重大升級，採用混合式的審計架構：

1. **標準審計欄位**：使用 Spring Data JPA 的註解進行實現
   - `@CreatedBy`：自動填充創建者ID
   - `@CreatedDate`：自動填充創建時間
   - `@LastModifiedBy`：自動填充修改者ID
   - `@LastModifiedDate`：自動填充修改時間

2. **擴展審計欄位**：使用介面架構實現
   - `AuditableInterface`：基礎審計介面，包含擴展審計欄位（如公司、部門信息）
   - `UserAuditableInterface`：擴展自基礎介面，添加用戶相關審計欄位（如姓名）
   - `EnvironmentAuditableInterface`：擴展自基礎介面，添加環境特有的審計欄位（如審核、部署相關欄位）

3. **專用監聽器**：
   - `AuditEntityListener`：處理擴展審計欄位（公司、部門）
   - `EnvironmentAuditListener`：處理環境特有審計欄位（審核、部署）

4. **特別流程支援**：
   - 添加了審核流程：`performReview` 方法填充審核相關欄位
   - 添加了部署流程：`performDeploy` 方法填充部署相關欄位

### Demo API 更新

1. **User API**:
   - POST `/api/users` - 創建用戶（展示基本審計欄位+用戶擴展審計欄位）
   - PUT `/api/users/{id}` - 更新用戶（展示審計欄位變化）
   - GET `/api/users/audit` - 查看用戶審計信息

2. **API管理 API**:
   - POST `/api/apis` - 創建API記錄（展示與User相同的審計模式）
   - PUT `/api/apis/{id}` - 更新API記錄
   
3. **Environment API**:
   - POST `/api/environments` - 創建環境配置
   - PUT `/api/environments/{id}` - 更新環境配置
   - POST `/api/environments/{id}/review` - 審核環境配置（展示特殊審計欄位）
   - POST `/api/environments/{id}/deploy` - 部署環境配置（展示特殊審計欄位）
   - GET `/api/environments/audit-fields` - 獲取環境審計欄位
   - GET `/api/environments/{id}/audit-fields` - 獲取特定環境審計欄位

## 技術棧

- Spring Boot 3.4.4
- Spring Data JPA
- PostgreSQL
- Maven
- Docker & Docker Compose

## 層次化審計介面架構

### 介面結構

```
AuditableInterface (擴展審計介面)
├── UserAuditableInterface (用戶審計介面)
└── EnvironmentAuditableInterface (環境審計介面)
```

### 1. AuditableInterface

基礎審計介面，包含擴展審計欄位：

```java
public interface AuditableInterface {
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
```

### 2. UserAuditableInterface

擴展自 `AuditableInterface`，添加用戶相關審計欄位：

```java
public interface UserAuditableInterface extends AuditableInterface {
    // 用戶相關的擴展審計欄位
    String getCreatedName();
    void setCreatedName(String createdName);
    String getModifiedName();
    void setModifiedName(String modifiedName);
}
```

### 3. EnvironmentAuditableInterface

擴展自 `AuditableInterface`，添加環境配置特有的審計欄位：

```java
public interface EnvironmentAuditableInterface extends AuditableInterface {
    // 審核相關欄位
    String getReviewedBy();
    void setReviewedBy(String reviewedBy);
    LocalDateTime getReviewedTime();
    void setReviewedTime(LocalDateTime reviewedTime);
    // ... 其他審核欄位
    
    // 部署相關欄位
    String getDeployedBy();
    void setDeployedBy(String deployedBy);
    LocalDateTime getDeployedTime();
    void setDeployedTime(LocalDateTime deployedTime);
    // ... 其他部署欄位
    
    // 環境特有欄位
    String getVersion();
    void setVersion(String version);
    Integer getStatus();
    void setStatus(Integer status);
}
```

### 監聽器實現

#### 1. AuditEntityListener

處理擴展審計欄位：

```java
@PrePersist
public void prePersist(Object entity) {
    if (entity instanceof AuditableInterface) {
        log.debug("實體創建前填充擴展審計欄位: {}", entity.getClass().getSimpleName());
        processAuditFieldsWithInterface((AuditableInterface) entity, true);
        
        // 處理特定於用戶的審計欄位
        if (entity instanceof UserAuditableInterface) {
            processUserAuditFields((UserAuditableInterface) entity, true);
        }
    }
}
```

#### 2. EnvironmentAuditListener

環境特有審計處理：

```java
public void performReview(EnvironmentAuditableInterface entity, String reviewStatus, String reviewComment) {
    // 獲取當前用戶
    String token = UserContext.getCurrentUser();
    String reviewerName = "系統";
    
    if (token != null && !token.isEmpty()) {
        TokenService tokenService = applicationContext.getBean(TokenService.class);
        var userInfo = tokenService.getUserInfoFromToken(token);
        reviewerName = userInfo.get("name");
    }
    
    // 設置審核相關欄位
    entity.setReviewerName(reviewerName);
    entity.setReviewStatus(reviewStatus);
    entity.setReviewComment(reviewComment);
    entity.setReviewedBy(token);
    entity.setReviewedTime(LocalDateTime.now());
}
```

## JWT令牌與用戶資訊的對應機制

本示範專案使用簡化的JWT處理方式，通過`TokenService`類來模擬真實JWT的處理流程：

1. **令牌來源**：
   - 在HTTP請求頭`Authorization`中提供用戶ID，例如「kenbai」、「peter」或「shawn」
   - 系統使用這個用戶ID作為簡化的「JWT令牌」

2. **用戶資訊映射**：
   - `TokenService`類中維護一個靜態映射表(Map)，存儲以下用戶的詳細資訊：
     - **kenbai**：肯白，拓連科技，行銷部
     - **peter**：彼得，拓連科技，研發部
     - **shawn**：肖恩，拓連科技，產品部
     - **system**：系統，系統，系統

3. **令牌處理流程**：
   1. `UserTokenInterceptor`從請求的`Authorization`頭提取令牌值
   2. 將令牌存儲到`UserContext`的ThreadLocal變量中
   3. 在實體持久化過程中，`CustomAuditorAware`從ThreadLocal獲取令牌
   4. `CustomAuditorAware`通過`TokenService`解析令牌獲取用戶ID
   5. 該用戶ID被Spring Data JPA用於填充標準審計欄位
   6. `AuditEntityListener`使用同一令牌查詢用戶詳細資訊
   7. 這些詳細資訊被用於填充擴展審計欄位

4. **複雜審計（環境特有）**：
   - 環境配置有特殊的審計需求，由`EnvironmentAuditListener`處理
   - 審核和部署操作會使用各自的操作者令牌更新專有審計欄位

## 核心實現

本示範使用Spring Data JPA Auditing和自定義介面混合實現審計功能：

1. **標準審計欄位**：使用Spring Data JPA Auditing實現
   - **@EnableJpaAuditing**: 開啟Spring Data JPA審計功能
   - **@CreatedBy, @LastModifiedBy**: 自動填充創建和修改者ID
   - **@CreatedDate, @LastModifiedDate**: 自動填充創建和修改時間
   - **AuditorAware**: 提供當前用戶ID

2. **擴展審計欄位**：使用介面方式實現
   - **AuditableInterface**: 定義基礎擴展審計欄位（公司、部門等）
   - **EntityListeners**: 使用實體監聽器填充擴展審計欄位
   - **自定義監聽器**: 負責填充不同類型的擴展審計欄位

## 快速開始

### 前提條件

- Docker和Docker Compose已安裝
- Java 21或更高版本
- Maven

### 啟動服務

1. 啟動PostgreSQL數據庫：

```bash
docker-compose up -d
```

2. 構建並運行應用：

```bash
./mvnw spring-boot:run
```

## 功能演示

本示範項目包含三種不同類型的實體，用於展示不同級別的審計功能：

### 1. 用戶管理 (User)

用戶管理功能展示基本的審計欄位使用，包括創建者、修改者以及他們的組織信息。

#### 創建用戶

```bash
# 使用kenbai身份創建用戶
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer kenbai" \
  -d '{
    "name": "測試用戶",
    "description": "測試描述",
    "email": "test@example.com",
    "username": "testuser",
    "password": "password123",
    "statusId": "1"
  }'
```

**審計欄位變化**：
- `created_by` = "kenbai" (JWT令牌中的用戶ID)
- `created_time` = 當前時間
- `modified_by` = "kenbai" (初始化與創建者相同)
- `modified_time` = 當前時間
- `created_company` = "拓連科技" (kenbai的公司)
- `created_unit` = "行銷部" (kenbai的部門)
- `created_name` = "肯白" (kenbai的姓名)
- `modified_company` = "拓連科技" (初始與created_company相同)
- `modified_unit` = "行銷部" (初始與created_unit相同)
- `modified_name` = "肯白" (初始與created_name相同)

#### 更新用戶

```bash
# 使用peter身份更新用戶
curl -X PUT http://localhost:8080/api/users/{user_id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer peter" \
  -d '{
    "name": "已更新的用戶",
    "description": "已更新的描述",
    "email": "updated@example.com"
  }'
```

**審計欄位變化**：
- `created_by` = 不變 (保持原始創建者記錄)
- `created_time` = 不變 (保持原始創建時間)
- `created_company` = 不變 (保持原始公司記錄)
- `created_unit` = 不變 (保持原始部門記錄)
- `created_name` = 不變 (保持原始姓名記錄)
- `modified_by` = "peter" (更新為當前JWT令牌中的用戶ID)
- `modified_time` = 當前時間 (更新為操作時間)
- `modified_company` = "拓連科技" (peter的公司)
- `modified_unit` = "研發部" (peter的部門)
- `modified_name` = "彼得" (peter的姓名)

#### 查看用戶審計信息

```bash
curl http://localhost:8080/api/users/audit
```

### 2. API管理 (Api)

API管理功能與用戶管理類似，共用相同的擴展審計欄位結構。API實體包含類似的審計欄位。

#### 創建API

```bash
curl -X POST http://localhost:8080/api/apis \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer kenbai" \
  -d '{
    "apiname": "test-api",
    "description": "測試API說明"
  }'
```

**審計欄位變化**：
- `created_by` = "kenbai" (JWT令牌中的用戶ID)
- `created_time` = 當前時間
- `modified_by` = "kenbai" (初始化與創建者相同)
- `modified_time` = 當前時間
- `created_company` = "拓連科技" (kenbai的公司)
- `created_unit` = "行銷部" (kenbai的部門)
- `created_name` = "肯白" (kenbai的姓名)
- `modified_company` = "拓連科技" (初始與created_company相同)
- `modified_unit` = "行銷部" (初始與created_unit相同)
- `modified_name` = "肯白" (初始與created_name相同)

#### 更新API

```bash
curl -X PUT http://localhost:8080/api/apis/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer peter" \
  -d '{
    "description": "已更新的API說明"
  }'
```

**審計欄位變化**：
- `created_by` = 不變 (保持原始創建者記錄)
- `created_time` = 不變 (保持原始創建時間)
- `created_company` = 不變 (保持原始公司記錄)
- `created_unit` = 不變 (保持原始部門記錄)
- `created_name` = 不變 (保持原始姓名記錄)
- `modified_by` = "peter" (更新為當前JWT令牌中的用戶ID)
- `modified_time` = 當前時間 (更新為操作時間)
- `modified_company` = "拓連科技" (peter的公司)
- `modified_unit` = "研發部" (peter的部門)
- `modified_name` = "彼得" (peter的姓名)

### 3. 環境配置管理 (Environment)

環境配置管理功能展示了更複雜的審計流程，除了基本的創建/修改審計外，還包含了審核與部署等特定業務流程的審計。與User和Api不同，Environment有特殊的審計欄位組。

#### 創建環境配置

```bash
curl -X POST http://localhost:8080/api/environments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer kenbai" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test.example.com\",\"port\":8080}",
    "status": 0
  }'
```

**審計欄位變化**：
- `created_by` = "kenbai" (JWT令牌中的用戶ID)
- `created_time` = 當前時間
- `modified_by` = "kenbai" (初始化與創建者相同)
- `modified_time` = 當前時間
- `created_company` = "拓連科技" (kenbai的公司)
- `created_unit` = "行銷部" (kenbai的部門)
- `modified_company` = "拓連科技" (初始與created_company相同)
- `modified_unit` = "行銷部" (初始與created_unit相同)
- `reviewed_by` = null (尚未審核)
- `reviewed_time` = null (尚未審核)
- `reviewed_company` = null (尚未審核)
- `reviewed_unit` = null (尚未審核)
- `deployed_by` = null (尚未部署)
- `deployed_time` = null (尚未部署)
- `deployed_company` = null (尚未部署)
- `deployed_unit` = null (尚未部署)
- `status` = 0 (草稿狀態)
- `version` = null (尚未設定版本)

#### 更新環境配置

```bash
curl -X PUT http://localhost:8080/api/environments/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer kenbai" \
  -d '{
    "description": "已更新的環境配置",
    "configValue": "{\"server\":\"updated.example.com\",\"port\":8088}"
  }'
```

**審計欄位變化**：
- `created_by` = 不變 (保持原始創建者記錄)
- `created_time` = 不變 (保持原始創建時間)
- `created_company` = 不變 (保持原始公司記錄)
- `created_unit` = 不變 (保持原始部門記錄)
- `modified_by` = "kenbai" (更新為當前JWT令牌中的用戶ID)
- `modified_time` = 當前時間 (更新為操作時間)
- `modified_company` = "拓連科技" (kenbai的公司)
- `modified_unit` = "行銷部" (kenbai的部門)
- 其他特殊審計欄位保持不變 (reviewed_*, deployed_*)

#### 審核環境配置

```bash
curl -X POST http://localhost:8080/api/environments/{id}/review \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer peter"
```

**審計欄位變化**：
- `created_by` = 不變 (保持原始創建者記錄)
- `created_time` = 不變 (保持原始創建時間)
- `created_company` = 不變 (保持原始公司記錄)
- `created_unit` = 不變 (保持原始部門記錄)
- `modified_by` = "peter" (更新為當前用戶)
- `modified_time` = 當前時間 (更新為操作時間)
- `modified_company` = "拓連科技" (peter的公司)
- `modified_unit` = "研發部" (peter的部門)
- `reviewed_by` = "peter" (審核人員ID)
- `reviewed_time` = 當前時間 (審核時間)
- `reviewerName` = "彼得" (peter的姓名)
- `reviewStatus` = "已審核" (審核狀態)
- `reviewComment` = "已完成審核" (審核評論)
- `deployed_by` = null (尚未部署)
- `deployed_time` = null (尚未部署)

#### 部署環境配置

```bash
curl -X POST http://localhost:8080/api/environments/{id}/deploy \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer shawn" \
  -d '{
    "version": "1.0.1"
  }'
```

**審計欄位變化**：
- `created_by` = 不變 (保持原始創建者記錄)
- `created_time` = 不變 (保持原始創建時間)
- `created_company` = 不變 (保持原始公司記錄)
- `created_unit` = 不變 (保持原始部門記錄)
- `modified_by` = "shawn" (更新為當前用戶)
- `modified_time` = 當前時間 (更新為操作時間)
- `modified_company` = "拓連科技" (shawn的公司)
- `modified_unit` = "產品部" (shawn的部門)
- `reviewed_by` = 不變 (保持審核人員記錄)
- `reviewed_time` = 不變 (保持審核時間記錄)
- `deployed_by` = "shawn" (部署人員ID)
- `deployed_time` = 當前時間 (部署時間)
- `deployerName` = "肖恩" (shawn的姓名)
- `deployStatus` = "已部署" (部署狀態)
- `deployComment` = "已完成部署" (部署評論)
- `status` = 3 (已部署狀態)
- `version` = "1.0.1" (請求中指定的版本號)

#### 獲取待部署環境配置列表

```bash
curl http://localhost:8080/api/environments/pending-deploy
```

## 審計流程說明

### 基本審計流程

1. **獲取操作者ID**：從HTTP請求頭`Authorization`中獲取操作者ID（在真實系統中通常從JWT令牌中提取）
2. **存儲當前用戶**：通過`UserContext` (ThreadLocal)保存當前操作者ID
3. **自動填充**：Spring Data JPA通過`AuditorAware`獲取操作者ID並自動填充標準審計欄位
4. **擴展填充**：實體監聽器使用TokenService查詢操作者詳細信息並填充擴展審計欄位

### 環境配置特定審計流程

環境配置實體(Environment)展示了更複雜的審計場景，包括：

1. **標準審計**：記錄創建者和修改者（與User實體相同）
2. **審核審計**：記錄審核者ID、審核時間以及審核者的組織信息
3. **部署審計**：記錄部署者ID、部署時間、版本號以及部署者的組織信息

## 數據庫詳情

PostgreSQL連接信息：
- 主機：localhost
- 端口：5432
- 數據庫：auditing
- 用戶名：postgres
- 密碼：postgres

### 主要表結構

1. **pf_user表**：用戶信息，包含標準審計欄位
   - 標準審計欄位：`created_by`, `created_time`, `modified_by`, `modified_time`
   - 擴展審計欄位：`created_company`, `created_unit`, `created_name`, `modified_company`, `modified_unit`, `modified_name`

2. **pf_api表**：API信息，包含與pf_user相同的審計欄位結構
   - 標準審計欄位：與pf_user相同
   - 擴展審計欄位：與pf_user相同

3. **pf_environment表**：環境配置，包含標準審計欄位及特定業務審計欄位
   - 標準審計欄位：與pf_user相同，但不含name欄位
   - 特定擴展審計欄位：`created_company`, `created_unit`, `modified_company`, `modified_unit`
   - 特定業務審計欄位：`reviewed_by`, `reviewed_time`, `reviewed_company`, `reviewed_unit`, `deployed_by`, `deployed_time`, `deployed_company`, `deployed_unit`
   - 環境特有審計欄位：`reviewer_name`, `review_status`, `review_comment`, `deployer_name`, `deploy_status`, `deploy_comment`

## 核心類說明

1. **TokenService**: 模擬JWT服務，提供用戶ID與詳細資訊的映射
2. **AuditEntityListener**: 通用審計監聽器，負責填充擴展審計欄位
3. **EnvironmentAuditListener**: 環境專用審計監聽器，處理審核和部署審計
4. **CustomAuditorAware**: 提供當前操作者ID給Spring JPA
5. **AuditableInterface**: 基礎審計介面
6. **UserAuditableInterface**: 用戶擴展審計介面
7. **EnvironmentAuditableInterface**: 環境配置特有審計介面

通過以上步驟，您可以全面了解系統的審計功能及其在不同業務場景中的應用。
 