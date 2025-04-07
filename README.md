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
    "statusId": "0"
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
    @Primary
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
@Table(name = "pf_user")
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