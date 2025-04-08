-- 創建審計演示所需的數據庫結構

-- 創建使用者表
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

-- 創建 API 表格
CREATE TABLE pf_api
(
    id               bigserial
        constraint pf_api_pk
            primary key,
    apiname          varchar(255)                          not null
        constraint pf_api_un
            unique,
    description      varchar(255),
    -- 審計欄位 (Spring Data JPA Auditing)
    created_by       varchar(100)                          not null,
    created_company  varchar(100),
    created_unit     varchar(100),
    created_name     varchar(100),
    created_time     timestamp   default now()             not null,
    modified_by      varchar(100)                          not null,
    modified_company varchar(100),
    modified_unit    varchar(100),
    modified_name    varchar(100),
    modified_time    timestamp   default now()             not null
);

-- 添加表和欄位註釋
COMMENT ON TABLE pf_api IS 'API資料表';
COMMENT ON COLUMN pf_api.id IS 'API編號';
COMMENT ON COLUMN pf_api.apiname IS 'API名稱';
COMMENT ON COLUMN pf_api.description IS 'API描述';
-- 審計欄位註釋
COMMENT ON COLUMN pf_api.created_by IS '建立人員ID';
COMMENT ON COLUMN pf_api.created_company IS '建立人員公司';
COMMENT ON COLUMN pf_api.created_unit IS '建立人員單位';
COMMENT ON COLUMN pf_api.created_name IS '建立人員姓名';
COMMENT ON COLUMN pf_api.created_time IS '建立時間';
COMMENT ON COLUMN pf_api.modified_by IS '修改人員ID';
COMMENT ON COLUMN pf_api.modified_company IS '修改人員公司';
COMMENT ON COLUMN pf_api.modified_unit IS '修改人員單位';
COMMENT ON COLUMN pf_api.modified_name IS '修改人員姓名';
COMMENT ON COLUMN pf_api.modified_time IS '修改時間';

-- 創建環境配置表
CREATE TABLE pf_environment
(
    id               bigserial
        constraint pf_environment_pk
            primary key,
    name             varchar(100)                          not null
        constraint pf_environment_un
            unique,
    description      varchar(255),
    type             varchar(50)                           not null,
    config_value     text,
    status           integer       default 0               not null,
    version          varchar(20)   default '1.0'           not null,
    -- 標準審計欄位 (Spring Data JPA Auditing)
    created_by       varchar(100)                          not null,
    created_time     timestamp     default now()           not null,
    modified_by      varchar(100)                          not null,
    modified_time    timestamp     default now()           not null,
    -- 擴展審計欄位
    created_company  varchar(100),
    created_unit     varchar(100),
    modified_company varchar(100),
    modified_unit    varchar(100),
    -- 環境配置特有審計欄位
    reviewed_by      varchar(100),
    reviewed_time    timestamp,
    reviewed_company varchar(100),
    reviewed_unit    varchar(100),
    deployed_by      varchar(100),
    deployed_time    timestamp,
    deployed_company varchar(100),
    deployed_unit    varchar(100)
);

-- 添加表和欄位註釋
COMMENT ON TABLE pf_environment IS '環境配置資料表';
COMMENT ON COLUMN pf_environment.id IS '環境編號';
COMMENT ON COLUMN pf_environment.name IS '環境名稱';
COMMENT ON COLUMN pf_environment.description IS '環境描述';
COMMENT ON COLUMN pf_environment.type IS '環境類型';
COMMENT ON COLUMN pf_environment.config_value IS '配置值';
COMMENT ON COLUMN pf_environment.status IS '狀態 (0:草稿, 1:審核中, 2:已審核, 3:已部署)';
COMMENT ON COLUMN pf_environment.version IS '版本號';
-- 審計欄位註釋
COMMENT ON COLUMN pf_environment.created_by IS '建立人員ID';
COMMENT ON COLUMN pf_environment.created_time IS '建立時間';
COMMENT ON COLUMN pf_environment.modified_by IS '修改人員ID';
COMMENT ON COLUMN pf_environment.modified_time IS '修改時間';
COMMENT ON COLUMN pf_environment.created_company IS '建立人員公司';
COMMENT ON COLUMN pf_environment.created_unit IS '建立人員單位';
COMMENT ON COLUMN pf_environment.modified_company IS '修改人員公司';
COMMENT ON COLUMN pf_environment.modified_unit IS '修改人員單位';
-- 特有審計欄位註釋
COMMENT ON COLUMN pf_environment.reviewed_by IS '審核人員ID';
COMMENT ON COLUMN pf_environment.reviewed_time IS '審核時間';
COMMENT ON COLUMN pf_environment.reviewed_company IS '審核人員公司';
COMMENT ON COLUMN pf_environment.reviewed_unit IS '審核人員單位';
COMMENT ON COLUMN pf_environment.deployed_by IS '部署人員ID';
COMMENT ON COLUMN pf_environment.deployed_time IS '部署時間';
COMMENT ON COLUMN pf_environment.deployed_company IS '部署人員公司';
COMMENT ON COLUMN pf_environment.deployed_unit IS '部署人員單位';


