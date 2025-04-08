package com.example.auditingdemo.repository;

import com.example.auditingdemo.model.Api;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiRepository extends JpaRepository<Api, Long> {
    // 這裡可以添加自定義查詢方法
    Api findByApiname(String apiname);
} 