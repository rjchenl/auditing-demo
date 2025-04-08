-- 演示如何使用審計功能的示例查詢

-- 模擬'kenbai'使用者登入
SELECT icp.simulate_user_operation('kenbai');

-- 以kenbai使用者身份更新使用者資料
UPDATE icp.pf_user 
SET email = 'kenbai.updated@example.com', 
    cellphone = '0987654321' 
WHERE username = 'shawn';

-- 模擬'peter'使用者登入
SELECT icp.simulate_user_operation('peter');

-- 以peter使用者身份新增使用者
INSERT INTO icp.pf_user (
    description, 
    username, 
    password, 
    email, 
    status_id, 
    default_language
)
VALUES (
    '新測試使用者', 
    'testuser', 
    '$2a$10$YG7eLd8l.qbkfP4.xRX.deZTMl7Wk0F3JziOKQ4/Ob.Stqa1O2JJ2', 
    'test@example.com', 
    '0', 
    'zh-tw'
);

-- 查看完整的審計資訊
SELECT 
    username, 
    email,
    created_by,
    created_company,
    created_unit,
    created_name,
    created_time,
    modified_by,
    modified_company,
    modified_unit,
    modified_name,
    modified_time
FROM icp.pf_user
ORDER BY modified_time DESC;

-- 查看由'kenbai'使用者更新的記錄 (使用新的審計欄位)
SELECT 
    username, 
    email, 
    modified_by,
    modified_company,
    modified_unit,
    modified_name,
    modified_time
FROM icp.pf_user
WHERE modified_by = 'kenbai'
ORDER BY modified_time DESC;

-- 查看新建立的使用者記錄 (顯示由peter建立)
SELECT 
    username, 
    email, 
    created_by,
    created_company,
    created_unit,
    created_name,
    created_time
FROM icp.pf_user
WHERE created_by = 'peter'
ORDER BY created_time DESC;

-- 比較新舊審計欄位 (用於演示向後相容性)
SELECT 
    username,
    -- 舊欄位
    create_user_id,
    create_time,
    update_user_id,
    update_time,
    -- 新欄位
    created_by,
    created_time,
    modified_by,
    modified_time
FROM icp.pf_user
ORDER BY modified_time DESC
LIMIT 3; 