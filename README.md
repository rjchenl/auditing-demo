# Spring Data JPA 審計功能 POC

這個項目是Spring Data JPA審計功能的概念驗證(POC)，專注於實現產品化的審計欄位設計。

## 功能需求

- 產品化設計需要儲存 createdBy 的 user 詳細資訊（company, unit, name）
- 使用 Spring Data JPA Auditing 的設計從 token 取出 userId 查詢相關資訊後儲存到 table

## 技術棧

- Spring Boot 3.4.4
- Spring Data JPA
- PostgreSQL
- Maven
- Docker & Docker Compose

## 實現方式

本POC使用Spring Data JPA Auditing框架實現審計功能：

1. **@EnableJpaAuditing**: 開啟Spring Data JPA審計功能
2. **@CreatedBy, @LastModifiedBy**: 自動填充創建和修改者ID
3. **@CreatedDate, @LastModifiedDate**: 自動填充創建和修改時間
4. **AuditorAware**: 提供當前用戶ID
5. **EntityListeners**: 使用實體監聽器填充擴展審計欄位(公司、部門、姓名等)

## 審計設計架構

![Auditing Architecture](docs/images/auditing-architecture.png)

### 主要組件

1. **JpaAuditingConfiguration**: 配置審計功能和AuditorAware
2. **CustomAuditorAware**: 獲取當前用戶ID（實際環境可從JWT Token獲取）
3. **UserAuditListener**: 實體監聽器，負責填充擴展審計欄位
4. **User實體**: 包含審計欄位和注解，實現審計功能

## 快速開始

### 前提條件

- Docker和Docker Compose已安裝
- Java Development Kit (JDK) 21
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

### API測試

可使用以下curl命令測試審計功能：

#### 創建用戶（模擬kenbai用戶操作）

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "X-User-Id: kenbai" \
  -d '{
    "description": "測試用戶",
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "statusId": "0",
    "defaultLanguage": "zh-tw"
  }'
```

#### 更新用戶（模擬peter用戶操作）

```bash
curl -X PUT http://localhost:8080/api/users/{user_id} \
  -H "Content-Type: application/json" \
  -H "X-User-Id: peter" \
  -d '{
    "email": "updated@example.com",
    "cellphone": "0912345678"
  }'
```

#### 查看審計信息

```bash
curl http://localhost:8080/api/users/audit
```

## 關鍵代碼說明

### JPA審計配置

```java
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new CustomAuditorAware();
    }
}
```

### 自定義AuditorAware實現

```java
@Component
public class CustomAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // 從UserContext獲取當前用戶ID
        // 實際應用中，可從JWT Token或Security Context獲取
        String currentUser = UserContext.getCurrentUser();
        return Optional.ofNullable(currentUser).or(() -> Optional.of("system"));
    }
}
```

### User實體中的審計欄位

```java
@Entity
@Table(name = "pf_user", schema = "icp")
@EntityListeners({AuditingEntityListener.class, UserAuditListener.class})
public class User {
    
    // 基本欄位...
    
    // Spring Data JPA審計欄位
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private Instant createdTime;
    
    @LastModifiedBy
    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;
    
    @LastModifiedDate
    @Column(name = "modified_time", nullable = false)
    private Instant modifiedTime;
    
    // 擴展審計欄位
    @Column(name = "created_company")
    private String createdCompany;
    
    @Column(name = "created_unit")
    private String createdUnit;
    
    @Column(name = "created_name")
    private String createdName;
    
    @Column(name = "modified_company")
    private String modifiedCompany;
    
    @Column(name = "modified_unit")
    private String modifiedUnit;
    
    @Column(name = "modified_name")
    private String modifiedName;
    
    // ...
}
```

### 用戶詳細信息審計監聽器

```java
@Component
public class UserAuditListener {
    
    private static UserInfoRepository userInfoRepository;
    
    @Autowired
    public void setUserInfoRepository(UserInfoRepository userInfoRepository) {
        UserAuditListener.userInfoRepository = userInfoRepository;
    }
    
    @PrePersist
    public void prePersist(User user) {
        fillUserDetails(user, user.getCreatedBy(), true);
    }
    
    @PreUpdate
    public void preUpdate(User user) {
        fillUserDetails(user, user.getModifiedBy(), false);
    }
    
