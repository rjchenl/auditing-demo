package com.example.auditingdemo;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuditingDemoApplication {

    public static void main(String[] args) {
        // 設置JVM默認時區為台灣時區
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
        SpringApplication.run(AuditingDemoApplication.class, args);
    }

}
