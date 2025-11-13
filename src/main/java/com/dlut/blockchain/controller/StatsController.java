package com.dlut.blockchain.controller;

import com.dlut.blockchain.common.Result;
import com.dlut.blockchain.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "数据统计", description = "系统数据统计相关接口")
public class StatsController {

    private final StatsService statsService;

    /**
     * 获取系统概览统计
     */
    @GetMapping("/overview")
    @Operation(summary = "系统概览统计", description = "获取系统的整体统计数据")
    public ResponseEntity<Result<Map<String, Object>>> getOverviewStats() {
        log.info("获取系统概览统计");
        Map<String, Object> overview = statsService.getOverviewStats();
        return ResponseEntity.ok(Result.success(overview));
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/users")
    @Operation(summary = "用户统计", description = "获取用户相关的统计数据")
    public ResponseEntity<Result<Map<String, Object>>> getUserStats() {
        log.info("获取用户统计");
        Map<String, Object> userStats = statsService.getUserStats();
        return ResponseEntity.ok(Result.success(userStats));
    }

    /**
     * 获取内容统计
     */
    @GetMapping("/content")
    @Operation(summary = "内容统计", description = "获取内容相关的统计数据（博客、项目、成员、例会）")
    public ResponseEntity<Result<Map<String, Object>>> getContentStats() {
        log.info("获取内容统计");
        Map<String, Object> contentStats = statsService.getContentStats();
        return ResponseEntity.ok(Result.success(contentStats));
    }

    /**
     * 获取趋势统计
     */
    @GetMapping("/trends")
    @Operation(summary = "趋势统计", description = "获取指定时间范围内的趋势数据")
    public ResponseEntity<Result<Map<String, Object>>> getTrendStats(
            @RequestParam(defaultValue = "30") int days) {
        log.info("获取趋势统计，天数: {}", days);
        Map<String, Object> trendStats = statsService.getTrendStats(days);
        return ResponseEntity.ok(Result.success(trendStats));
    }

    /**
     * 获取活跃度统计
     */
    @GetMapping("/activity")
    @Operation(summary = "活跃度统计", description = "获取用户活跃度统计数据")
    public ResponseEntity<Result<Map<String, Object>>> getActivityStats() {
        log.info("获取活跃度统计");
        Map<String, Object> activityStats = statsService.getActivityStats();
        return ResponseEntity.ok(Result.success(activityStats));
    }

    /**
     * 获取分类统计
     */
    @GetMapping("/categories")
    @Operation(summary = "分类统计", description = "获取内容按分类的统计数据")
    public ResponseEntity<Result<Map<String, Object>>> getCategoryStats() {
        log.info("获取分类统计");
        Map<String, Object> categoryStats = statsService.getCategoryStats();
        return ResponseEntity.ok(Result.success(categoryStats));
    }

    /**
     * 获取文件统计（复用FileController中的统计）
     */
    @GetMapping("/files")
    @Operation(summary = "文件统计", description = "获取文件相关的统计数据")
    public ResponseEntity<Result<Map<String, Object>>> getFileStats() {
        log.info("获取文件统计");
        Map<String, Object> fileStats = statsService.getFileStats();
        return ResponseEntity.ok(Result.success(fileStats));
    }

    /**
     * 获取指定时间范围的统计
     */
    @GetMapping("/range")
    @Operation(summary = "时间范围统计", description = "获取指定时间范围内的统计数据")
    public ResponseEntity<Result<Map<String, Object>>> getStatsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("获取时间范围统计，开始日期: {}, 结束日期: {}", startDate, endDate);
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            Map<String, Object> stats = statsService.getStatsByDateRange(start, end);
            return ResponseEntity.ok(Result.success(stats));
        } catch (Exception e) {
            log.error("日期解析错误", e);
            return ResponseEntity.badRequest().body(Result.error("日期格式错误，请使用YYYY-MM-DD格式"));
        }
    }

    /**
     * 获取实时统计
     */
    @GetMapping("/realtime")
    @Operation(summary = "实时统计", description = "获取实时的系统统计数据")
    public ResponseEntity<Result<Map<String, Object>>> getRealtimeStats() {
        log.info("获取实时统计");
        Map<String, Object> realtimeStats = statsService.getRealtimeStats();
        return ResponseEntity.ok(Result.success(realtimeStats));
    }

    /**
     * 导出统计数据
     */
    @GetMapping("/export")
    @Operation(summary = "导出统计", description = "导出统计数据为JSON格式")
    public ResponseEntity<Result<Map<String, Object>>> exportStats() {
        log.info("导出统计数据");
        Map<String, Object> allStats = new HashMap<>();
        allStats.put("overview", statsService.getOverviewStats());
        allStats.put("users", statsService.getUserStats());
        allStats.put("content", statsService.getContentStats());
        allStats.put("files", statsService.getFileStats());
        allStats.put("activity", statsService.getActivityStats());
        allStats.put("exportTime", LocalDate.now());
        
        return ResponseEntity.ok(Result.success(allStats));
    }
}