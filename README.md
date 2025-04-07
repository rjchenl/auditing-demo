# Spring Data JPA 審計功能 POC

這個項目是Spring Data JPA審計功能的概念驗證(POC)，專注於實現產品化的審計欄位設計。

## 功能需求

- 產品化設計需要儲存 createdBy 的 user 詳細資訊（company, unit, name）
- 使用 Auditing 的設計從 token 取出 userId 查詢相關資訊後需要儲存到 table

## 技術棧

- Spring Boot 3.4.4
- Spring Data JPA
- PostgreSQL
- Maven
- Docker & Docker Compose

## 快速開始

### 前提條件

- Docker和Docker Compose已安裝
- Java Development Kit (JDK) 21
- Maven

### 啟動服務

1. 啟動PostgreSQL數據庫和pgAdmin：

```bash
docker-compose up -d
```

**注意**: 本POC項目的PostgreSQL容器配置為非持久化，每次重啟都會重置數據。這是為了確保每次演示時有一個乾淨的環境。

### 審計欄位設計

本POC實現了以下審計欄位：

- `created_by` - 創建人員ID
- `created_company` - 創建人員公司
- `created_unit` - 創建人員單位
- `created_name` - 創建人員姓名
- `created_time` - 創建時間
- `modified_by` - 修改人員ID
- `modified_company` - 修改人員公司
- `modified_unit` - 修改人員單位
- `modified_name` - 修改人員姓名
- `modified_time` - 修改時間

同時保留了舊的審計欄位以確保兼容性：
- `create_time`
- `create_user_id`
- `update_time`
- `update_user_id`

## 完整演示步驟

本章節將帶您完整重現演示過程，展示審計功能的效果。

### 步驟1：查看初始數據

首先查看已初始化的用戶資料和審計欄位：

```bash
docker exec -it auditing-postgres psql -U postgres -d auditing -c "SELECT username, email, created_by, created_company, created_unit, created_name, created_time FROM icp.pf_user LIMIT 3;"
```

輸出結果：
```
 username |             email             |  created_by   | created_company | created_unit | created_name |        created_time
----------+-------------------------------+---------------+-----------------+--------------+--------------+---------------------------
 kenbai   | A@A.com                       | anonymousUser | System          | System       | System       | 2024-05-16 14:06:49.057397
 janice   | janice.chiang@tpisoftware.com | anonymousUser | System          | System       | System       | 2024-05-02 09:40:23.367392
 sunya    | sunya@abc.com                 | anonymousUser | System          | System       | System       | 2024-04-30 18:46:15.241267
```

### 步驟2：查看用戶資訊表

查看用於模擬從token獲取的用戶詳細信息的表：

```bash
docker exec -it auditing-postgres psql -U postgres -d auditing -c "SELECT * FROM icp.pf_user_info WHERE user_id = 'kenbai';"
```

輸出結果：
```
 user_id |   company   |   unit   |  name  
---------+-------------+----------+--------
 kenbai  | TPIsoftware | 研發一處 | 白建鈞
```

### 步驟3：模擬kenbai用戶操作

執行以下命令模擬kenbai用戶登入並更新資料：

```bash
docker exec -it auditing-postgres psql -U postgres -d auditing -c "BEGIN; SELECT icp.simulate_user_operation('kenbai'); UPDATE icp.pf_user SET email = 'shawn.from.kenbai@example.com' WHERE username = 'shawn'; COMMIT;"
```

輸出結果：
```
BEGIN
 simulate_user_operation 
-------------------------
 
(1 row)

UPDATE 1
COMMIT
```

### 步驟4：檢查審計欄位更新

查看更新後的數據及審計欄位值：

```bash
docker exec -it auditing-postgres psql -U postgres -d auditing -c "SELECT username, email, modified_by, modified_company, modified_unit, modified_name, modified_time FROM icp.pf_user WHERE username = 'shawn';"
```

輸出結果：
```
 username |             email             | modified_by | modified_company | modified_unit | modified_name |       modified_time
----------+-------------------------------+-------------+------------------+---------------+---------------+----------------------------
 shawn    | shawn.from.kenbai@example.com | kenbai      | TPIsoftware      | 研發一處      | 白建鈞        | 2025-04-07 01:17:04.599344
```

### 步驟5：模擬peter用戶新增資料

執行以下命令模擬peter用戶登入並新增資料：

```bash
docker exec -it auditing-postgres psql -U postgres -d auditing -c "BEGIN; SELECT icp.simulate_user_operation('peter'); INSERT INTO icp.pf_user (description, username, password, email, status_id, default_language) VALUES ('新測試用戶2', 'testuser2', '$2a$10$YG7eLd8l.qbkfP4.xRX.deZTMl7Wk0F3JziOKQ4/Ob.Stqa1O2JJ2', 'test2@example.com', '0', 'zh-tw'); COMMIT;"
```

輸出結果：
```
BEGIN
 simulate_user_operation 
-------------------------
 
(1 row)

INSERT 0 1
COMMIT
```

### 步驟6：檢查新增資料的審計欄位

查看新增資料的審計欄位值：

```bash
docker exec -it auditing-postgres psql -U postgres -d auditing -c "SELECT username, email, created_by, created_company, created_unit, created_name, modified_by, modified_company, modified_unit, modified_name FROM icp.pf_user WHERE username = 'testuser2';"
```