    private void fillUserDetails(User user, String userId, boolean isCreated) {
        // 查詢用戶詳細信息並填充到審計欄位
        UserInfo userInfo = userInfoRepository.findById(userId).orElse(null);
        if (userInfo != null) {
            if (isCreated) {
                user.setCreatedCompany(userInfo.getCompany());
                user.setCreatedUnit(userInfo.getUnit());
                user.setCreatedName(userInfo.getName());
            } else {
                user.setModifiedCompany(userInfo.getCompany());
                user.setModifiedUnit(userInfo.getUnit());
                user.setModifiedName(userInfo.getName());
            }
        }
    }
}
```

## 數據庫訪問

PostgreSQL連接信息：
- 主機：localhost
- 端口：5432
- 數據庫：auditing
- 用戶名：postgres
- 密碼：postgres

pgAdmin訪問信息：
- URL：http://localhost:5050
- 郵箱：admin@example.com
- 密碼：admin 

## 審計Demo詳細說明

為了更直觀地展示審計流程和數據關聯，本項目包含了一個專門的審計Demo控制器。以下是詳細的演示步驟和說明：

### 數據表關係說明

本Demo包含兩個核心表：

1. **pf_user_info表** - 存儲用戶詳細信息
   - 主鍵：`user_id` (如 "kenbai", "peter" 等)
   - 包含用戶的組織信息：公司(company)、部門(unit)、姓名(name)
   - 這些記錄在系統啟動時由`DataInitializer`自動創建

2. **pf_user表** - 存儲用戶帳號及審計信息
   - 主鍵：`user_uid` (UUID格式)
   - 標準審計欄位：`created_by`, `created_time`, `modified_by`, `modified_time`
   - 擴展審計欄位：`created_company`, `created_unit`, `created_name`, `modified_company`, `modified_unit`, `modified_name`

### 核心關聯
- `pf_user.created_by` 對應 `pf_user_info.user_id`
- `pf_user.modified_by` 對應 `pf_user_info.user_id`

### 審計流程演示

以下API可幫助您清晰理解整個審計流程：

#### 1. 查看所有用戶信息(UserInfo)

```bash
curl http://localhost:8080/api/audit-demo/user-info
```

這會返回所有可用於審計的用戶信息，類似於：
```json
[
  {
    "userId": "kenbai", 
    "company": "TPIsoftware",
    "unit": "研發一處",
    "name": "白建鈞"
  },
  {
    "userId": "peter", 
    "company": "TPIsoftware",
    "unit": "研發二處",
    "name": "游XX"
  }
  // 其他用戶...
]
```

#### 2. 使用審計演示API創建用戶

```bash
curl -X POST http://localhost:8080/api/audit-demo/create-with-audit \
  -H "Content-Type: application/json" \
  -H "X-User-Id: kenbai" \
  -d '{
    "description": "審計演示用戶",
    "username": "demo-user",
    "password": "password123",
    "email": "demo@example.com",
    "statusId": "0"
  }'
```

這會返回詳細的審計流程信息：
```json
{
  "userId": "UUID格式的ID",
  "username": "demo-user",
  "auditInfo": {
    "createdBy": "kenbai",
    "createdTime": "2025-04-07 14:30:00",
    "createdCompany": "TPIsoftware",
    "createdUnit": "研發一處",
    "createdName": "白建鈞"
  },
  "operatorFromUserInfo": {
    "userId": "kenbai",
    "name": "白建鈞",
    "company": "TPIsoftware",
    "unit": "研發一處"
  },
  "auditProcess": [
    "1. 從HTTP頭獲取操作者ID: kenbai",
    "2. 保存到UserContext的ThreadLocal中",
    "3. SpringData JPA通過CustomAuditorAware獲取操作者ID並設置created_by",
    "4. UserAuditListener從User.createdBy獲取ID (kenbai)",
    "5. 使用這個ID從UserInfo表查詢操作者詳細資料",
    "6. 填充擴展審計欄位: created_company, created_unit, created_name"
  ]
}
```

#### 3. 查看所有用戶的審計詳情

```bash
curl http://localhost:8080/api/audit-demo/audit-with-details
```

這會返回所有用戶的審計信息，明確展示`pf_user_info`和`pf_user`審計欄位的關聯：
```json
[
  {
    "userId": "UUID格式的ID",
    "username": "demo-user",
    "createdBy": "kenbai",
    "createdTime": "2025-04-07 14:30:00",
    "creatorDetails": {
      "userId": "kenbai",
      "name": "白建鈞",
      "company": "TPIsoftware",
      "unit": "研發一處"
    },
    "modifiedBy": "kenbai",
    "modifiedTime": "2025-04-07 14:30:00",
    "modifierDetails": {
      "userId": "kenbai",
      "name": "白建鈞",
      "company": "TPIsoftware",
      "unit": "研發一處"
    }
  }
]
```

### 審計數據流說明

1. **操作用戶ID來源**：
   - HTTP請求頭：`X-User-Id: kenbai`
   - 進入`UserContext.currentUser` (ThreadLocal變量)
   - 被`CustomAuditorAware.getCurrentAuditor()`返回給Spring Data JPA
   - Spring Data JPA將其設置為`pf_user.created_by = "kenbai"`

2. **擴展欄位填充流程**：
   - `UserAuditListener.prePersist()`被觸發
   - 從`pf_user.created_by`獲取用戶ID值："kenbai"
   - 使用此ID查詢`pf_user_info`表：`SELECT * FROM pf_user_info WHERE user_id = 'kenbai'`
   - 獲取查詢結果並填充用戶擴展欄位：
     - `pf_user.created_company = pf_user_info.company`  
     - `pf_user.created_unit = pf_user_info.unit`
     - `pf_user.created_name = pf_user_info.name`

### 為什麼需要兩個表？

1. **數據分離原則**：
   - `pf_user`表：存儲用戶帳號信息
   - `pf_user_info`表：存儲用戶詳細資料和組織信息

2. **審計需求**：
   - 只有操作者ID會被記錄在審計字段中（誰做了操作）
   - 但我們需要知道操作者當時的組織信息（公司、部門、姓名）
   - 用戶的組織信息可能隨時間變化（調職、晉升等）

3. **資料完整性**：
   - 操作發生時，系統查詢並記錄當時操作者的組織信息
   - 即使操作者信息後來變更，歷史審計記錄仍保持不變，確保資料準確性
 