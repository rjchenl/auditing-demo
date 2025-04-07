-- 創建審計演示所需的數據庫結構

-- 創建icp模式
CREATE SCHEMA IF NOT EXISTS icp;

-- 創建使用者表
CREATE TABLE icp.pf_user
(
    user_uid         uuid        default gen_random_uuid() not null
        constraint pf_user_pk
            primary key,
    description      varchar(100)                          not null,
    username         varchar(255)                          not null
        constraint pf_user_un
            unique,
    password         varchar(255)                          not null,
    email            varchar(255),
    cellphone        varchar(20),
    company_id       varchar(100),
    status_id        varchar(20)                           not null,
    -- 審計欄位：舊的欄位保留以兼容現有數據，但將使用新的審計欄位
    create_time      timestamp   default now()             not null,
    create_user_id   varchar(20)                           not null,
    update_time      timestamp   default now()             not null,
    update_user_id   varchar(20)                           not null,
    -- 新的標準化審計欄位
    created_by       varchar(100)                          not null,
    created_company  varchar(100),
    created_unit     varchar(100),
    created_name     varchar(100),
    created_time     timestamp   default now()             not null,
    modified_by      varchar(100)                          not null,
    modified_company varchar(100),
    modified_unit    varchar(100),
    modified_name    varchar(100),
    modified_time    timestamp   default now()             not null,
    default_language varchar(20) default ''::character varying
);

-- 添加表和欄位註釋
COMMENT ON TABLE icp.pf_user IS '使用者資料表';
COMMENT ON COLUMN icp.pf_user.user_uid IS '使用者 uid';
COMMENT ON COLUMN icp.pf_user.description IS '使用者';
COMMENT ON COLUMN icp.pf_user.username IS '使用者名稱';
COMMENT ON COLUMN icp.pf_user.password IS '使用者密碼';
COMMENT ON COLUMN icp.pf_user.email IS '電子信箱';
COMMENT ON COLUMN icp.pf_user.cellphone IS '手機';
COMMENT ON COLUMN icp.pf_user.company_id IS '公司';
COMMENT ON COLUMN icp.pf_user.status_id IS '狀態';
-- 舊審計欄位註釋
COMMENT ON COLUMN icp.pf_user.create_time IS '創建時間 (舊)';
COMMENT ON COLUMN icp.pf_user.create_user_id IS '創建人員 (舊)';
COMMENT ON COLUMN icp.pf_user.update_time IS '更新時間 (舊)';
COMMENT ON COLUMN icp.pf_user.update_user_id IS '更新人員 (舊)';
-- 新審計欄位註釋
COMMENT ON COLUMN icp.pf_user.created_by IS '創建人員ID';
COMMENT ON COLUMN icp.pf_user.created_company IS '創建人員公司';
COMMENT ON COLUMN icp.pf_user.created_unit IS '創建人員單位';
COMMENT ON COLUMN icp.pf_user.created_name IS '創建人員姓名';
COMMENT ON COLUMN icp.pf_user.created_time IS '創建時間';
COMMENT ON COLUMN icp.pf_user.modified_by IS '修改人員ID';
COMMENT ON COLUMN icp.pf_user.modified_company IS '修改人員公司';
COMMENT ON COLUMN icp.pf_user.modified_unit IS '修改人員單位';
COMMENT ON COLUMN icp.pf_user.modified_name IS '修改人員姓名';
COMMENT ON COLUMN icp.pf_user.modified_time IS '修改時間';
COMMENT ON COLUMN icp.pf_user.default_language IS '預設語言';

