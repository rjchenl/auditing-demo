package com.example.auditingdemo.audit;

/**
 * 用戶上下文類
 * 用於模擬當前用戶信息的存儲和獲取
 * 在實際項目中通常使用Spring Security的SecurityContextHolder
 */
public class UserContext {
    
    // ThreadLocal用於存儲當前線程關聯的用戶ID
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    
    /**
     * 設置當前用戶ID
     */
    public static void setCurrentUser(String userId) {
        currentUser.set(userId);
    }
    
    /**
     * 獲取當前用戶ID
     */
    public static String getCurrentUser() {
        return currentUser.get();
    }
    
    /**
     * 清除當前用戶信息
     */
    public static void clear() {
        currentUser.remove();
    }
} 