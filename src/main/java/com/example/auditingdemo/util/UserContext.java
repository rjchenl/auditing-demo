package com.example.auditingdemo.util;

import com.example.auditingdemo.model.UserInfo;

/**
 * 用戶上下文
 * 用於存儲當前用戶的資訊
 */
public class UserContext {
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<UserInfo> currentUserInfo = new ThreadLocal<>();

    public static void setCurrentUserId(String userId) {
        currentUserId.set(userId);
    }

    public static String getCurrentUserId() {
        return currentUserId.get();
    }

    public static void setCurrentUserInfo(UserInfo userInfo) {
        currentUserInfo.set(userInfo);
    }

    public static UserInfo getCurrentUserInfo() {
        return currentUserInfo.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentUserInfo.remove();
    }
} 