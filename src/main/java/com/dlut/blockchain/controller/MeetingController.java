package com.dlut.blockchain.controller;

import com.dlut.blockchain.common.Result;
import com.dlut.blockchain.dto.MeetingDto;
import com.dlut.blockchain.entity.Meeting;
import com.dlut.blockchain.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 例会控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Tag(name = "例会管理", description = "实验室例会相关接口")
public class MeetingController {

    private final MeetingService meetingService;

    /**
     * 获取所有例会（分页）
     */
    @GetMapping
    @Operation(summary = "获取所有例会", description = "分页获取所有例会")
    public ResponseEntity<Page<MeetingDto>> getAllMeetings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "meetingTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        // 如果sortBy是meetingTime，需要改为meetingDate
        if ("meetingTime".equals(sortBy)) {
            sortBy = "meetingDate";
        }
        try {
            log.info("获取所有例会请求: page={}, size={}, sortBy={}, sortDirection={}", 
                    page, size, sortBy, sortDirection);
            Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<MeetingDto> meetings = meetingService.getAllMeetings(pageable);
            log.info("成功获取 {} 个例会", meetings.getTotalElements());
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            log.error("获取所有例会失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取已完成的例会
     */
    @GetMapping("/completed")
    @Operation(summary = "获取已完成的例会", description = "获取所有已完成的例会")
    public ResponseEntity<List<MeetingDto>> getCompletedMeetings() {
        List<MeetingDto> meetings = meetingService.getCompletedMeetings();
        return ResponseEntity.ok(meetings);
    }

    /**
     * 获取即将举行的例会
     */
    @GetMapping("/upcoming")
    @Operation(summary = "获取即将举行的例会", description = "获取即将举行的例会")
    public ResponseEntity<List<MeetingDto>> getUpcomingMeetings() {
        List<MeetingDto> meetings = meetingService.getUpcomingMeetings();
        return ResponseEntity.ok(meetings);
    }

    /**
     * 根据ID获取例会
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取例会", description = "根据ID获取例会详细信息")
    public ResponseEntity<MeetingDto> getMeetingById(@PathVariable Long id) {
        MeetingDto meeting = meetingService.getMeetingById(id);
        return ResponseEntity.ok(meeting);
    }

    /**
     * 根据状态获取例会
     */
    @GetMapping("/status/{status}")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "根据状态获取例会", description = "根据例会状态获取例会（隐藏入口访问）")
    public ResponseEntity<Page<MeetingDto>> getMeetingsByStatus(
            @PathVariable Meeting.MeetingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "meetingTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        if ("meetingTime".equals(sortBy)) {
            sortBy = "meetingDate";
        }
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MeetingDto> meetings = meetingService.getMeetingsByStatus(status, pageable);
        return ResponseEntity.ok(meetings);
    }

    /**
     * 根据类型获取例会
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型获取例会", description = "根据例会类型获取例会")
    public ResponseEntity<List<MeetingDto>> getMeetingsByType(@PathVariable Meeting.MeetingType type) {
        List<MeetingDto> meetings = meetingService.getMeetingsByType(type);
        return ResponseEntity.ok(meetings);
    }

    /**
     * 搜索例会
     */
    @GetMapping("/search")
    @Operation(summary = "搜索例会", description = "根据关键词搜索例会")
    public ResponseEntity<Page<MeetingDto>> searchMeetings(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "meetingTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        if ("meetingTime".equals(sortBy)) {
            sortBy = "meetingDate";
        }
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MeetingDto> meetings = meetingService.searchMeetings(keyword, pageable);
        return ResponseEntity.ok(meetings);
    }

    /**
     * 创建例会（需要管理员权限）
     */
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "创建例会", description = "创建新的例会（隐藏入口访问）")
    public ResponseEntity<MeetingDto> createMeeting(@Valid @RequestBody MeetingDto meetingDto) {
        log.info("创建例会请求: {}", meetingDto.getTitle());
        MeetingDto createdMeeting = meetingService.createMeeting(meetingDto);
        return ResponseEntity.ok(createdMeeting);
    }

    /**
     * 更新例会（需要管理员权限）
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "更新例会", description = "更新例会（隐藏入口访问）")
    public ResponseEntity<MeetingDto> updateMeeting(
            @PathVariable Long id,
            @Valid @RequestBody MeetingDto meetingDto) {
        log.info("更新例会请求: {}", id);
        MeetingDto updatedMeeting = meetingService.updateMeeting(id, meetingDto);
        return ResponseEntity.ok(updatedMeeting);
    }

    /**
     * 删除例会（需要管理员权限）
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "删除例会", description = "删除例会（隐藏入口访问）")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long id) {
        log.info("删除例会请求: {}", id);
        meetingService.deleteMeeting(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新例会状态（需要管理员权限）
     */
    @PatchMapping("/{id}/status")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "更新例会状态", description = "更新例会状态（隐藏入口访问）")
    public ResponseEntity<MeetingDto> updateMeetingStatus(
            @PathVariable Long id,
            @RequestParam Meeting.MeetingStatus status) {
        log.info("更新例会状态请求: {} -> {}", id, status);
        MeetingDto updatedMeeting = meetingService.updateMeetingStatus(id, status);
        return ResponseEntity.ok(updatedMeeting);
    }

    /**
     * 更新例会显示顺序（需要管理员权限）
     */
    @PatchMapping("/{id}/display-order")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "更新例会显示顺序", description = "更新例会显示顺序（隐藏入口访问）")
    public ResponseEntity<Void> updateDisplayOrder(
            @PathVariable Long id,
            @RequestParam Integer displayOrder) {
        log.info("更新例会显示顺序请求: {} -> {}", id, displayOrder);
        meetingService.updateDisplayOrder(id, displayOrder);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取例会统计信息
     * 隐藏入口访问
     */
    @GetMapping("/statistics")
    public ResponseEntity<Result<Map<String, Object>>> getMeetingStatistics() {
        log.info("获取例会统计信息");
        Map<String, Object> statistics = new HashMap<>();
        
        // 总例会数
        long totalMeetings = meetingService.getMeetingCount();
        
        // 各状态例会数
        Map<String, Long> statusCounts = meetingService.getMeetingCountByStatus();
        
        // 计算完成率
        long completedCount = statusCounts.getOrDefault("COMPLETED", 0L);
        double completionRate = totalMeetings > 0 ? (double) completedCount / totalMeetings * 100 : 0;
        
        statistics.put("totalMeetings", totalMeetings);
        statistics.put("statusCounts", statusCounts);
        statistics.put("completionRate", String.format("%.2f%%", completionRate));
        
        Result<Map<String, Object>> result = Result.success(statistics);
        return ResponseEntity.ok(result);
    }
}