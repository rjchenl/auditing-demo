# Spring Boot JPA 審計功能實作指南

本專案提供一個完整的 Spring Data JPA 審計功能實作範例，作為開發人員的參考模板。專案實現了標準審計欄位與擴展審計欄位的混合架構，適用於各種複雜度的業務場景。

## 目錄

1. [功能特色](#功能特色)
2. [審計架構設計](#審計架構設計)
3. [核心組件介紹](#核心組件介紹)
4. [使用示例](#使用示例)
5. [實體模型說明](#實體模型說明)
6. [擴展與客製化](#擴展與客製化)
7. [快速開始](#快速開始)
8. [常見問題](#常見問題)
9. [設計圖表](#設計圖表)

## 功能特色

- **混合式審計架構**：結合 Spring Data JPA 標準審計功能與自訂介面擴展
- **層次化審計介面**：使用介面繼承提供彈性的擴展結構
- **業務特定審計**：支援複雜業務場景的特殊審計需求（如審核、部署流程）
- **可擴展設計**：易於根據業務需求進行擴展和客製化

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

### 標準審計欄位

使用 Spring Data JPA 註解實現基本審計功能：

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

### 擴展審計欄位

使用介面架構實現：

```
AuditableInterface (擴展審計介面)
├── UserAuditableInterface (用戶審計介面)
└── EnvironmentAuditableInterface (環境審計介面)
```

## 核心組件介紹

### 1. JPA 審計配置

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new CustomAuditorAware();
    }
}
```

`@EnableJpaAuditing` 啟用 Spring Data JPA 的審計功能，並指定 `auditorProvider` 作為審計者提供者。

### 2. 審計者提供者

```java
@Component
public class CustomAuditorAware implements AuditorAware<String> {
    
    @Autowired
    private TokenService tokenService;

    @Override
    public Optional<String> getCurrentAuditor() {
        String token = UserContext.getCurrentUser();
        if (token != null && !token.isEmpty()) {
            Map<String, String> userInfo = tokenService.getUserInfoFromToken(token);
            if (userInfo != null && userInfo.containsKey("userId")) {
                return Optional.of(userInfo.get("userId"));
            }
        }
        return Optional.of("system");
    }
}
```

`CustomAuditorAware` 實現 Spring Data JPA 的 `AuditorAware` 介面，負責提供當前操作用戶的 ID。

### 3. 審計介面

#### 基礎審計介面

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

#### 用戶審計介面

```java
public interface UserAuditableInterface extends AuditableInterface {
    // 用戶相關擴展審計欄位
    String getCreatedName();
    void setCreatedName(String createdName);
    String getModifiedName();
    void setModifiedName(String modifiedName);
}
```

#### 環境審計介面

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
    // ... 其他部署欄位
}
```

### 4. 審計監聽器

#### 通用審計監聽器

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
    
    private void processAuditFieldsWithInterface(AuditableInterface entity, boolean isCreate) {
        // 處理審計欄位
    }
}
```

#### 環境審計監聽器

```java
@Component
@Configurable
public class EnvironmentAuditListener {
    
    public void performReview(EnvironmentAuditableInterface entity, String reviewStatus, String reviewComment) {
        // 處理審核相關欄位
    }
    
    public void performDeploy(EnvironmentAuditableInterface entity, String deployStatus, String deployComment) {
        // 處理部署相關欄位
    }
}
```

## 使用示例

### 實體類定義

```java
@Entity
@Table(name = "pf_user")
@EntityListeners({AuditingEntityListener.class, AuditEntityListener.class})
public class User implements UserAuditableInterface {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    // 標準審計欄位
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
    
    // 擴展審計欄位
    @Column(name = "created_company")
    private String createdCompany;
    
    // ... 其他欄位
}
```

### 環境實體範例

```java
@Entity
@Table(name = "pf_environment")
@EntityListeners({AuditingEntityListener.class, EnvironmentAuditListener.class, AuditEntityListener.class})
public class Environment implements EnvironmentAuditableInterface {
    
    // ... 實體欄位
    
    // 標準審計欄位
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;
    
    // ... 其他標準審計欄位
    
    // 擴展審計欄位
    @Column(name = "created_company", updatable = false)
    private String createdCompany;
    
    // ... 其他擴展審計欄位
    
    // 環境特有審計欄位
    @Column(name = "reviewed_by")
    private String reviewedBy;
    
    // ... 其他環境特有審計欄位
}
```

## 實體模型說明

本專案包含三種實體模型，展示不同程度的審計需求：

1. **User 實體**：基本審計 + 用戶擴展審計
2. **Api 實體**：基本審計 + 用戶擴展審計
3. **Environment 實體**：基本審計 + 環境特有審計 (包含審核與部署流程)

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

## 擴展與客製化

### 添加新的審計欄位

1. 在相應的審計介面中定義新的 getter 和 setter 方法
2. 在實體類中添加對應的屬性和欄位
3. 在監聽器中添加處理邏輯

### 增加新的業務流程審計

1. 在 EnvironmentAuditableInterface 中定義新的審計欄位
2. 在 Environment 實體中添加對應的欄位
3. 在 EnvironmentAuditListener 中添加新的方法處理特定業務流程的審計邏輯

### 使用不同的用戶身份識別方式

如需使用不同的用戶身份識別方式（例如使用完整的 User 實體而非僅 ID）：

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration {
    
    @Bean
    public AuditorAware<User> auditorProvider() {
        return new CustomUserEntityAuditorAware();
    }
}

public class CustomUserEntityAuditorAware implements AuditorAware<User> {

    @Autowired
    private UserRepository userRepository;
    
    @Override
    public Optional<User> getCurrentAuditor() {
        String token = UserContext.getCurrentUser();
        if (token != null && !token.isEmpty()) {
            return userRepository.findByUsername(token);
        }
        return userRepository.findByUsername("system");
    }
}
```

## 快速開始

### 環境需求

- Java 11+
- Maven 3.6+
- Docker & Docker Compose (用於本地開發)

### 啟動步驟

1. 克隆專案:
   ```bash
   git clone https://github.com/yourusername/auditing-demo.git
   cd auditing-demo
   ```

2. 啟動 PostgreSQL 資料庫:
   ```bash
   docker-compose up -d
   ```

3. 編譯和運行應用:
   ```bash
   ./mvnw spring-boot:run
   ```

4. 應用將在 http://localhost:8080 運行

### 示例調用

```bash
# 創建用戶
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer kenbai" \
  -d '{
    "name": "測試用戶",
    "description": "測試描述",
    "email": "test@example.com",
    "username": "testuser",
    "password": "password123"
  }'
```

### 環境配置生命週期測試

以下示例展示了環境配置的完整生命週期操作，從創建到部署：

```bash
# 1. 創建環境配置（草稿狀態）
curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer user_token" \
  -d '{
    "name":"開發環境",
    "description":"用於開發測試的環境",
    "type":"DEV",
    "configValue":"{\"server\":\"dev-server\",\"port\":8080,\"database\":\"dev-db\"}",
    "status":0
  }' \
  http://localhost:8080/api/environments

# 2. 提交審核（狀態更新為審核中）
curl -X PUT -H "Content-Type: application/json" -H "Authorization: Bearer user_token" \
  -d '{"status":1}' \
  http://localhost:8080/api/environments/12

# 3. 執行審核操作
curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer user_token" \
  http://localhost:8080/api/environments/12/review

# 4. 更新狀態為已審核
curl -X PUT -H "Content-Type: application/json" -H "Authorization: Bearer user_token" \
  -d '{"status":2}' \
  http://localhost:8080/api/environments/12

# 5. 執行部署操作
curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer user_token" \
  -d '{"version":"1.0.0"}' \
  http://localhost:8080/api/environments/12/deploy

# 6. 查詢環境審計欄位
curl -H "Authorization: Bearer user_token" \
  http://localhost:8080/api/environments/12/audit-fields
```

執行以上命令後，可以看到環境配置經過了完整的生命週期：
- 從草稿狀態 (0) 開始
- 更新為審核中 (1) 
- 執行審核操作，填充審核相關欄位
- 更新為已審核狀態 (2)
- 執行部署操作，設置版本號，填充部署相關欄位，狀態更新為已部署 (3)

最終響應中可以看到完整的審計信息：

```json
{
  "created_time": "2025-04-08T23:54:00.380375",
  "deployed_company": null,
  "reviewed_by": "0",
  "modified_company": null,
  "deployed_unit": null,
  "modified_unit": null,
  "created_by": "0",
  "created_company": null,
  "version": "1.0.0",
  "deployed_time": "2025-04-08T23:55:07.437094",
  "modified_time": "2025-04-08T23:55:07.438478",
  "name": "開發環境",
  "modified_by": "0",
  "reviewed_time": "2025-04-08T23:54:57.40655",
  "reviewed_company": null,
  "id": 12,
  "deployed_by": "0",
  "reviewed_unit": null,
  "created_unit": null,
  "status": 3
}
```

## 常見問題

### 如何切換使用者身份識別方式？

默認使用 String 型別的用戶 ID，如需使用實體關聯：

1. 修改 AuditorAware 實現為 `AuditorAware<User>`
2. 修改實體類中的審計欄位類型為 User 並添加 @ManyToOne 關聯
3. 更新 CustomAuditorAware 實現從資料庫查詢用戶實體

### 如何擴展到其他業務場景？

1. 定義新的審計介面，繼承自基礎 AuditableInterface
2. 為新業務場景添加特定審計欄位
3. 實現專屬的監聽器或在現有監聽器中添加處理邏輯

### 如何在測試中模擬審計功能？

可以使用 Spring Security Test 提供的 `WithMockUser` 註解或手動設置 `UserContext` 中的用戶值。

## 設計圖表

本專案包含一系列 PlantUML 設計圖表，用於說明審計系統的設計和實現細節：

### 審計系統架構概覽

此圖展示整個審計系統的架構，包括標準審計框架、自定義審計接口和監聽器、用戶上下文處理機制等。
- [查看架構圖](docs/diagrams/audit-architecture.puml)

### 審計欄位處理流程

詳細展示審計欄位從請求進入系統到數據最終持久化的完整流程，包括標準審計和擴展審計的處理路徑。
- [查看流程圖](docs/diagrams/audit-process.puml)

### 審計類圖

說明系統中審計相關的類和接口關係，包括審計接口的繼承關係、實體與審計接口的實現關係等。
- [查看類圖](docs/diagrams/audit-entities.puml)

### 審計欄位關系圖

詳細展示各類審計欄位的分組和關係，包括標準審計欄位、擴展審計欄位及環境特有審計欄位。
- [查看欄位關系圖](docs/diagrams/audit-fields.puml)

### 審計元件交互圖

展示系統各組件之間的交互關係，包括控制器、實體、審計監聽器與上下文服務的交互。
- [查看元件交互圖](docs/diagrams/audit-component.puml)

您可以使用 PlantUML 工具查看這些圖表，或參考 [圖表說明文檔](docs/diagrams/README.md) 獲取更多信息。

# 複雜審計功能演示

## 簡介

本專案展示了兩種審計方式：
1. 基本審計：使用簡單的 `String` 類型記錄用戶 ID
2. 複雜審計：使用 `JSONB` 類型存儲完整的用戶資訊

## 複雜審計功能

複雜審計功能使用 PostgreSQL 的 `JSONB` 數據類型，可以存儲完整的用戶資訊，而不僅僅是用戶 ID。這種方式提供以下優勢：

- 審計資訊更加完整（包含用戶姓名、角色、部門等）
- 無需關聯查詢即可獲取完整的審計資訊
- 保留歷史時間點的用戶資訊快照
- 更靈活的用戶資訊存儲方式

## 資料庫結構

複雜審計演示表 `pf_demo_complex_audit` 結構如下：

```sql
CREATE TABLE pf_demo_complex_audit (
    id                  bigserial
        constraint pf_demo_complex_audit_pk
            primary key,
    name                varchar(100)                         not null,
    description         text,
    -- 複雜審計欄位（使用 JSON 類型存儲完整的用戶資訊）
    created_by_user     jsonb                               not null,
    created_time        timestamp    default now()           not null,
    last_modified_by_user jsonb                             not null,
    last_modified_time  timestamp    default now()           not null,
    version            integer      default 0               not null
);
```

# 審計功能演示

## 一、複雜審計功能演示 (pf_demo_complex_audit)

以下是使用 curl 命令進行複雜審計功能演示的步驟：

### 1. 創建記錄

使用 `test-token` 創建一條新記錄：

```bash
curl -X POST http://localhost:8080/api/demo-complex-audit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{"name":"測試複雜審計","description":"這是一個測試複雜審計功能的項目"}' | jq
```

輸出結果：

```json
{
  "id": 1,
  "name": "測試複雜審計",
  "description": "這是一個測試複雜審計功能的項目",
  "createdByUser": {
    "id": 1001,
    "username": "test.user",
    "name": "測試使用者",
    "email": "test.user@example.com",
    "company": "測試公司",
    "unit": "研發部門",
    "roles": [
      "ADMIN",
      "USER"
    ],
    "ip": null,
    "device": null,
    "location": null
  },
  "createdTime": "2025-04-08T23:13:03.559332",
  "lastModifiedByUser": {
    "id": 1001,
    "username": "test.user",
    "name": "測試使用者",
    "email": "test.user@example.com",
    "company": "測試公司",
    "unit": "研發部門",
    "roles": [
      "ADMIN",
      "USER"
    ],
    "ip": null,
    "device": null,
    "location": null
  },
  "lastModifiedTime": "2025-04-08T23:13:03.559332",
  "version": 0
}
```

**審計欄位說明：**
- `createdByUser`：包含完整的創建者資訊（ID、用戶名、姓名、電子郵件、公司、部門、角色等）
- `createdTime`：創建時間
- `lastModifiedByUser`：與 createdByUser 相同（初始創建時）
- `lastModifiedTime`：與 createdTime 相同（初始創建時）
- `version`：版本號，初始為 0

### 2. 更新記錄

使用不同的用戶（`admin-token`）更新記錄：

```bash
curl -X PUT http://localhost:8080/api/demo-complex-audit/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{"name":"管理員更新的名稱","description":"這是由管理員更新的描述"}' | jq
```

輸出結果：

```json
{
  "id": 1,
  "name": "管理員更新的名稱",
  "description": "這是由管理員更新的描述",
  "createdByUser": {
    "id": 1001,
    "username": "test.user",
    "name": "測試使用者",
    "email": "test.user@example.com",
    "company": "測試公司",
    "unit": "研發部門",
    "roles": [
      "ADMIN",
      "USER"
    ],
    "ip": null,
    "device": null,
    "location": null
  },
  "createdTime": "2025-04-08T23:13:03.559332",
  "lastModifiedByUser": {
    "id": 1002,
    "username": "admin.user",
    "name": "管理員",
    "email": "admin@example.com",
    "company": "測試公司",
    "unit": "管理部門",
    "roles": [
      "SUPER_ADMIN"
    ],
    "ip": null,
    "device": null,
    "location": null
  },
  "lastModifiedTime": "2025-04-08T23:13:09.79954",
  "version": 1
}
```

**審計欄位變化：**
- `createdByUser`：保持不變（仍然是最初創建記錄的用戶）
- `createdTime`：保持不變
- `lastModifiedByUser`：更新為新用戶（管理員）的完整資訊
- `lastModifiedTime`：更新為新的時間戳
- `version`：從 0 增加到 1

### 3. 查詢記錄

查詢記錄以查看審計歷史：

```bash
curl -X GET http://localhost:8080/api/demo-complex-audit/1 | jq
```

## 二、基本審計功能演示 (pf_user)

### 1. 創建用戶

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
  }' | jq
```

輸出結果：

```json
{
  "id": 1,
  "name": "張三",
  "description": "測試用戶",
  "username": "zhangsan",
  "password": "password123",
  "email": "zhangsan@example.com",
  "cellphone": "0912345678",
  "statusId": "1",
  "createdBy": "1001",
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "createdName": "測試使用者",
  "createdTime": "2025-04-08T23:30:00.123456",
  "modifiedBy": "1001",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "研發部門",
  "modifiedName": "測試使用者",
  "modifiedTime": "2025-04-08T23:30:00.123456"
}
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

### 2. 更新用戶

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
  }' | jq
```

輸出結果：

```json
{
  "id": 1,
  "name": "張三",
  "description": "更新後的描述",
  "username": "zhangsan",
  "password": "password123",
  "email": "zhangsan.updated@example.com",
  "cellphone": "0912345678",
  "statusId": "2",
  "createdBy": "1001",
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "createdName": "測試使用者",
  "createdTime": "2025-04-08T23:30:00.123456",
  "modifiedBy": "1002",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "管理部門",
  "modifiedName": "管理員",
  "modifiedTime": "2025-04-08T23:35:00.654321"
}
```

**審計欄位變化：**
- `createdBy`、`createdCompany`、`createdUnit`、`createdName`、`createdTime`：保持不變
- `modifiedBy`：更新為新的修改者 ID
- `modifiedCompany`：更新為新的修改者所屬公司
- `modifiedUnit`：更新為新的修改者所屬部門
- `modifiedName`：更新為新的修改者姓名
- `modifiedTime`：更新為新的修改時間

## 三、環境配置審計演示 (pf_environment)

環境配置表除了標準審計欄位外，還有專屬的狀態變更審計欄位，用於記錄審核及部署等操作。

### 1. 創建環境配置

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
  }' | jq
```

輸出結果：

```json
{
  "id": 8,
  "name": "測試環境",
  "description": "用於測試的環境配置",
  "type": "TEST",
  "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
  "status": 0,
  "version": "1.0",
  "createdBy": "1001",
  "createdTime": "2025-04-08T23:40:00.123456",
  "modifiedBy": "1001",
  "modifiedTime": "2025-04-08T23:40:00.123456",
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "研發部門",
  "reviewedBy": null,
  "reviewedTime": null,
  "deployedBy": null,
  "deployedTime": null,
  "deployedCompany": null,
  "deployedUnit": null
}
```

**審計欄位說明：**
- 標準審計欄位：記錄創建和修改信息
- 專屬審計欄位：`reviewedBy`, `reviewedTime`, `deployedBy`, `deployedTime` 等，初始為 null

### 2. 提交審核環境配置（更新狀態為審核中）

```bash
curl -X PUT http://localhost:8080/api/environments/8 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
    "status": 1
  }' | jq
```

輸出結果：

```json
{
  "id": 8,
  "name": "測試環境",
  "description": "用於測試的環境配置",
  "type": "TEST",
  "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
  "status": 1,
  "version": "1.0",
  "createdBy": "1001",
  "createdTime": "2025-04-08T23:40:00.123456",
  "modifiedBy": "1001",
  "modifiedTime": "2025-04-08T23:45:00.789012",
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "研發部門",
  "reviewedBy": null,
  "reviewedTime": null,
  "reviewedCompany": null,
  "reviewedUnit": null,
  "deployedBy": null,
  "deployedTime": null,
  "deployedCompany": null,
  "deployedUnit": null
}
```

**審計欄位變化：**
- `status` 變更為 1（審核中）
- `modifiedBy`, `modifiedTime` 等修改欄位更新
- 審核和部署欄位仍為 null

### 3. 審核環境配置（更新狀態為已審核）

```bash
curl -X PUT http://localhost:8080/api/environments/8 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
    "status": 2,
    "reviewedBy": "1002",
    "reviewedTime": "2025-04-08 23:50:00",
    "reviewedCompany": "測試公司",
    "reviewedUnit": "管理部門"
  }' | jq
```

輸出結果：

```json
{
  "id": 8,
  "name": "測試環境",
  "description": "用於測試的環境配置",
  "type": "TEST",
  "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
  "status": 2,
  "version": "1.0",
  "createdBy": "1001",
  "createdTime": "2025-04-08T23:40:00.123456",
  "modifiedBy": "1002",
  "modifiedTime": "2025-04-08T23:50:00.456789",
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "管理部門",
  "reviewedBy": "1002",
  "reviewedTime": "2025-04-08T23:50:00.456789",
  "reviewedCompany": "測試公司",
  "reviewedUnit": "管理部門",
  "deployedBy": null,
  "deployedTime": null,
  "deployedCompany": null,
  "deployedUnit": null
}
```

**審計欄位變化：**
- `status` 變更為 2（已審核）
- `modifiedBy`, `modifiedTime` 等修改欄位更新
- 審核欄位 `reviewedBy`, `reviewedTime`, `reviewedCompany`, `reviewedUnit` 被填充
- 部署欄位仍為 null

### 4. 部署環境配置（更新狀態為已部署）

```bash
curl -X PUT http://localhost:8080/api/environments/8 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
    "status": 3,
    "reviewedBy": "1002",
    "reviewedTime": "2025-04-08 23:50:00",
    "reviewedCompany": "測試公司",
    "reviewedUnit": "管理部門",
    "deployedBy": "1002",
    "deployedTime": "2025-04-08 23:55:00",
    "deployedCompany": "測試公司",
    "deployedUnit": "管理部門"
  }' | jq
```

輸出結果：

```json
{
  "id": 8,
  "name": "測試環境",
  "description": "用於測試的環境配置",
  "type": "TEST",
  "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
  "status": 3,
  "version": "1.0",
  "createdBy": "1001",
  "createdTime": "2025-04-08T23:40:00.123456",
  "modifiedBy": "1002",
  "modifiedTime": "2025-04-08T23:55:00.123456",
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "管理部門",
  "reviewedBy": "1002",
  "reviewedTime": "2025-04-08T23:50:00.456789",
  "reviewedCompany": "測試公司",
  "reviewedUnit": "管理部門",
  "deployedBy": "1002",
  "deployedTime": "2025-04-08T23:55:00.123456",
  "deployedCompany": "測試公司",
  "deployedUnit": "管理部門"
}
```

**審計欄位變化：**
- `status` 變更為 3（已部署）
- `modifiedBy`, `modifiedTime` 等修改欄位更新
- 部署欄位 `deployedBy`, `deployedTime`, `deployedCompany`, `deployedUnit` 被填充

## 四、API 審計演示 (pf_api)

### 1. 創建 API

```bash
curl -X POST http://localhost:8080/api/apis \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "apiname": "user-service",
    "description": "用戶服務 API"
  }' | jq
```

輸出結果：

```json
{
  "id": 1,
  "apiname": "user-service",
  "description": "用戶服務 API",
  "createdBy": "1001",
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "createdName": "測試使用者",
  "createdTime": "2025-04-09T00:00:00.123456",
  "modifiedBy": "1001",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "研發部門",
  "modifiedName": "測試使用者",
  "modifiedTime": "2025-04-09T00:00:00.123456"
}
```

**審計欄位說明：**
- 與用戶表相同的標準審計欄位結構

### 2. 更新 API

```bash
curl -X PUT http://localhost:8080/api/apis/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "apiname": "user-service",
    "description": "用戶服務 API - 更新版"
  }' | jq
```

輸出結果：

```json
{
  "id": 1,
  "apiname": "user-service",
  "description": "用戶服務 API - 更新版",
  "createdBy": "1001",
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "createdName": "測試使用者",
  "createdTime": "2025-04-09T00:00:00.123456",
  "modifiedBy": "1002",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "管理部門",
  "modifiedName": "管理員",
  "modifiedTime": "2025-04-09T00:05:00.654321"
}
```

**審計欄位變化：**
- `createdBy`, `createdCompany`, `createdUnit`, `createdName`, `createdTime` 保持不變
- `modifiedBy`, `modifiedCompany`, `modifiedUnit`, `modifiedName`, `modifiedTime` 更新為新的修改者和時間

## 對比傳統審計方式

相比於傳統只存儲用戶 ID 的審計方式，複雜審計功能有以下優勢：

1. **資訊更完整**：存儲用戶的全部資訊，而不僅是 ID
2. **無需關聯查詢**：直接從審計欄位獲取完整用戶資訊
3. **歷史快照**：即使用戶資訊後來變更，審計記錄仍保留操作時的用戶狀態
4. **彈性更大**：可以根據需求擴展存儲的用戶屬性
5. **簡化報表**：所有資訊集中在一處，便於生成審計報表

## 可用的測試令牌

演示中可使用的令牌：

1. `test-token` - 普通用戶
   - ID: 1001
   - 用戶名: test.user
   - 角色: ADMIN,USER

2. `admin-token` - 管理員
   - ID: 1002
   - 用戶名: admin.user
   - 角色: SUPER_ADMIN
 