輸出結果：
```
 username  |       email       | created_by | created_company | created_unit | created_name | modified_by | modified_company | modified_unit | modified_name
-----------+-------------------+------------+-----------------+--------------+--------------+-------------+------------------+---------------+---------------
 testuser2 | test2@example.com | peter      | TPIsoftware     | 研發二處     | 游XX         | peter       | TPIsoftware      | 研發二處      | 游XX
```

### 步驟7：查看最近修改的資料

查看按修改時間排序的資料，檢查審計記錄：

```bash
docker exec -it auditing-postgres psql -U postgres -d auditing -c "SELECT username, email, created_by, created_name, modified_by, modified_name, modified_time FROM icp.pf_user ORDER BY modified_time DESC LIMIT 4;"
```

輸出結果：
```
 username  |             email             |  created_by   | created_name |  modified_by  | modified_name |       modified_time
-----------+-------------------------------+---------------+--------------+---------------+---------------+----------------------------
 testuser2 | test2@example.com             | peter         | 游XX         | peter         | 游XX          | 2025-04-07 01:17:22.789929
 shawn     | shawn.from.kenbai@example.com | anonymousUser | System       | kenbai        | 白建鈞        | 2025-04-07 01:17:04.599344
 testuser  | test@example.com              | peter         | 游XX         | peter         | 游XX          | 2025-04-07 00:53:55.227834
 kenbai    | A@A.com                       | anonymousUser | System       | anonymousUser | System        | 2024-05-16 14:06:49.057397
```

## 關鍵代碼說明

### 用戶詳細信息表

```sql
-- 創建一個用戶資訊表，用於模擬從token獲取的用戶詳細信息
CREATE TABLE icp.pf_user_info (
    user_id   varchar(100) PRIMARY KEY,
    company   varchar(100),
    unit      varchar(100),
    name      varchar(100) NOT NULL
);

-- 插入模擬的用戶資訊數據
INSERT INTO icp.pf_user_info (user_id, company, unit, name) VALUES
('kenbai', 'TPIsoftware', '研發一處', '白建鈞'),
('peter', 'TPIsoftware', '研發二處', '游XX'),
('shawn', 'TPIsoftware', '研發一處', '林XX'),
('janice', 'TPIsoftware', '研發三處', '姜XX'),
('sunya', 'TPIsoftware', '研發二處', '孫XX'),
('anonymousUser', 'System', 'System', 'System'),
('system', 'System', 'System', 'System');
```

### 審計觸發器

關鍵審計功能實現在這個觸發器函數中：

```sql
-- 創建審計觸發器函數
CREATE OR REPLACE FUNCTION icp.update_audit_fields()
RETURNS TRIGGER AS $$
DECLARE
    v_user_id VARCHAR;
    v_company VARCHAR;
    v_unit VARCHAR;
    v_name VARCHAR;
BEGIN
    -- 從環境變量獲取當前用戶ID
    v_user_id := current_setting('app.current_user', TRUE);
    
    -- 如果當前用戶設置不存在，使用默認值
    IF v_user_id IS NULL THEN
        v_user_id := 'system';
    END IF;
    
    -- 查詢用戶詳細信息
    SELECT company, unit, name 
    INTO v_company, v_unit, v_name
    FROM icp.pf_user_info
    WHERE user_id = v_user_id;
    
    -- 如果沒有找到用戶信息，使用默認值
    IF v_name IS NULL THEN
        v_company := 'Unknown';
        v_unit := 'Unknown';
        v_name := v_user_id;
    END IF;
    
    -- 僅在INSERT時設置創建信息
    IF TG_OP = 'INSERT' THEN
        -- 舊審計欄位（為了兼容性）
        NEW.create_time := CURRENT_TIMESTAMP;
        NEW.create_user_id := v_user_id;
        
        -- 新審計欄位
        NEW.created_by := v_user_id;
        NEW.created_company := v_company;
        NEW.created_unit := v_unit;
        NEW.created_name := v_name;
        NEW.created_time := CURRENT_TIMESTAMP;
    END IF;
    
    -- 在INSERT和UPDATE時都設置修改信息
    -- 舊審計欄位（為了兼容性）
    NEW.update_time := CURRENT_TIMESTAMP;
    NEW.update_user_id := v_user_id;
    
    -- 新審計欄位
    NEW.modified_by := v_user_id;
    NEW.modified_company := v_company;
    NEW.modified_unit := v_unit;
    NEW.modified_name := v_name;
    NEW.modified_time := CURRENT_TIMESTAMP;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### 模擬用戶操作函數

```sql
-- 設置當前用戶的函數
CREATE OR REPLACE FUNCTION icp.simulate_user_operation(username VARCHAR)
RETURNS VOID AS $$
BEGIN
    -- 設置當前用戶環境變量
    PERFORM set_config('app.current_user', username, FALSE);
END;
$$ LANGUAGE plpgsql;
```

## 總結與下一步

本POC成功驗證了產品化審計設計：

1. **自動獲取用戶信息**：從token（模擬使用環境變量）獲取用戶ID
2. **查詢完整用戶信息**：根據用戶ID獲取公司、單位和姓名
3. **審計欄位自動填充**：在增刪改操作時自動記錄完整審計信息
4. **兼容舊系統**：保留舊審計欄位同時支持新欄位

這個設計可以輕鬆整合到Spring Data JPA應用中：
- 實現`AuditorAware<String>`以從JWT token獲取當前用戶ID
- 添加審計欄位到實體類並使用`@LastModifiedBy`等注解
- 結合上述數據庫級別審計可實現全面的審計追蹤

## 資料庫訪問

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