package com.example.auditingdemo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auditingdemo.model.AuditRecord;
import com.example.auditingdemo.repository.AuditRecordRepository;

@RestController
@RequestMapping("/api/audit-records")
public class AuditRecordController {
    
    @Autowired
    private AuditRecordRepository auditRecordRepository;
    
    @GetMapping
    public List<AuditRecord> getAllAuditRecords() {
        return auditRecordRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AuditRecord> getAuditRecord(@PathVariable Long id) {
        return auditRecordRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public AuditRecord createAuditRecord(@RequestBody Map<String, Object> request) {
        AuditRecord record = AuditRecord.builder()
                .operation((String) request.get("operation"))
                .targetType((String) request.get("targetType"))
                .targetId(Long.valueOf(request.get("targetId").toString()))
                .details((String) request.get("details"))
                .build();
        
        return auditRecordRepository.save(record);
    }
}