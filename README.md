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
  "id": 21,
  "name": "測試環境",
  "description": "用於測試的環境配置",
  "type": "TEST",
  "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
  "createdBy": "1001",
  "createdTime": "2025-04-09 09:39:10",
  "modifiedBy": "1001",
  "modifiedTime": "2025-04-09 09:39:10",
  "reviewedBy": null,
  "reviewedTime": null,
  "deployedBy": null,
  "deployedTime": null,
  "version": null,
  "status": 0,
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "研發部門",
  "reviewedCompany": null,
  "reviewedUnit": null,
  "deployedCompany": null,
  "deployedUnit": null,
  "reviewerName": null,
  "reviewStatus": null,
  "reviewComment": null,
  "deployerName": null,
  "deployStatus": null,
  "deployComment": null
}
```

**審計欄位說明：**
- 標準審計欄位：記錄創建和修改信息
- 專屬審計欄位：`reviewedBy`, `reviewedTime`, `deployedBy`, `deployedTime` 等，初始為 null

### 2. 提交審核環境配置（更新狀態為審核中）

```bash
curl -X PUT http://localhost:8080/api/environments/21 \
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
  "id": 21,
  "name": "測試環境",
  "description": "用於測試的環境配置",
  "type": "TEST",
  "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
  "createdBy": "1001",
  "createdTime": "2025-04-09 09:39:10",
  "modifiedBy": "1001",
  "modifiedTime": "2025-04-09 09:39:19",
  "reviewedBy": null,
  "reviewedTime": null,
  "deployedBy": null,
  "deployedTime": null,
  "version": null,
  "status": 1,
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "研發部門",
  "reviewedCompany": null,
  "reviewedUnit": null,
  "deployedCompany": null,
  "deployedUnit": null,
  "reviewerName": null,
  "reviewStatus": null,
  "reviewComment": null,
  "deployerName": null,
  "deployStatus": null,
  "deployComment": null
}
```

**審計欄位變化：**
- `status` 變更為 1（審核中）
- `modifiedBy`, `modifiedTime` 等修改欄位更新
- 審核和部署欄位仍為 null

### 3. 審核環境配置（更新狀態為已審核）

```bash
curl -X PUT http://localhost:8080/api/environments/21 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
    "status": 2
  }' | jq
```

輸出結果：

```json
{
  "id": 21,
  "name": "測試環境",
  "description": "用於測試的環境配置",
  "type": "TEST",
  "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
  "createdBy": "1001",
  "createdTime": "2025-04-09 09:39:10",
  "modifiedBy": "1002",
  "modifiedTime": "2025-04-09 09:45:28",
  "reviewedBy": "1002",
  "reviewedTime": "2025-04-09 09:45:28",
  "deployedBy": null,
  "deployedTime": null,
  "version": null,
  "status": 2,
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "管理部門",
  "reviewedCompany": "測試公司",
  "reviewedUnit": "管理部門",
  "deployedCompany": null,
  "deployedUnit": null,
  "reviewerName": "管理員",
  "reviewStatus": "已審核",
  "reviewComment": "透過狀態更新自動審核",
  "deployerName": null,
  "deployStatus": null,
  "deployComment": null
}
```

**審計欄位變化：**
- `status` 變更為 2（已審核）
- `modifiedBy`, `modifiedTime` 等修改欄位更新
- 審核欄位 `reviewedBy`, `reviewedTime`, `reviewedCompany`, `reviewedUnit` 被自動填充
- 擴展審核欄位 `reviewerName`, `reviewStatus`, `reviewComment` 也被自動填充
- 部署欄位仍為 null

### 4. 部署環境配置（更新狀態為已部署）

```bash
curl -X PUT http://localhost:8080/api/environments/21 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "name": "測試環境",
    "description": "用於測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
    "status": 3
  }' | jq
