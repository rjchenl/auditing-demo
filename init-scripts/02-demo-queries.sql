-- 演示如何使用審計功能的示例查詢

-- 模擬'kenbai'用戶登錄
SELECT icp.simulate_user_operation('kenbai');

-- 以kenbai用戶身份更新用戶資料
UPDATE icp.pf_user 
SET email = 'kenbai.updated@example.com', 
    cellphone = '0987654321' 
WHERE username = 'shawn';

-- 模擬'peter'用戶登錄
SELECT icp.simulate_user_operation('peter');

-- 以peter用戶身份添加新用戶
INSERT INTO icp.pf_user (
    description, 
    username, 
    password, 
    email, 
    status_id, 
    default_language
)
VALUES (
    '新測試用戶', 
    'testuser', 
    '$2a$10$YG7eLd8l.qbkfP4.xRX.deZTMl7Wk0F3JziOKQ4/Ob.Stqa1O2JJ2', 
    'test@example.com', 
    '0', 
    'zh-tw'
);

-- 查看完整的審計信息
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

-- 查看由'kenbai'用戶更新的記錄 (使用新的審計欄位)
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

-- 查看新創建的用戶記錄 (顯示由peter創建)
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

-- 比較新舊審計欄位 (用於演示向後兼容性)
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