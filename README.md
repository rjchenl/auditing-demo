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

## 功能特色

- **混合式審計架構**：結合 Spring Data JPA 標準審計功能與自訂介面擴展
- **層次化審計介面**：使用介面繼承提供彈性的擴展結構
- **業務特定審計**：支援複雜業務場景的特殊審計需求（如審核、部署流程）
- **可擴展設計**：易於根據業務需求進行擴展和客製化

## 審計架構設計

本專案使用混合式審計架構，結合 Spring Data JPA 的標準審計功能與自訂介面擴展：

![審計架構](https://example.com/audit-architecture.png)

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
 