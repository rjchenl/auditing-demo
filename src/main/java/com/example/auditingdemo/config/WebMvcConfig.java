package com.example.auditingdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.auditingdemo.interceptor.UserTokenInterceptor;

/**
 * Web MVC配置類
 * 配置攔截器和其他Web相關設置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserTokenInterceptor userTokenInterceptor;
    
    /**
     * 添加攔截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 註冊使用者Token攔截器，應用於所有請求
        registry.addInterceptor(userTokenInterceptor)
                .addPathPatterns("/**"); // 攔截所有請求
    }
} 