# 層次化審計介面結構設計

## 概述

本專案實現了基於介面的層次化審計欄位結構，以取代原有的註解式審計方式。這種方式提供了更好的類型安全性、代碼結構和擴展性，特別適合有多種不同審計需求的複雜專案。

## 設計原則

1. **層次化繼承**：通過介面繼承實現不同層次的審計能力
2. **類型安全**：使用強類型介面確保審計欄位的完整性
3. **分離關注點**：將不同類型的審計邏輯分離到不同的監聽器中
4. **向後兼容**：保留對原有註解方式的支援，實現平滑遷移

## 介面結構

```
AuditableInterface (基礎審計介面)
├── UserAuditableInterface (用戶審計介面)
└── EnvironmentAuditableInterface (環境審計介面)
```

### 1. AuditableInterface

基礎審計介面，包含所有實體共用的標準審計欄位：

- 標準審計欄位（Spring Data JPA）
  - createdBy, createdTime
  - modifiedBy, modifiedTime
- 基本擴展審計欄位
  - createdCompany, createdUnit
  - modifiedCompany, modifiedUnit

### 2. UserAuditableInterface

擴展自 `AuditableInterface`，添加用戶相關審計欄位：

- 用戶特有欄位
  - createdName, modifiedName

適用於 `User` 和 `Api` 等實體。

### 3. EnvironmentAuditableInterface

擴展自 `AuditableInterface`，添加環境配置特有的審計欄位：

- 審核相關欄位
  - reviewedBy, reviewedTime, reviewedCompany, reviewedUnit
- 部署相關欄位
  - deployedBy, deployedTime, deployedCompany, deployedUnit
- 版本控制
  - version, status

## 監聽器實現

1. **AuditEntityListener**
   - 處理基礎和用戶審計欄位
   - 分別處理 `AuditableInterface` 和 `UserAuditableInterface`
   - 保留對舊式 `@Auditable` 註解的支援

2. **EnvironmentAuditListener**
   - 處理環境配置特有的審計需求
   - 實現審核和部署流程
   - 使用 `EnvironmentAuditableInterface` 進行類型安全操作

## 使用方法

### 1. 應用到實體類

```java
@Entity
@Table(name = "pf_user")
@EntityListeners({AuditingEntityListener.class, AuditEntityListener.class})
public class User implements UserAuditableInterface {
    // 實體欄位...
}
```

### 2. 添加新的審計類型

若要添加新的審計類型，只需建立新的介面繼承現有介面：

```java
public interface ProjectAuditableInterface extends AuditableInterface {
    // 項目特有審計欄位...
}
```

### 3. 擴展監聽器處理

```java
if (entity instanceof ProjectAuditableInterface) {
    processProjectAuditFields((ProjectAuditableInterface) entity, isCreate);
}
```

## 優勢

1. **結構清晰**：明確定義了不同實體的審計需求
2. **類型安全**：編譯時檢查確保審計欄位完整性
3. **易於擴展**：簡單添加新的介面和監聽器支援
4. **代碼複用**：避免重複實現相似的審計邏輯
5. **適應性強**：靈活應對不同的業務審計需求

## 遷移策略

1. 創建適當的審計介面
2. 修改實體類實現相應介面
3. 更新監聽器以支援介面方式
4. 逐步淘汰註解方式 