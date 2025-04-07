package com.example.auditingdemo.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.auditingdemo.audit.UserContext;
import com.example.auditingdemo.model.UserInfo;
import com.example.auditingdemo.repository.UserInfoRepository;

/**
 * 數據初始化器
 * 在應用啟動時加載示例數據
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 初始化用戶詳細信息數據
        initUserInfoData();
    }
    
    /**
     * 初始化用戶詳細信息數據
     */
    private void initUserInfoData() {
        // 檢查是否已有數據
        if (userInfoRepository.count() > 0) {
            return;
        }
        
        // 創建示例用戶詳細信息
        List<UserInfo> userInfos = Arrays.asList(
            createUserInfo("kenbai", "TPIsoftware", "研發一處", "白建鈞"),
            createUserInfo("peter", "TPIsoftware", "研發二處", "游XX"),
            createUserInfo("shawn", "TPIsoftware", "研發一處", "林XX"),
            createUserInfo("janice", "TPIsoftware", "研發三處", "姜XX"),
            createUserInfo("sunya", "TPIsoftware", "研發二處", "孫XX"),
            createUserInfo("anonymousUser", "System", "System", "System"),
            createUserInfo("system", "System", "System", "System")
        );
        
        // 批量保存用戶詳細信息
        userInfoRepository.saveAll(userInfos);
    }
    
    /**
     * 創建用戶詳細信息
     */
    private UserInfo createUserInfo(String userId, String company, String unit, String name) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setCompany(company);
        userInfo.setUnit(unit);
        userInfo.setName(name);
        return userInfo;
    }
} 