package com.dlut.blockchain.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控服务类
 * 提供系统性能监控、指标收集和性能统计功能
 * 
 * @author 区块链实验室
 * @version 1.0.0
 */
@Service
public class PerformanceMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitorService.class);
    
    private final MeterRegistry meterRegistry;
    private final MemoryMXBean memoryMXBean;
    private final ThreadMXBean threadMXBean;
    
    // 性能统计缓存
    private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> slowQueries = new ConcurrentHashMap<>();
    
    public PerformanceMonitorService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        
        // 注册系统指标
        registerSystemMetrics();
    }
    
    /**
     * 注册系统性能指标
     * 包括内存使用、线程数等基础监控指标
     */
    private void registerSystemMetrics() {
        // 内存使用量
        Gauge.builder("system.memory.usage", this, PerformanceMonitorService::getMemoryUsage)
            .description("System memory usage in bytes")
            .register(meterRegistry);
            
        // 堆内存使用率
        Gauge.builder("system.memory.heap.usage", this, PerformanceMonitorService::getHeapUsagePercentage)
            .description("Heap memory usage percentage")
            .register(meterRegistry);
            
        // 线程数
        Gauge.builder("system.thread.count", this, PerformanceMonitorService::getThreadCount)
            .description("Current thread count")
            .register(meterRegistry);
            
        // 活跃线程数
        Gauge.builder("system.thread.active.count", this, PerformanceMonitorService::getActiveThreadCount)
            .description("Active thread count")
            .register(meterRegistry);
    }
    
    /**
     * 记录API请求性能指标
     * 
     * @param endpoint API端点
     * @param method HTTP方法
     * @param duration 执行时间（毫秒）
     * @param success 是否成功
     */
    public void recordApiMetrics(String endpoint, String method, long duration, boolean success) {
        String metricName = "api.request";
        
        // 记录请求计数
        Counter.builder(metricName + ".count")
            .tag("endpoint", endpoint)
            .tag("method", method)
            .tag("status", success ? "success" : "error")
            .register(meterRegistry)
            .increment();
            
        // 记录响应时间
        Timer.builder(metricName + ".duration")
            .tag("endpoint", endpoint)
            .tag("method", method)
            .tag("status", success ? "success" : "error")
            .register(meterRegistry)
            .record(java.time.Duration.ofMillis(duration));
            
        // 记录慢查询
        if (duration > 1000) {
            Counter.builder(metricName + ".slow")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry)
                .increment();
                
            logger.warn("Slow API request detected: {} {} took {}ms", method, endpoint, duration);
        }
        
        // 更新统计缓存
        String key = method + " " + endpoint;
        requestCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        if (!success) {
            errorCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        }
    }
    
    /**
     * 记录数据库查询性能指标
     * 
     * @param queryType 查询类型
     * @param tableName 表名
     * @param duration 执行时间（毫秒）
     * @param rowsAffected 影响的行数
     */
    public void recordDatabaseMetrics(String queryType, String tableName, long duration, long rowsAffected) {
        String metricName = "database.query";
        
        // 记录查询计数
        Counter.builder(metricName + ".count")
            .tag("type", queryType)
            .tag("table", tableName)
            .register(meterRegistry)
            .increment();
            
        // 记录查询时间
        Timer.builder(metricName + ".duration")
            .tag("type", queryType)
            .tag("table", tableName)
            .register(meterRegistry)
            .record(java.time.Duration.ofMillis(duration));
            
        // 记录影响的行数
        Counter.builder(metricName + ".rows")
            .tag("type", queryType)
            .tag("table", tableName)
            .register(meterRegistry)
            .increment(rowsAffected);
            
        // 记录慢查询
        if (duration > 500) {
            Counter.builder(metricName + ".slow")
                .tag("type", queryType)
                .tag("table", tableName)
                .register(meterRegistry)
                .increment();
                
            logger.warn("Slow database query detected: {} on {} took {}ms, affected {} rows", 
                queryType, tableName, duration, rowsAffected);
        }
    }
    
    /**
     * 获取内存使用量
     * 
     * @return 内存使用量（字节）
     */
    private double getMemoryUsage() {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
        return heapUsage.getUsed() + nonHeapUsage.getUsed();
    }
    
    /**
     * 获取堆内存使用率
     * 
     * @return 堆内存使用率（百分比）
     */
    private double getHeapUsagePercentage() {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        long maxMemory = heapUsage.getMax();
        long usedMemory = heapUsage.getUsed();
        return maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
    }
    
    /**
     * 获取线程总数
     * 
     * @return 线程总数
     */
    private double getThreadCount() {
        return threadMXBean.getThreadCount();
    }
    
    /**
     * 获取活跃线程数
     * 
     * @return 活跃线程数
     */
    private double getActiveThreadCount() {
        return threadMXBean.getDaemonThreadCount();
    }
    
    /**
     * 获取性能统计信息
     * 
     * @return 性能统计信息
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 系统信息
        stats.put("timestamp", LocalDateTime.now());
        stats.put("memoryUsage", getMemoryUsage());
        stats.put("heapUsagePercentage", getHeapUsagePercentage());
        stats.put("threadCount", getThreadCount());
        stats.put("activeThreadCount", getActiveThreadCount());
        
        // API统计
        stats.put("requestCounts", new HashMap<>(requestCounts));
        stats.put("errorCounts", new HashMap<>(errorCounts));
        
        return stats;
    }
    
    /**
     * 定时清理过期统计数据的任务
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredStats() {
        logger.info("Starting cleanup of expired performance statistics");
        
        // 清理统计数据（保留最近7天的数据）
        long cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        
        // 清理慢查询记录
        slowQueries.entrySet().removeIf(entry -> entry.getValue() < cutoffTime);
        
        logger.info("Completed cleanup of expired performance statistics");
    }
    
    /**
     * 定时报告性能统计
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void reportPerformanceStats() {
        Map<String, Object> stats = getPerformanceStats();
        logger.info("Performance Statistics: {}", stats);
    }
}