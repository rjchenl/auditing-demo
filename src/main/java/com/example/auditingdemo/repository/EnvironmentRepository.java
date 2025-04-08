package com.example.auditingdemo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auditingdemo.model.Environment;

/**
 * 環境配置數據訪問層
 */
@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    
    /**
     * 根據名稱查詢配置
     */
    Optional<Environment> findByName(String name);
    
    /**
     * 根據類型查詢所有配置
     */
    List<Environment> findByType(String type);
    
    /**
     * 查詢已審核但未部署的配置
     */
    List<Environment> findByReviewedByIsNotNullAndDeployedByIsNull();
    
    /**
     * 查詢已部署的配置
     */
    List<Environment> findByDeployedByIsNotNull();
} 