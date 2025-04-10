# Spring Boot JPA 審計實作範例

本專案展示了在 Spring Boot 應用程式中實現審計功能的基本方法，專注於使用者活動的記錄和追蹤。

## 功能特點

- 標準審計欄位（創建者、創建時間、修改者、修改時間）
- 擴展審計欄位（創建者公司、單位、修改者公司、單位）
- 使用者實體及其審計追蹤

## 架構設計

### 核心組件

1. **審計介面**：定義審計欄位及其 Getter/Setter 方法
2. **實體監聽器**：自動監聽實體的持久化事件，設置審計欄位
3. **使用者上下文**：使用 ThreadLocal 保存當前使用者信息

### 審計介面階層

```
AuditableInterface (基本審計欄位)
└── UserAuditableInterface (使用者相關審計欄位)
```

## 實作範例

### 使用者審計

使用者實體實作了 `UserAuditableInterface`，包含標準和擴展審計欄位。

#### 使用者資料表結構

```sql
CREATE TABLE pf_user (
    id               bigserial PRIMARY KEY,
    name             varchar(100),
    description      varchar(100) NOT NULL,
    username         varchar(255) NOT NULL UNIQUE,
    password         varchar(255) NOT NULL,
    email            varchar(255),
    cellphone        varchar(20),
    company_id       varchar(100),
    status_id        varchar(20) NOT NULL,
    -- 審計欄位
    created_by       varchar(100) NOT NULL,
    created_company  varchar(100),
    created_unit     varchar(100),
    created_name     varchar(100),
    created_time     timestamp DEFAULT now() NOT NULL,
    modified_by      varchar(100) NOT NULL,
    modified_company varchar(100),
    modified_unit    varchar(100),
    modified_name    varchar(100),
    modified_time    timestamp DEFAULT now() NOT NULL,
    default_language varchar(20) DEFAULT ''
);
```

#### 使用者審計操作示例

##### 1. 創建使用者

```bash
curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer admin-token" \
     -d '{
         "username": "newuser",
         "name": "新使用者",
         "description": "新建立的使用者帳號",
         "email": "newuser@example.com",
         "password": "password123",
         "statusId": "1"
     }'
```

審計欄位將自動填充：
- `createdBy`: "admin-token"
- `createdTime`: 當前時間
- `modifiedBy`: "admin-token"
- `modifiedTime`: 當前時間

##### 2. 更新使用者

```bash
curl -X PUT http://localhost:8080/api/users/101 \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer system" \
     -d '{
         "name": "更新的使用者名稱",
         "email": "updated@example.com"
     }'
```

審計欄位變化：
- `createdBy`: 保持不變
- `createdTime`: 保持不變
- `modifiedBy`: 更新為 "system"
- `modifiedTime`: 更新為當前時間

##### 3. 查詢審計資訊

```bash
curl -X GET http://localhost:8080/api/users/audit
```

返回所有使用者的審計資訊，包括創建和修改的詳細記錄。

## 安裝與執行

### 前置需求

- Java 21
- Maven 3.8+
- PostgreSQL 14+

### 建置步驟

1. 克隆專案
   ```bash
   git clone https://github.com/yourusername/auditing-demo.git
   cd auditing-demo
   ```

2. 設定資料庫
   ```bash
   # 在 application.properties 中設定資料庫連接
   ```

3. 執行專案
   ```bash
   ./mvnw spring-boot:run
   ```

## API 文件

| 端點 | 方法 | 描述 |
|------|------|------|
| `/api/users` | GET | 獲取所有使用者 |
| `/api/users/{id}` | GET | 根據 ID 獲取使用者 |
| `/api/users` | POST | 創建新使用者 |
| `/api/users/{id}` | PUT | 更新使用者 |
| `/api/users/audit` | GET | 獲取所有使用者的審計資訊 |