```

輸出結果：

```json
{
  "id": 21,
  "name": "測試環境",
  "description": "用於測試的環境配置",
  "type": "TEST",
  "configValue": "{\"server\":\"test-server\",\"port\":8080,\"debug\":true}",
  "createdBy": "1001",
  "createdTime": "2025-04-09 09:39:10",
  "modifiedBy": "1002",
  "modifiedTime": "2025-04-09 09:45:33",
  "reviewedBy": "1002",
  "reviewedTime": "2025-04-09 09:45:28",
  "deployedBy": "1002",
  "deployedTime": "2025-04-09 09:45:33",
  "version": null,
  "status": 3,
  "createdCompany": "測試公司",
  "createdUnit": "研發部門",
  "modifiedCompany": "測試公司",
  "modifiedUnit": "管理部門",
  "reviewedCompany": "測試公司",
  "reviewedUnit": "管理部門",
  "deployedCompany": "測試公司",
  "deployedUnit": "管理部門",
  "reviewerName": "管理員",
  "reviewStatus": "已審核",
  "reviewComment": "透過狀態更新自動審核",
  "deployerName": "管理員",
  "deployStatus": "已部署",
  "deployComment": "透過狀態更新自動部署"
}
```

**審計欄位變化：**
- `status` 變更為 3（已部署）
- `modifiedBy`, `modifiedTime` 等修改欄位更新
- 部署欄位 `deployedBy`, `deployedTime`, `deployedCompany`, `deployedUnit` 被自動填充
- 擴展部署欄位 `deployerName`, `deployStatus`, `deployComment` 也被自動填充

### 5. 查看最終審計欄位

```bash
curl -X GET http://localhost:8080/api/environments/21/audit-fields | jq
```

輸出結果：

```json
{
  "created_time": "2025-04-09T09:39:10.545391",
  "deployed_company": "測試公司",
  "reviewed_by": "1002",
  "modified_company": "測試公司",
  "deployed_unit": "管理部門",
  "modified_unit": "管理部門",
  "created_by": "1001",
  "created_company": "測試公司",
  "version": null,
  "deployed_time": "2025-04-09T09:45:33.123456",
  "modified_time": "2025-04-09T09:45:33.123456",
  "name": "測試環境",
  "modified_by": "1002",
  "reviewed_time": "2025-04-09T09:45:28.123456",
  "reviewed_company": "測試公司",
  "id": 21,
  "deployed_by": "1002",
  "reviewed_unit": "管理部門",
  "created_unit": "研發部門",
  "status": 3
}
```

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

## 實體關聯審計示例

除了使用 `String` 類型作為審計者識別符外，本專案也示範了如何實現實體關聯式的審計功能，即使用 `AuditorAware<User>` 的方式直接關聯到用戶實體。

### 1. 實體定義

```java
@Entity
@Table(name = "pf_demo_complex_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplexAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column
    private String description;
    
    // 使用實體關聯方式的審計字段
    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdByUser;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @ManyToOne
    @JoinColumn(name = "last_modified_by_id", nullable = false)
    private User lastModifiedByUser;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_modified_time", nullable = false)
    private LocalDateTime lastModifiedTime;
    
    @Column(nullable = false)
    private Integer version;
}
```

### 2. 資料庫表結構

```sql
CREATE TABLE IF NOT EXISTS pf_demo_complex_audit (
    id                  bigserial
        constraint pf_demo_complex_audit_pk
            primary key,
    name                varchar(100)                         not null,
    description         text,
    -- 實體關聯方式審計欄位
    created_by_id       bigint                               not null,
    created_time        timestamp    default now()           not null,
    last_modified_by_id bigint                               not null,
    last_modified_time  timestamp    default now()           not null,
    version             integer      default 0               not null,
    
    -- 外鍵約束
    constraint fk_demo_complex_audit_created_by
        foreign key (created_by_id) references pf_user (id),
    constraint fk_demo_complex_audit_modified_by
        foreign key (last_modified_by_id) references pf_user (id)
);
```

### 3. 控制器實現

```java
@Slf4j
@RestController
@RequestMapping("/api/demo-complex-audit")
public class ComplexAuditController {

