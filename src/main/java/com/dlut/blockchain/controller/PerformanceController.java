package com.dlut.blockchain.controller;

import com.dlut.blockchain.common.Result;
import com.dlut.blockchain.service.PerformanceMonitorService;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 性能监控控制器
 * 提供性能监控数据的REST API接口
 * 
 * @author 区块链实验室
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/performance")
@Tag(name = "性能监控", description = "系统性能监控和指标查询接口")
public class PerformanceController {

    @Autowired
    private PerformanceMonitorService performanceMonitorService;
    
    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 获取系统性能统计信息
     * 
     * @return 性能统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取性能统计", description = "获取系统当前的性能统计信息，包括内存使用、线程数等")
    public ResponseEntity<Result> getPerformanceStats() {
        try {
            Map<String, Object> stats = performanceMonitorService.getPerformanceStats();
            return ResponseEntity.ok(Result.success(stats));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("获取性能统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取API性能指标
     * 
     * @return API性能指标数据
     */
    @GetMapping("/metrics/api")
    @Operation(summary = "获取API指标", description = "获取API接口的性能指标，包括请求数、响应时间等")
    public ResponseEntity<Result> getApiMetrics() {
        try {
            Map<String, Object> apiMetrics = new HashMap<>();
            
            // 获取API请求计数
            apiMetrics.put("apiRequestCount", 
                meterRegistry.counter("api.request.count").count());
                
            // 获取API响应时间
            apiMetrics.put("apiRequestDuration", 
                meterRegistry.timer("api.request.duration").mean(TimeUnit.MILLISECONDS));
                
            // 获取慢查询计数
            apiMetrics.put("slowApiRequests", 
                meterRegistry.counter("api.request.slow").count());
            
            return ResponseEntity.ok(Result.success(apiMetrics));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("获取API指标失败: " + e.getMessage()));
        }
    }

    /**
     * 获取数据库性能指标
     * 
     * @return 数据库性能指标数据
     */
    @GetMapping("/metrics/database")
    @Operation(summary = "获取数据库指标", description = "获取数据库操作的性能指标，包括查询数、执行时间等")
    public ResponseEntity<Result> getDatabaseMetrics() {
        try {
            Map<String, Object> dbMetrics = new HashMap<>();
            
            // 获取数据库查询计数
            dbMetrics.put("databaseQueryCount", 
                meterRegistry.counter("database.query.count").count());
                
            // 获取数据库查询时间
            dbMetrics.put("databaseQueryDuration", 
                meterRegistry.timer("database.query.duration").mean(TimeUnit.MILLISECONDS));
                
            // 获取慢查询计数
            dbMetrics.put("slowDatabaseQueries", 
                meterRegistry.counter("database.query.slow").count());
                
            // 获取影响行数
            dbMetrics.put("databaseRowsAffected", 
                meterRegistry.counter("database.query.rows").count());
            
            return ResponseEntity.ok(Result.success(dbMetrics));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("获取数据库指标失败: " + e.getMessage()));
        }
    }

    /**
     * 获取系统资源使用指标
     * 
     * @return 系统资源使用指标数据
     */
    @GetMapping("/metrics/system")
    @Operation(summary = "获取系统指标", description = "获取系统资源使用指标，包括内存、线程等")
    public ResponseEntity<Result> getSystemMetrics() {
        try {
            Map<String, Object> systemMetrics = new HashMap<>();
            
            // 获取内存使用率
            systemMetrics.put("memoryUsage", 
                meterRegistry.gauge("system.memory.usage", 0));
                
            // 获取堆内存使用率
            systemMetrics.put("heapUsagePercentage", 
                meterRegistry.gauge("system.memory.heap.usage", 0));
                
            // 获取线程数
            systemMetrics.put("threadCount", 
                meterRegistry.gauge("system.thread.count", 0));
                
            // 获取活跃线程数
            systemMetrics.put("activeThreadCount", 
                meterRegistry.gauge("system.thread.active.count", 0));
            
            return ResponseEntity.ok(Result.success(systemMetrics));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("获取系统指标失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有性能指标
     * 
     * @return 所有性能指标的综合数据
     */
    @GetMapping("/metrics/all")
    @Operation(summary = "获取所有指标", description = "获取系统所有性能指标的综合数据")
    public ResponseEntity<Result> getAllMetrics() {
        try {
            Map<String, Object> allMetrics = new HashMap<>();
            
            // 系统性能统计
            allMetrics.put("performanceStats", performanceMonitorService.getPerformanceStats());
            
            // API指标
            allMetrics.put("apiMetrics", getApiMetrics().getBody().getData());
            
            // 数据库指标
            allMetrics.put("databaseMetrics", getDatabaseMetrics().getBody().getData());
            
            // 系统指标
            allMetrics.put("systemMetrics", getSystemMetrics().getBody().getData());
            
            return ResponseEntity.ok(Result.success(allMetrics));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error("获取所有指标失败: " + e.getMessage()));
        }
    }
}