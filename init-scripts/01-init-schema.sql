-- 創建審計演示所需的數據庫結構

-- 創建使用者表 pf_user
CREATE TABLE pf_user
(
    id               bigserial
        constraint pf_user_pk
            primary key,
    name             varchar(100),
    description      varchar(100)                          not null,
    username         varchar(255)                          not null
        constraint pf_user_un
            unique,
    password         varchar(255)                          not null,
    email            varchar(255),
    cellphone        varchar(20),
    company_id       varchar(100),
    status_id        varchar(20)                           not null,
    -- 審計欄位 (Spring Data JPA Auditing)
    created_by       bigint                                not null,
    created_company  varchar(100),
    created_unit     varchar(100),
    created_name     varchar(100),
    created_time     timestamp   default now()             not null,
    modified_by      bigint                                not null,
    modified_company varchar(100),
    modified_unit    varchar(100),
    modified_name    varchar(100),
    modified_time    timestamp   default now()             not null,
    default_language varchar(20) default ''::character varying
);

-- 添加表和欄位註釋
COMMENT ON TABLE pf_user IS '使用者資料表';
COMMENT ON COLUMN pf_user.id IS '使用者編號';
COMMENT ON COLUMN pf_user.name IS '使用者姓名';
COMMENT ON COLUMN pf_user.description IS '使用者描述';
COMMENT ON COLUMN pf_user.username IS '使用者帳號';
COMMENT ON COLUMN pf_user.password IS '使用者密碼';
COMMENT ON COLUMN pf_user.email IS '電子郵件';
COMMENT ON COLUMN pf_user.cellphone IS '手機號碼';
COMMENT ON COLUMN pf_user.company_id IS '公司編號';
COMMENT ON COLUMN pf_user.status_id IS '狀態編號';
-- 審計欄位註釋
COMMENT ON COLUMN pf_user.created_by IS '建立人員ID';
COMMENT ON COLUMN pf_user.created_company IS '建立人員公司';
COMMENT ON COLUMN pf_user.created_unit IS '建立人員單位';
COMMENT ON COLUMN pf_user.created_name IS '建立人員姓名';
COMMENT ON COLUMN pf_user.created_time IS '建立時間';
COMMENT ON COLUMN pf_user.modified_by IS '修改人員ID';
COMMENT ON COLUMN pf_user.modified_company IS '修改人員公司';
COMMENT ON COLUMN pf_user.modified_unit IS '修改人員單位';
COMMENT ON COLUMN pf_user.modified_name IS '修改人員姓名';
COMMENT ON COLUMN pf_user.modified_time IS '修改時間';
COMMENT ON COLUMN pf_user.default_language IS '預設語言';

-- 添加外鍵約束（自參照）
ALTER TABLE pf_user ADD CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES pf_user(id);
ALTER TABLE pf_user ADD CONSTRAINT fk_modified_by FOREIGN KEY (modified_by) REFERENCES pf_user(id);

-- 創建一個系統管理員用戶，用於解決循環依賴問題
-- 由於外鍵約束，我們需要暫時禁用它們
ALTER TABLE pf_user DISABLE TRIGGER ALL;

INSERT INTO pf_user (
    id, username, password, name, description, 
    email, status_id, created_by, modified_by, 
    created_time, modified_time,
    created_company, created_unit, created_name,
    modified_company, modified_unit, modified_name
) VALUES (
    1, 'system', '$2a$10$VxLkSPCIBj49g0zIX8aHUeE5L86VqZP/7geO3lk.i3tjEZVN0/hEy', 
    '系統管理員', '系統內建管理員帳戶', 
    'system@example.com', 'ACTIVE', 1, 1, 
    NOW(), NOW(),
    '系統', '系統', '系統管理員',
    '系統', '系統', '系統管理員'
);

-- 啟用觸發器
ALTER TABLE pf_user ENABLE TRIGGER ALL;

-- 創建顧客表
CREATE TABLE pf_customer
(
    id               bigserial
        constraint pf_customer_pk
            primary key,
    name             varchar(100),
    email            varchar(255),
    phone            varchar(20),
    address          varchar(255),
    company          varchar(100),
    
    -- 標準審計欄位 (Spring Data JPA Auditing)
    created_by       bigint                                not null,
    created_time     timestamp   default now()             not null,
    modified_by      bigint                                not null,
    modified_time    timestamp   default now()             not null,

    -- 擴充審計欄位
    created_company  varchar(100),
    created_unit     varchar(100),
    created_name     varchar(100),
    
    modified_company varchar(100),
    modified_unit    varchar(100),
    modified_name    varchar(100),
    
    CONSTRAINT fk_customer_created_by FOREIGN KEY (created_by) REFERENCES pf_user(id),
    CONSTRAINT fk_customer_modified_by FOREIGN KEY (modified_by) REFERENCES pf_user(id)
);

COMMENT ON TABLE pf_customer IS '顧客資料表';
COMMENT ON COLUMN pf_customer.id IS '顧客編號';
COMMENT ON COLUMN pf_customer.name IS '顧客名稱';
COMMENT ON COLUMN pf_customer.email IS '電子郵件';
COMMENT ON COLUMN pf_customer.phone IS '電話號碼';
COMMENT ON COLUMN pf_customer.address IS '地址';
COMMENT ON COLUMN pf_customer.company IS '公司名稱';
COMMENT ON COLUMN pf_customer.created_by IS '建立人員ID';
COMMENT ON COLUMN pf_customer.created_time IS '建立時間';
COMMENT ON COLUMN pf_customer.modified_by IS '修改人員ID';
COMMENT ON COLUMN pf_customer.modified_time IS '修改時間';