    @Autowired
    private ComplexAuditRepository complexAuditRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 創建複雜審計記錄
     * 手動處理審計字段
     */
    @PostMapping
    public ComplexAudit createComplexAudit(
            @RequestBody ComplexAudit complexAudit,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            // 設置請求頭中的用戶令牌到UserContext
            setUserContext(authHeader);
            
            // 獲取當前審計用戶
            User currentUser = getCurrentAuditor();
            
            // 設置審計字段
            complexAudit.setVersion(0);
            complexAudit.setCreatedByUser(currentUser);
            complexAudit.setCreatedTime(LocalDateTime.now());
            complexAudit.setLastModifiedByUser(currentUser);
            complexAudit.setLastModifiedTime(LocalDateTime.now());
            
            // 保存記錄
            return complexAuditRepository.save(complexAudit);
        } finally {
            // 清除 ThreadLocal
            UserContext.clear();
        }
    }
    
    /**
     * 獲取當前審計用戶
     */
    private User getCurrentAuditor() {
        String token = UserContext.getCurrentUser();
        
        // 根據token查找用戶
        if (token != null && !token.isEmpty()) {
            Optional<User> user = userRepository.findByUsername(token);
            if (user.isPresent()) {
                return user.get();
            }
        }
        
        // 返回系統用戶
        return userRepository.findByUsername("system")
                .orElseThrow(() -> new IllegalStateException("找不到有效的審計用戶"));
    }
}
```

### 4. 測試指令

準備測試環境：

```bash
# 確保系統用戶和測試用戶存在
INSERT INTO pf_user (username, name, description, email, password, status_id, created_by, created_time, modified_by, modified_time) 
VALUES ('system', '系統用戶', '系統管理員用戶', 'system@example.com', 'none', '1', 'system', now(), 'system', now()),
       ('test-token', '測試用戶', '測試用戶帳號', 'test@example.com', 'none', '1', 'system', now(), 'system', now());
```

創建記錄:

```bash
curl -X POST http://localhost:8080/api/demo-complex-audit \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer test-token" \
     -d '{"name":"測試實體關聯審計","description":"這是一個測試實體關聯審計方式的範例"}'
```

回應:

```json
{
  "id": 1,
  "name": "測試實體關聯審計",
  "description": "這是一個測試實體關聯審計方式的範例",
  "createdByUser": {
    "id": 2,
    "name": "測試用戶",
    "email": "test@example.com",
    "description": "測試用戶帳號",
    "username": "test-token",
    ...
  },
  "createdTime": "2025-04-09 10:47:04",
  "lastModifiedByUser": {
    "id": 2,
    "name": "測試用戶",
    ...
  },
  "lastModifiedTime": "2025-04-09 10:47:04",
  "version": 0
}
```

更新記錄:

```bash
curl -X PUT http://localhost:8080/api/demo-complex-audit/1 \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer admin-token" \
     -d '{"name":"已更新的測試實體關聯審計","description":"這是一個測試實體關聯審計方式的更新範例"}'
```

回應:

```json
{
  "id": 1,
  "name": "已更新的測試實體關聯審計",
  "description": "這是一個測試實體關聯審計方式的更新範例",
  "createdByUser": {
    "id": 2,
    "name": "測試用戶",
    ...
  },
  "createdTime": "2025-04-09 10:47:04",
  "lastModifiedByUser": {
    "id": 1,
    "name": "系統用戶",
    ...
  },
  "lastModifiedTime": "2025-04-09 10:47:15",
  "version": 1
}
```

### 5. 實體關聯審計方式的優點

1. **完整數據關聯**: 直接關聯到用戶實體，可以獲得用戶的完整信息
2. **數據完整性**: 通過外鍵約束確保審計數據的完整性和一致性
3. **查詢便利性**: 可以直接通過關聯獲取審計用戶的詳細信息
4. **業務語義清晰**: 審計字段的命名和關聯更符合業務語義

這種方式特別適合需要保存完整審計用戶信息和強關聯的場景，與基於字符串ID的審計方式相比，提供了更豐富的審計信息和更強的數據完整性。 