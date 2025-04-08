package com.example.auditingdemo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 數據初始化器
 * 在應用啟動時加載示例數據
 * 
 * 注意：由於我們現在使用TokenService直接模擬用戶數據，不再需要初始化數據庫
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        log.info("應用程序啟動，使用TokenService提供的模擬用戶數據");
    }
} 