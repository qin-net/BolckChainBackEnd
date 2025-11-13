package com.dlut.blockchain.controller;

import com.dlut.blockchain.service.VisitLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 访问统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/visit-stats")
@RequiredArgsConstructor
@Tag(name = "访问统计", description = "访问统计相关接口")
public class VisitStatsController {
    
    private final VisitLogService visitLogService;
    
    /**
     * 获取访问概览统计
     */
    @GetMapping("/overview")
    @Operation(summary = "获取访问概览统计", description = "获取今日、昨日、本周、本月的访问统计数据")
    public ResponseEntity<Map<String, Object>> getVisitOverview() {
        try {
            Map<String, Object> overview = visitLogService.getVisitOverview();
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("获取访问概览统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取访问概览统计失败"));
        }
    }
    
    /**
     * 获取访问趋势统计
     */
    @GetMapping("/trend")
    @Operation(summary = "获取访问趋势统计", description = "获取指定天数内的访问趋势数据")
    public ResponseEntity<Map<String, Object>> getVisitTrend(
            @Parameter(description = "天数，默认为30天") 
            @RequestParam(defaultValue = "30") int days) {
        try {
            if (days <= 0 || days > 365) {
                return ResponseEntity.badRequest().body(Map.of("error", "天数必须在1-365之间"));
            }
            
            Map<String, Object> trend = visitLogService.getTrendStats(days);
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            log.error("获取访问趋势统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取访问趋势统计失败"));
        }
    }
    
    /**
     * 获取设备统计
     */
    @GetMapping("/devices")
    @Operation(summary = "获取设备统计", description = "获取指定时间范围内的设备类型访问统计")
    public ResponseEntity<Map<String, Object>> getDeviceStats(
            @Parameter(description = "开始时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            LocalDateTime startTime = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
            
            Map<String, Object> deviceStats = visitLogService.getDeviceStats(startTime, endTime);
            return ResponseEntity.ok(deviceStats);
        } catch (Exception e) {
            log.error("获取设备统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取设备统计失败"));
        }
    }
    
    /**
     * 获取浏览器统计
     */
    @GetMapping("/browsers")
    @Operation(summary = "获取浏览器统计", description = "获取指定时间范围内的浏览器访问统计")
    public ResponseEntity<Map<String, Object>> getBrowserStats(
            @Parameter(description = "开始时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            LocalDateTime startTime = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
            
            Map<String, Object> browserStats = visitLogService.getBrowserStats(startTime, endTime);
            return ResponseEntity.ok(browserStats);
        } catch (Exception e) {
            log.error("获取浏览器统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取浏览器统计失败"));
        }
    }
    
    /**
     * 获取流量来源统计
     */
    @GetMapping("/traffic-sources")
    @Operation(summary = "获取流量来源统计", description = "获取指定时间范围内的流量来源统计")
    public ResponseEntity<Map<String, Object>> getTrafficSourceStats(
            @Parameter(description = "开始时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            LocalDateTime startTime = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
            
            Map<String, Object> sourceStats = visitLogService.getTrafficSourceStats(startTime, endTime);
            return ResponseEntity.ok(sourceStats);
        } catch (Exception e) {
            log.error("获取流量来源统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取流量来源统计失败"));
        }
    }
    
    /**
     * 获取热门页面统计
     */
    @GetMapping("/top-pages")
    @Operation(summary = "获取热门页面统计", description = "获取指定时间范围内的热门页面访问统计")
    public ResponseEntity<Map<String, Object>> getTopPages(
            @Parameter(description = "开始时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "返回数量，默认为10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            LocalDateTime startTime = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
            
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest().body(Map.of("error", "返回数量必须在1-100之间"));
            }
            
            Map<String, Object> topPages = visitLogService.getTopPages(startTime, endTime, limit);
            return ResponseEntity.ok(topPages);
        } catch (Exception e) {
            log.error("获取热门页面统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取热门页面统计失败"));
        }
    }
    
    /**
     * 获取性能统计
     */
    @GetMapping("/performance")
    @Operation(summary = "获取性能统计", description = "获取指定时间范围内的网站性能统计")
    public ResponseEntity<Map<String, Object>> getPerformanceStats(
            @Parameter(description = "开始时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束时间，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            LocalDateTime startTime = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
            
            Map<String, Object> performanceStats = visitLogService.getPerformanceStats(startTime, endTime);
            return ResponseEntity.ok(performanceStats);
        } catch (Exception e) {
            log.error("获取性能统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取性能统计失败"));
        }
    }
    
    /**
     * 获取实时统计
     */
    @GetMapping("/realtime")
    @Operation(summary = "获取实时统计", description = "获取实时的访问统计概览")
    public ResponseEntity<Map<String, Object>> getRealtimeStats() {
        try {
            Map<String, Object> overview = visitLogService.getVisitOverview();
            
            // 添加实时数据
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            
            Map<String, Object> realtime = new java.util.HashMap<>();
            realtime.put("overview", overview);
            realtime.put("lastUpdated", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(realtime);
        } catch (Exception e) {
            log.error("获取实时统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取实时统计失败"));
        }
    }
}