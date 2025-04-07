package com.example.auditingdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.auditingdemo.interceptor.UserTokenInterceptor;

/**
 * Web MVC配置類
 * 配置拦截器和其他Web相關設置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserTokenInterceptor userTokenInterceptor;
    
    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 註冊用戶Token拦截器，應用於所有請求
        registry.addInterceptor(userTokenInterceptor)
                .addPathPatterns("/**"); // 拦截所有請求
    }
} 