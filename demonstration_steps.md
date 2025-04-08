# 環境配置API與審計欄位測試步驟

本文檔展示如何使用cURL命令測試環境配置API及其審計欄位實現。通過這些步驟，您可以驗證介面方式實現的審計欄位是否按預期工作。

## 前提條件

- 已啟動PostgreSQL數據庫（使用docker-compose up -d）
- 已啟動Spring Boot應用（使用./mvnw spring-boot:run）

## 1. 創建環境配置

首先，我們創建一個新的環境配置，使用kenbai的身份：

```bash
curl -X POST http://localhost:8080/api/environments \
  -H "Content-Type: application/json" \
  -H "Authorization: kenbai" \
  -d '{
    "name": "測試環境",
    "description": "介面實現測試的環境配置",
    "type": "TEST",
    "configValue": "{\"server\":\"test.example.com\",\"port\":8080}",
    "status": 0
  }'
```

預期結果：
- 成功創建環境配置
- HTTP狀態碼：201 Created
- 響應中包含創建的環境配置內容

## 2. 檢查審計欄位

我們可以查看剛剛創建的環境配置的審計欄位：

```bash
curl -X GET http://localhost:8080/api/environments/1/audit-fields | jq
```

預期結果：
- 標準審計欄位：
  - `created_by`: "kenbai"
  - `modified_by`: "kenbai"
  - `created_time`: 當前時間戳
  - `modified_time`: 當前時間戳
- 擴展審計欄位：
  - `created_company`: "拓連科技"
  - `created_unit`: "行銷部"
  - `modified_company`: "拓連科技"
  - `modified_unit`: "行銷部"
- 環境特有審計欄位：
  - `version`: "1.0"
  - `status`: 0
  - 審核和部署相關欄位為null

## 3. 更新環境配置

現在使用peter的身份更新環境配置：

```bash
curl -X PUT http://localhost:8080/api/environments/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: peter" \
  -d '{
    "description": "已更新的測試環境",
    "configValue": "{\"server\":\"updated.example.com\",\"port\":8888}"
  }'
```

預期結果：
- 成功更新環境配置
- HTTP狀態碼：200 OK
- 響應中包含更新的環境配置內容

## 4. 再次檢查審計欄位

再次檢查環境配置的審計欄位：

```bash
curl -X GET http://localhost:8080/api/environments/1/audit-fields | jq
```

預期結果：
- 標準審計欄位：
  - `created_by`: "kenbai" (不變)
  - `created_time`: 創建時間 (不變)
  - `modified_by`: "peter" (已更新)
  - `modified_time`: 當前時間戳 (已更新)
- 擴展審計欄位：
  - `created_company`: "拓連科技" (不變)
  - `created_unit`: "行銷部" (不變)
  - `modified_company`: "拓連科技" (已更新)
  - `modified_unit`: "研發部" (已更新)

## 5. 審核環境配置

使用shawn的身份進行審核：

```bash
curl -X POST http://localhost:8080/api/environments/1/review \
  -H "Authorization: shawn"
```

預期結果：
- 成功審核環境配置
- HTTP狀態碼：200 OK
- 響應中包含審核後的環境配置，其中審核相關欄位已填充

## 6. 檢查審核後的審計欄位

檢查審核後的審計欄位：

```bash
curl -X GET http://localhost:8080/api/environments/1/audit-fields | jq
```

預期結果：
- 環境特有審計欄位：
  - `reviewed_by`: "shawn"
  - `reviewed_time`: 當前時間戳
  - `reviewed_company`: "拓連科技"
  - `reviewed_unit`: "產品部"
  - `status`: 2 (已審核)
- 標準審計欄位：
  - `modified_by`: "shawn" (已更新)
  - `modified_time`: 當前時間戳 (已更新)
- 擴展審計欄位：
  - `modified_company`: "拓連科技" (不變)
  - `modified_unit`: "產品部" (已更新)

## 7. 部署環境配置

使用system身份進行部署：

```bash
curl -X POST http://localhost:8080/api/environments/1/deploy \
  -H "Content-Type: application/json" \
  -H "Authorization: system" \
  -d '{
    "version": "1.5"
  }'
```

預期結果：
- 成功部署環境配置
- HTTP狀態碼：200 OK
- 響應中包含部署後的環境配置，其中部署相關欄位已填充

## 8. 檢查部署後的審計欄位

檢查部署後的審計欄位：

```bash
curl -X GET http://localhost:8080/api/environments/1/audit-fields | jq
```

預期結果：
- 環境特有審計欄位：
  - `deployed_by`: "system"
  - `deployed_time`: 當前時間戳
  - `deployed_company`: "系統"
  - `deployed_unit`: "系統"
  - `version`: "1.5" (指定的版本)
  - `status`: 3 (已部署)
- 標準審計欄位：
  - `modified_by`: "system" (已更新)
  - `modified_time`: 當前時間戳 (已更新)
- 擴展審計欄位：
  - `modified_company`: "系統" (已更新)
  - `modified_unit`: "系統" (已更新)

## 總結

通過以上步驟，我們可以驗證環境配置實體在使用介面方式實現審計欄位後，是否能夠正確捕獲和記錄各種審計信息，包括：

1. 標準審計欄位（Spring Data JPA提供）
2. 擴展審計欄位（自定義）
3. 環境特有審計欄位（特殊業務需求）

所有這些審計欄位都應該能夠正確工作，表明從註解方式切換到介面方式實現審計功能是成功的。 