-- 創建一個用戶資訊表，用於模擬從token獲取的用戶詳細信息
CREATE TABLE icp.pf_user_info
(
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

-- 插入示例資料 (插入舊的用戶數據，但也添加新的審計欄位)
INSERT INTO icp.pf_user (
    user_uid, description, username, password, email, cellphone, company_id, status_id, 
    create_time, create_user_id, update_time, update_user_id,
    created_by, created_company, created_unit, created_name, created_time,
    modified_by, modified_company, modified_unit, modified_name, modified_time,
    default_language
)
VALUES 
('085874f6-f285-423e-9cd1-4982ae365a67', 'kenbai', 'kenbai', '$2a$10$ttOHuu6fAws6dplrJhh8xOfjlP7vRFenVRoDN0SxGJjSb/hzVLEPq', 'A@A.com', '0999999999', 'CLI', '0', 
 '2024-05-16 14:06:49.057397', 'anonymousUser', '2024-05-16 14:06:49.057397', 'anonymousUser',
 'anonymousUser', 'System', 'System', 'System', '2024-05-16 14:06:49.057397',
 'anonymousUser', 'System', 'System', 'System', '2024-05-16 14:06:49.057397',
 'zh-tw'),
('eef87711-2be2-4b61-9482-da3926b0ebe4', 'shawn', 'shawn', '$2a$10$KF65IZMFC4LpjywmxBQuIu/PnKLDngaXJBF62oWzPRqqDkNOZqbTq', 'shawn.lin@tpisoftware.com', NULL, NULL, '0', 
 '2024-05-23 16:35:41.447175', 'anonymousUser', '2024-05-23 16:35:41.447175', 'anonymousUser',
 'anonymousUser', 'System', 'System', 'System', '2024-05-23 16:35:41.447175',
 'anonymousUser', 'System', 'System', 'System', '2024-05-23 16:35:41.447175',
 'zh-tw'),
('8c65fb84-9172-45f5-849a-aafdaf866021', 'janice', 'janice', '$2a$10$/.Z9eyK/mVzEb8EVHsqwr.c0MrYUWf0EuKS2VtVFFjbt5ZndHlXF6', 'janice.chiang@tpisoftware.com', NULL, NULL, '0', 
 '2024-05-02 09:40:23.367392', 'anonymousUser', '2024-05-02 09:40:27.897145', 'anonymousUser',
 'anonymousUser', 'System', 'System', 'System', '2024-05-02 09:40:23.367392',
 'anonymousUser', 'System', 'System', 'System', '2024-05-02 09:40:27.897145',
 'zh-tw'),
('2dd62558-cf0a-41bb-ad32-692fd467d14d', 'sunya', 'sunya', '$2a$10$YG7eLd8l.qbkfP4.xRX.deZTMl7Wk0F3JziOKQ4/Ob.Stqa1O2JJ2', 'sunya@abc.com', NULL, NULL, '0', 
 '2024-04-30 18:46:15.241267', 'anonymousUser', '2024-05-02 12:01:21.851139', 'anonymousUser',
 'anonymousUser', 'System', 'System', 'System', '2024-04-30 18:46:15.241267',
 'anonymousUser', 'System', 'System', 'System', '2024-05-02 12:01:21.851139',
 'zh-tw'),
('40b69358-140c-49cb-833c-9e355c33f393', 'peter', 'peter', '$2a$10$9khg/C5UW7vRldj5vWRO4Ow3xWfRzqxqzjzEz.iCKm7Gujw.VtM4a', 'peter.yu@tpisoftware.com', '09xxxxxxxx', 'CLI', '0', 
 '2024-05-02 13:49:23.865080', 'anonymousUser', '2024-05-02 13:49:23.865080', 'anonymousUser',
 'anonymousUser', 'System', 'System', 'System', '2024-05-02 13:49:23.865080',
 'anonymousUser', 'System', 'System', 'System', '2024-05-02 13:49:23.865080',
 'zh-tw');

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

-- 為pf_user表添加審計觸發器
CREATE TRIGGER pf_user_audit_trigger
BEFORE INSERT OR UPDATE ON icp.pf_user
FOR EACH ROW EXECUTE FUNCTION icp.update_audit_fields();

-- 設置當前用戶的函數
CREATE OR REPLACE FUNCTION icp.simulate_user_operation(username VARCHAR)
RETURNS VOID AS $$
BEGIN
    -- 設置當前用戶環境變量
    PERFORM set_config('app.current_user', username, FALSE);
END;
$$ LANGUAGE plpgsql;

-- 說明如何設置當前用戶
COMMENT ON FUNCTION icp.update_audit_fields() IS '
使用以下SQL設置當前用戶:
SELECT set_config(''app.current_user'', ''用戶名'', FALSE);
或使用:
SELECT icp.simulate_user_operation(''用戶名'');
';

-- 授予權限
GRANT USAGE ON SCHEMA icp TO PUBLIC;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA icp TO postgres; 