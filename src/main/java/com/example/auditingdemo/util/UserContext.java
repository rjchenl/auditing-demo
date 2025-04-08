package com.example.auditingdemo.util;

/**
 * 用戶上下文，用於存儲當前線程的用戶信息
 */
public class UserContext {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    /**
     * 設置當前用戶令牌
     * 
     * @param userToken 用戶令牌
     */
    public static void setCurrentUser(String userToken) {
        currentUser.set(userToken);
    }

    /**
     * 獲取當前用戶令牌
     * 
     * @return 當前用戶令牌
     */
    public static String getCurrentUser() {
        return currentUser.get();
    }

    /**
     * 清理ThreadLocal，防止內存泄漏
     */
    public static void clear() {
        currentUser.remove();
    }
} 