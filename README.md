# Spring Boot JPA 審計功能實作指南

本專案提供一個完整的 Spring Data JPA 審計功能實作範例，作為開發人員的參考模板。專案實現了標準審計欄位與擴展審計欄位的混合架構，適用於各種複雜度的業務場景。

## 目錄

1. [功能特色](#功能特色)
2. [審計架構設計](#審計架構設計)
3. [核心組件介紹](#核心組件介紹)
4. [審計欄位實作差別](#審計欄位實作差別)
5. [審計介面階層關係](#審計介面階層關係)
6. [實體模型說明](#實體模型說明)
7. [功能演示](#功能演示)
8. [擴展與客製化](#擴展與客製化)
9. [快速開始](#快速開始)
10. [設計圖表](#設計圖表)

## 功能特色

- **混合式審計架構**：結合 Spring Data JPA 標準審計功能與自訂介面擴展
- **層次化審計介面**：使用介面繼承提供彈性的擴展結構
- **業務特定審計**：支援複雜業務場景的特殊審計需求（如審核、部署流程）
- **可擴展設計**：易於根據業務需求進行擴展和客製化
- **多種實作方式**：提供基於字符串ID和實體關聯兩種審計實現方式

## 審計架構設計

本專案使用混合式審計架構，結合 Spring Data JPA 的標準審計功能與自訂介面擴展：

![審計架構](https://example.com/audit-architecture.png)

### 設計圖表

為了更清晰地說明審計系統的設計，我們提供了一系列的 PlantUML 圖表：

- **[審計系統架構概覽](docs/diagrams/audit-architecture.puml)** - 展示審計系統的整體架構與主要組件
- **[審計欄位處理流程](docs/diagrams/audit-process.puml)** - 詳細說明審計欄位從請求到持久化的完整流程
- **[審計類圖](docs/diagrams/audit-entities.puml)** - 展示審計相關的類和接口關係
- **[審計欄位關系圖](docs/diagrams/audit-fields.puml)** - 詳細展示各類審計欄位的分組和從屬關係
- **[審計元件交互圖](docs/diagrams/audit-component.puml)** - 說明審計系統各組件之間的交互關係

您可以在[設計圖表目錄](docs/diagrams/)中找到更詳細的說明。

## 審計欄位實作差別

本專案示範了三種不同的審計欄位實作方式，每種方式適用於不同的需求場景：

### 1. 標準審計欄位 (`@CreatedBy`, `@CreatedDate` 等)

Spring Data JPA 內建的審計功能，通過註解輕鬆實現基本審計：

```java
@CreatedBy
@Column(name = "created_by", nullable = false, updatable = false)
private String createdBy;

@CreatedDate
@Column(name = "created_time", nullable = false, updatable = false)
private LocalDateTime createdTime;

@LastModifiedBy
@Column(name = "modified_by", nullable = false)
private String modifiedBy;

@LastModifiedDate
@Column(name = "modified_time", nullable = false)
private LocalDateTime modifiedTime;
```

**實作方式**：
1. 在實體類加上 `@EntityListeners(AuditingEntityListener.class)` 註解
2. 為審計欄位加上對應的 `@CreatedBy`, `@CreatedDate` 等註解
3. 配置 `@EnableJpaAuditing` 和 `AuditorAware<String>` 以提供當前用戶

**優點**：
- 簡單易用，配置少
- Spring 原生支持，無需額外代碼
- 自動填充，無需手動設置

**適用場景**：
- 基本的審計需求
- 只需記錄操作者ID和時間
- 追求簡潔的實現方式

### 2. 擴展審計欄位 (通過介面和監聽器)

通過自定義介面和實體監聽器，實現比標準審計更豐富的欄位：

```java
public interface AuditableInterface {
    String getCreatedCompany();
    void setCreatedCompany(String createdCompany);
    String getCreatedUnit();
    void setCreatedUnit(String createdUnit);
    // ... 更多擴展欄位
}
```

**實作方式**：
1. 定義審計介面（如 `AuditableInterface`）
2. 實體類實現該介面並添加對應欄位
3. 創建 `EntityListener` 監聽器在 `@PrePersist` 和 `@PreUpdate` 時處理這些欄位
4. 在實體類添加 `@EntityListeners(AuditEntityListener.class)` 註解

**優點**：
- 可添加比標準審計更多的自定義欄位
- 可實現更複雜的業務邏輯
- 仍然保持自動化處理
- 可通過介面繼承實現分層次的審計需求

**適用場景**：
- 需要記錄更多審計信息（如部門、公司等）
- 有特定業務場景的審計需求
- 需要根據不同實體類型記錄不同審計信息

### 3. 實體關聯審計 (使用 `AuditorAware<User>`)

不僅存儲用戶ID，而是通過外鍵關聯到完整用戶實體：

```java
@ManyToOne
@JoinColumn(name = "created_by_id", nullable = false, updatable = false)
private User createdByUser;

@ManyToOne
@JoinColumn(name = "last_modified_by_id", nullable = false)
private User lastModifiedByUser;
```

**實作方式**：
1. 配置 `AuditorAware<User>` 返回完整用戶實體
2. 在實體類使用 `@ManyToOne` 和 `@JoinColumn` 建立關聯
3. 使用外鍵約束確保數據完整性

**優點**：
- 完整的數據關聯，可獲取用戶的所有信息
- 通過外鍵保證數據完整性
- 便於關聯查詢
- 業務語義更清晰

**適用場景**：
- 需要完整用戶信息的場景
- 需要強關聯和數據完整性的場景
- 複雜的報表和分析需求

## 審計介面階層關係

本專案設計了層次化的審計介面結構，通過介面繼承實現不同層級的審計需求。這種階層設計有助於實現代碼的高內聚低耦合，並能根據業務需求靈活組合不同的審計能力。

### 介面階層圖

```
AuditableInterface (基礎審計介面)
├── UserAuditableInterface (用戶審計介面)
└── EnvironmentAuditableInterface (環境審計介面)
```

### 各層介面說明

#### AuditableInterface (基礎審計介面)

基礎審計介面定義了所有審計實體共同需要的擴展審計欄位，作為整個審計體系的基礎。

```java
public interface AuditableInterface {
    // 組織審計欄位
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

**主要職責**：
- 提供基本的擴展審計欄位（如公司、部門信息）
- 作為其他專用審計介面的基礎
- 與 Spring Data JPA 標準審計功能配合使用

#### UserAuditableInterface (用戶審計介面)

用戶審計介面繼承自基礎審計介面，並添加了用戶特有的審計欄位，適用於與用戶相關的實體。

```java
public interface UserAuditableInterface extends AuditableInterface {
    // 用戶特有審計欄位
    String getCreatedName();
    void setCreatedName(String createdName);
    String getModifiedName();
    void setModifiedName(String modifiedName);
}
```

**主要職責**：
- 繼承基礎審計欄位
- 添加用戶相關的擴展欄位（如用戶姓名）
- 用於需要記錄詳細用戶信息的實體（如 User, Api 等）

#### EnvironmentAuditableInterface (環境審計介面)

環境審計介面也繼承自基礎審計介面，但添加了環境配置特有的審計欄位，如審核和部署相關信息。

```java
public interface EnvironmentAuditableInterface extends AuditableInterface {
    // 審核相關欄位
    String getReviewedBy();
    void setReviewedBy(String reviewedBy);
    LocalDateTime getReviewedTime();
    void setReviewedTime(LocalDateTime reviewedTime);
    String getReviewedCompany();
    void setReviewedCompany(String reviewedCompany);
    String getReviewedUnit();
    void setReviewedUnit(String reviewedUnit);
    
    // 審核者信息
    String getReviewerName();
    void setReviewerName(String reviewerName);
    String getReviewStatus();
    void setReviewStatus(String reviewStatus);
    String getReviewComment();
    void setReviewComment(String reviewComment);
    
    // 部署相關欄位
    String getDeployedBy();
    void setDeployedBy(String deployedBy);
    LocalDateTime getDeployedTime();
    void setDeployedTime(LocalDateTime deployedTime);
    String getDeployedCompany();
    void setDeployedCompany(String deployedCompany);
    String getDeployedUnit();
    void setDeployedUnit(String deployedUnit);
    
    // 部署者信息
    String getDeployerName();
    void setDeployerName(String deployerName);
    String getDeployStatus();
    void setDeployStatus(String deployStatus);
    String getDeployComment();
    void setDeployComment(String deployComment);
    
    // 其他管理欄位
    String getVersion();
    void setVersion(String version);
    Integer getStatus();
    void setStatus(Integer status);
}
```

**主要職責**：
- 繼承基礎審計欄位
- 添加審核和部署流程相關的欄位
- 用於需要進行狀態管理和流程追蹤的實體（如 Environment）

### 介面實現示例

介面的實現方式示例：

```java
@Entity
@Table(name = "pf_user")
@EntityListeners({AuditingEntityListener.class, AuditEntityListener.class})
public class User implements UserAuditableInterface {
    // 實體欄位...
    
    // 標準審計欄位實現
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    // UserAuditableInterface 介面實現
    @Column(name = "created_name")
    private String createdName;
    
    // 其他實現...
    
    @Override
    public String getCreatedName() {
        return this.createdName;
    }
    
    @Override
    public void setCreatedName(String createdName) {
        this.createdName = createdName;
    }
    
    // 其他方法實現...
}
```

### 介面監聽器

本專案為介面實現設計了對應的監聽器，負責自動填充擴展審計欄位。

#### AuditEntityListener (通用審計監聽器)

處理所有 `AuditableInterface` 及其子介面的通用審計欄位。

```java
@Component
@Configurable
public class AuditEntityListener {
    
    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof AuditableInterface) {
            processAuditFieldsWithInterface((AuditableInterface) entity, true);
            
            if (entity instanceof UserAuditableInterface) {
                processUserAuditFields((UserAuditableInterface) entity, true);
            }
        }
    }
    
    @PreUpdate
    public void preUpdate(Object entity) {
        // 處理更新事件
    }
    
    // 處理方法...
}
```

#### EnvironmentAuditListener (環境審計監聽器)

專門處理環境實體的審核和部署相關欄位。

```java
@Component
@Configurable
public class EnvironmentAuditListener {
    
    public void performReview(EnvironmentAuditableInterface entity, String reviewStatus, String reviewComment) {
        // 設置審核相關欄位
    }
    
    public void performDeploy(EnvironmentAuditableInterface entity, String deployStatus, String deployComment) {
        // 設置部署相關欄位
    }
}
```

### 介面階層設計的優勢

1. **模組化**：各個介面負責特定領域的審計需求，職責單一明確
2. **可組合性**：可以根據需要組合不同的審計介面
3. **代碼複用**：通過介面繼承減少重複代碼
4. **擴展性**：可以方便地添加新的審計介面或擴展現有介面
5. **類型安全**：利用 Java 的類型系統確保正確實現所有必要的審計方法

### 如何選擇合適的審計介面

選擇合適的審計介面應根據實體的業務需求：

- 一般實體：實現 `AuditableInterface`
- 用戶相關實體：實現 `UserAuditableInterface`
- 需要審核流程的實體：實現 `EnvironmentAuditableInterface`

## 實體模型說明

本專案包含三種實體模型，展示不同程度的審計需求：

1. **User 實體**：基本審計 + 用戶擴展審計
2. **Api 實體**：基本審計 + 用戶擴展審計
3. **Environment 實體**：基本審計 + 環境特有審計 (包含審核與部署流程)
4. **ComplexAudit 實體**：實體關聯式審計示例

### 標準審計欄位

| 欄位名 | 說明 | 類型 |
|--------|------|------|
| created_by | 創建者ID | String |
| created_time | 創建時間 | LocalDateTime |
| modified_by | 修改者ID | String |
| modified_time | 修改時間 | LocalDateTime |

### 擴展審計欄位 

| 欄位名 | 說明 | 類型 |
|--------|------|------|
| created_company | 創建者所屬公司 | String |
| created_unit | 創建者所屬部門 | String |
| created_name | 創建者姓名 (僅用戶相關實體) | String |
| modified_company | 修改者所屬公司 | String |
| modified_unit | 修改者所屬部門 | String |
| modified_name | 修改者姓名 (僅用戶相關實體) | String |

### 環境特有審計欄位

| 欄位名 | 說明 | 類型 |
|--------|------|------|
| reviewed_by | 審核者ID | String |
| reviewed_time | 審核時間 | LocalDateTime |
| reviewer_name | 審核者姓名 | String |
| review_status | 審核狀態 | String |
| deployed_by | 部署者ID | String |
| deployed_time | 部署時間 | LocalDateTime |
| deployer_name | 部署者姓名 | String |
| deploy_status | 部署狀態 | String |

## 功能演示

### 一、基本審計功能演示 (pf_user)

User實體展示了標準審計欄位與用戶擴展審計欄位的使用。

#### 1. 創建用戶

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "name": "張三",
    "description": "測試用戶",
    "username": "zhangsan",
    "password": "password123",
    "email": "zhangsan@example.com",
    "cellphone": "0912345678",
    "statusId": "1"
  }'
```

**審計欄位說明：**
- `createdBy`：創建者 ID
- `createdCompany`：創建者所屬公司
- `createdUnit`：創建者所屬部門
- `createdName`：創建者姓名
- `createdTime`：創建時間
- `modifiedBy`：最後修改者 ID（初始創建時與創建者相同）
- `modifiedCompany`：最後修改者所屬公司
- `modifiedUnit`：最後修改者所屬部門
- `modifiedName`：最後修改者姓名
- `modifiedTime`：最後修改時間（初始創建時與創建時間相同）

#### 2. 更新用戶

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "name": "張三",
    "description": "更新後的描述",
    "username": "zhangsan",
    "password": "password123",
    "email": "zhangsan.updated@example.com",
    "cellphone": "0912345678",
    "statusId": "2"
  }'
```

**審計欄位變化：**
- `createdBy`、`createdCompany`、`createdUnit`、`createdName`、`createdTime`：保持不變
- `modifiedBy`：更新為新的修改者 ID
- `modifiedCompany`：更新為新的修改者所屬公司
- `modifiedUnit`：更新為新的修改者所屬部門
- `modifiedName`：更新為新的修改者姓名
- `modifiedTime`：更新為新的修改時間

### 二、API 審計演示 (pf_api)

API實體也展示了標準審計欄位與用戶擴展審計欄位的使用，與User實體類似。

#### 1. 創建 API

```bash
curl -X POST http://localhost:8080/api/apis \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "apiname": "user-service",
    "description": "用戶服務 API"
  }'
```

**審計欄位說明：**
- 與用戶表相同的標準審計欄位結構，包括創建和修改的用戶信息

#### 2. 更新 API

```bash
curl -X PUT http://localhost:8080/api/apis/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "apiname": "user-service",
    "description": "用戶服務 API - 更新版"
  }'
```

**審計欄位變化：**
- `createdBy`, `createdCompany`, `createdUnit`, `createdName`, `createdTime` 保持不變
- `modifiedBy`, `modifiedCompany`, `modifiedUnit`, `modifiedName`, `modifiedTime` 更新為新的修改者和時間

### 三、環境配置審計演示 (pf_environment)

Environment實體展示了更複雜的審計需求，除了標準審計欄位外，還包含了環境特有的審計欄位，用於記錄審核和部署流程。

#### 1. 創建環境配置

```bash
curl -X POST http://localhost:8080/api/environments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
    "status": 0
  }'
```

**審計欄位說明：**
- 標準審計欄位：記錄創建和修改信息
- 專屬審計欄位：`reviewedBy`, `reviewedTime`, `deployedBy`, `deployedTime` 等，初始為 null

#### 2. 提交審核環境配置（更新狀態為審核中）

```bash
curl -X PUT http://localhost:8080/api/environments/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
    "status": 1
  }'
```

**審計欄位變化：**
- `status` 變更為 1（審核中）
- `modifiedBy`, `modifiedTime` 等修改欄位更新
- 審核和部署欄位仍為 null

#### 3. 審核環境配置（更新狀態為已審核）

```bash
curl -X PUT http://localhost:8080/api/environments/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
    "status": 2
  }'
```

**審計欄位變化：**
- `status` 變更為 2（已審核）
- `modifiedBy`, `modifiedTime` 等修改欄位更新
- 審核欄位 `reviewedBy`, `reviewedTime`, `reviewedCompany`, `reviewedUnit` 被自動填充
- 擴展審核欄位 `reviewerName`, `reviewStatus`, `reviewComment` 也被自動填充
- 部署欄位仍為 null

#### 4. 部署環境配置（更新狀態為已部署）

```bash
curl -X PUT http://localhost:8080/api/environments/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
    "status": 3
  }'
```

**審計欄位變化：**
- `status` 變更為 3（已部署）
- `modifiedBy`, `modifiedTime` 等修改欄位更新
- 部署欄位 `deployedBy`, `deployedTime`, `deployedCompany`, `deployedUnit` 被自動填充
- 擴展部署欄位 `deployerName`, `deployStatus`, `deployComment` 也被自動填充

#### 5. 查看最終審計欄位

```bash
curl -X GET http://localhost:8080/api/environments/1/audit-fields
```

### 四、實體關聯審計示例 (pf_demo_complex_audit)

本專案也示範了如何實現實體關聯式的審計功能，即使用 `AuditorAware<User>` 的方式直接關聯到用戶實體。

#### 1. 創建記錄

```bash
curl -X POST http://localhost:8080/api/demo-complex-audit \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer test-token" \
     -d '{"name":"測試實體關聯審計","description":"這是一個測試實體關聯審計方式的範例"}'
```

**審計欄位實際值：**
```json
{
    "id": 7,
    "name": "測試實體關聯審計",
    "description": "這是一個測試實體關聯審計方式的範例",
    "createdByUser": {
        "id": 51,
        "name": "測試用戶",
        "email": "test@example.com",
        "description": "測試用戶帳號",
        "username": "test-token",
        "password": "none",
        "statusId": "1",
        ...其他用戶屬性...
    },
    "createdTime": "2025-04-09 13:25:58",
    "lastModifiedByUser": {
        "id": 51,
        "name": "測試用戶",
        "email": "test@example.com",
        "description": "測試用戶帳號",
        "username": "test-token",
        ...其他用戶屬性...
    },
    "lastModifiedTime": "2025-04-09 13:25:58",
    "version": 0
}
```

**審計欄位說明：**
- `createdByUser`：成功關聯到 "test-token" 對應的用戶實體（ID: 51, 名稱: "測試用戶"）
- `createdTime`：自動設置為當前時間 "2025-04-09 13:25:58"
- `lastModifiedByUser`：初始與創建者相同，也是同一個測試用戶實體
- `lastModifiedTime`：初始與創建時間相同
- `version`：初始值設置為 0

#### 2. 查詢記錄

```bash
curl -X GET http://localhost:8080/api/demo-complex-audit/7
```

返回的結果中包含完整的用戶實體信息，無需額外查詢即可獲取審計者詳細資料。

#### 3. 更新記錄

```bash
curl -X PUT http://localhost:8080/api/demo-complex-audit/7 \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer admin-token" \
     -d '{"name":"更新的實體關聯審計","description":"更新實體關聯審計記錄"}'
```

**審計欄位變化：**
```json
{
    "id": 7,
    "name": "更新的實體關聯審計",
    "description": "更新實體關聯審計記錄",
    "createdByUser": {
        "id": 51,
        "name": "測試用戶",
        "email": "test@example.com",
        "description": "測試用戶帳號",
        "username": "test-token",
        ...不變的創建者資訊...
    },
    "createdTime": "2025-04-09 13:25:58",
    "lastModifiedByUser": {
        "id": 50,
        "name": "系統用戶",
        "email": "system@example.com",
        "description": "系統管理員用戶",
        "username": "system",
        ...新的修改者資訊...
    },
    "lastModifiedTime": "2025-04-09 13:26:08",
    "version": 1
}
```

**審計欄位實際變化：**
- `createdByUser`：保持不變，仍然是測試用戶（ID: 51）
- `createdTime`：保持不變 "2025-04-09 13:25:58"
- `lastModifiedByUser`：成功更新為系統用戶（ID: 50, 名稱: "系統用戶"）
- `lastModifiedTime`：更新為當前時間 "2025-04-09 13:26:08"
- `version`：正確遞增為 1

#### 4. 再次查詢查看變化

```bash
curl -X GET http://localhost:8080/api/demo-complex-audit/7
```

可以觀察到審計欄位的完整變化，特別是`lastModifiedByUser`已更新為不同的用戶實體，而`createdByUser`保持不變。

### 實體關聯審計的注意事項

1. **性能考量**：關聯查詢可能會導致多表連接，影響查詢性能
2. **序列化問題**：在序列化實體時需要注意避免無限遞迴問題
3. **刪除策略**：需要謹慎設計實體的刪除策略，避免因為關聯導致的刪除問題

### 實際測試案例

以下是實際執行的測試案例，展示了實體關聯審計的具體運作方式。

#### 1. 創建記錄

```bash
curl -X POST http://localhost:8080/api/demo-complex-audit \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer test-token" \
     -d '{"name":"測試實體關聯審計","description":"這是一個測試實體關聯審計方式的範例"}'
```


**審計欄位說明：**
- `createdByUser`：成功關聯到 "test-token" 對應的用戶實體（ID: 51, 名稱: "測試用戶"）
- `createdTime`：自動設置為當前時間 "2025-04-09 13:25:58"
- `lastModifiedByUser`：初始與創建者相同，也是同一個測試用戶實體
- `lastModifiedTime`：初始與創建時間相同
- `version`：初始值設置為 0

#### 2. 查詢記錄

```bash
curl -X GET http://localhost:8080/api/demo-complex-audit/7
```

返回的結果中包含完整的用戶實體信息，無需額外查詢即可獲取審計者詳細資料。

#### 3. 更新記錄

```bash
curl -X PUT http://localhost:8080/api/demo-complex-audit/7 \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer admin-token" \
     -d '{"name":"更新的實體關聯審計","description":"更新實體關聯審計記錄"}'
```


**審計欄位實際變化：**
- `createdByUser`：保持不變，仍然是測試用戶（ID: 51）
- `createdTime`：保持不變 "2025-04-09 13:25:58"
- `lastModifiedByUser`：成功更新為系統用戶（ID: 50, 名稱: "系統用戶"）
- `lastModifiedTime`：更新為當前時間 "2025-04-09 13:26:08"
- `version`：正確遞增為 1

#### 4. 再次查詢查看變化

```bash
curl -X GET http://localhost:8080/api/demo-complex-audit/7
```

可以觀察到審計欄位的完整變化，特別是`lastModifiedByUser`已更新為不同的用戶實體，而`createdByUser`保持不變。

### 實體關聯審計的關鍵發現

通過上述實際測試，我們驗證了以下關鍵特性：

1. **實體關聯正常運作**：審計欄位直接關聯到完整的用戶實體，而不僅僅是ID
2. **審計字段不可篡改性**：
   - 創建者信息在記錄生命週期中保持不變
   - 修改者信息反映最後操作的用戶
3. **版本控制有效性**：每次更新操作都正確遞增版本號
4. **數據豐富性**：通過單次查詢即可獲取完整的審計用戶信息
5. **無需關聯查詢**：不需要額外查詢用戶表即可獲取審計者詳細信息

實體關聯審計相比基於字符串ID的審計，能提供更豐富、更直接的審計信息，適用於對審計要求較高的業務場景。

### 實體關聯審計 (Entity Relationship Auditing) 總結

實體關聯審計是一種通過外鍵關聯直接連接到審計者實體的審計方式，與傳統的只存儲審計者ID的方法相比，具有以下特點：

#### 1. 實現機制
```java
// 實體類中使用 @ManyToOne 和 @JoinColumn 建立關聯
@ManyToOne
@JoinColumn(name = "created_by_id", nullable = false, updatable = false)
private User createdByUser;

@ManyToOne
@JoinColumn(name = "last_modified_by_id", nullable = false)
private User lastModifiedByUser;
```

```sql
-- 資料庫中建立外鍵約束
constraint fk_demo_complex_audit_created_by
    foreign key (created_by_id) references pf_user (id),
constraint fk_demo_complex_audit_modified_by
    foreign key (last_modified_by_id) references pf_user (id)
```

#### 2. 操作方法
- **創建記錄**：直接設置完整的 User 實體，而非僅設置 ID
  ```java
  complexAudit.setCreatedByUser(currentUser);
  complexAudit.setLastModifiedByUser(currentUser);
  ```

- **更新記錄**：僅更新 lastModifiedByUser 欄位
  ```java
  existingAudit.setLastModifiedByUser(currentUser);
  existingAudit.setLastModifiedTime(LocalDateTime.now());
  ```
