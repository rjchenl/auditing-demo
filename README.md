# Spring Data JPA 審計功能 POC

這個項目是Spring Data JPA審計功能的概念驗證(POC)。

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

2. 訪問數據庫

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

### 運行應用

使用Maven運行應用：

```bash
./mvnw spring-boot:run
```

或者構建並運行JAR文件：

```bash
./mvnw clean package
java -jar target/auditing-demo-0.0.1-SNAPSHOT.jar
```

## 項目功能

- 實現基於Spring Data JPA的審計功能
- 自動捕獲和記錄實體的創建時間、修改時間、創建者和修改者
- 使用PostgreSQL持久化審計數據 