package com.dlut.blockchain.service;

import com.dlut.blockchain.entity.Post;
import com.dlut.blockchain.repository.*;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final MeetingRepository meetingRepository;
    private final FileUploadRepository fileUploadRepository;

    /**
     * 获取系统概览统计
     */
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> overview = new HashMap<>();
        
        // 总用户数
        long totalUsers = userRepository.count();
        overview.put("totalUsers", totalUsers);
        
        // 总内容数（博客 + 项目 + 成员 + 例会）
        long totalPosts = postRepository.count();
        long totalProjects = projectRepository.count();
        long totalMembers = memberRepository.count();
        long totalMeetings = meetingRepository.count();
        long totalContent = totalPosts + totalProjects + totalMembers + totalMeetings;
        
        overview.put("totalPosts", totalPosts);
        overview.put("totalProjects", totalProjects);
        overview.put("totalMembers", totalMembers);
        overview.put("totalMeetings", totalMeetings);
        overview.put("totalContent", totalContent);
        
        // 总文件数
        long totalFiles = fileUploadRepository.count();
        overview.put("totalFiles", totalFiles);
        
        // 今日新增统计
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        
        long todayUsers = userRepository.countByCreatedAtAfter(startOfDay);
        long todayPosts = postRepository.countByCreatedAtAfter(startOfDay);
        long todayProjects = projectRepository.countByCreatedAtAfter(startOfDay);
        long todayMeetings = meetingRepository.countByCreatedAtAfter(startOfDay);
        long todayFiles = fileUploadRepository.countByUploadedAtAfter(startOfDay);
        
        overview.put("todayUsers", todayUsers);
        overview.put("todayPosts", todayPosts);
        overview.put("todayProjects", todayProjects);
        overview.put("todayMeetings", todayMeetings);
        overview.put("todayFiles", todayFiles);
        
        return overview;
    }

    /**
     * 获取用户统计
     */
    public Map<String, Object> getUserStats() {
        Map<String, Object> userStats = new HashMap<>();
        
        // 用户总数
        long totalUsers = userRepository.count();
        userStats.put("totalUsers", totalUsers);
        
        // 用户活跃度统计（最近30天登录的用户）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeUsers = userRepository.countByLastLoginAtAfter(thirtyDaysAgo);
        userStats.put("activeUsers", activeUsers);
        userStats.put("activeUsersRate", totalUsers > 0 ? (activeUsers * 100.0 / totalUsers) : 0);
        
        // 用户注册趋势（最近7天）
        List<Map<String, Object>> registrationTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            long count = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.toString());
            dayStat.put("count", count);
            registrationTrend.add(dayStat);
        }
        userStats.put("registrationTrend", registrationTrend);
        
        return userStats;
    }

    /**
     * 获取内容统计
     */
    public Map<String, Object> getContentStats() {
        Map<String, Object> contentStats = new HashMap<>();
        
        // 博客统计
        long totalPosts = postRepository.count();
        long publishedPosts = postRepository.countByStatus(Post.PostStatus.PUBLISHED);
        long draftPosts = postRepository.countByStatus(Post.PostStatus.DRAFT);
        
        Map<String, Object> postStats = new HashMap<>();
        postStats.put("total", totalPosts);
        postStats.put("published", publishedPosts);
        postStats.put("draft", draftPosts);
        postStats.put("publishRate", totalPosts > 0 ? (publishedPosts * 100.0 / totalPosts) : 0);
        contentStats.put("posts", postStats);
        
        // 项目统计
        long totalProjects = projectRepository.count();
        Map<String, Object> projectStats = new HashMap<>();
        projectStats.put("total", totalProjects);
        contentStats.put("projects", projectStats);
        
        // 成员统计
        long totalMembers = memberRepository.count();
        Map<String, Object> memberStats = new HashMap<>();
        memberStats.put("total", totalMembers);
        contentStats.put("members", memberStats);
        
        // 例会统计
        long totalMeetings = meetingRepository.count();
        Map<String, Object> meetingStats = new HashMap<>();
        meetingStats.put("total", totalMeetings);
        contentStats.put("meetings", meetingStats);
        
        return contentStats;
    }

    /**
     * 获取趋势统计
     */
    public Map<String, Object> getTrendStats(int days) {
        Map<String, Object> trendStats = new HashMap<>();
        
        // 用户注册趋势
        List<Map<String, Object>> userTrend = new ArrayList<>();
        // 内容发布趋势
        List<Map<String, Object>> contentTrend = new ArrayList<>();
        // 文件上传趋势
        List<Map<String, Object>> fileTrend = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            // 用户注册
            long userCount = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            Map<String, Object> userDay = new HashMap<>();
            userDay.put("date", date.toString());
            userDay.put("count", userCount);
            userTrend.add(userDay);
            
            // 内容发布（博客+项目+例会）
            long contentCount = postRepository.countByCreatedAtBetween(startOfDay, endOfDay) +
                               projectRepository.countByCreatedAtBetween(startOfDay, endOfDay) +
                               meetingRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            Map<String, Object> contentDay = new HashMap<>();
            contentDay.put("date", date.toString());
            contentDay.put("count", contentCount);
            contentTrend.add(contentDay);
            
            // 文件上传
            long fileCount = fileUploadRepository.countByUploadedAtBetween(startOfDay, endOfDay);
            Map<String, Object> fileDay = new HashMap<>();
            fileDay.put("date", date.toString());
            fileDay.put("count", fileCount);
            fileTrend.add(fileDay);
        }
        
        trendStats.put("userTrend", userTrend);
        trendStats.put("contentTrend", contentTrend);
        trendStats.put("fileTrend", fileTrend);
        
        return trendStats;
    }

    /**
     * 获取活跃度统计
     */
    public Map<String, Object> getActivityStats() {
        Map<String, Object> activityStats = new HashMap<>();
        
        // 最近7天的活跃度
        List<Map<String, Object>> weeklyActivity = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            long userActivity = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            long contentActivity = postRepository.countByCreatedAtBetween(startOfDay, endOfDay) +
                                  projectRepository.countByCreatedAtBetween(startOfDay, endOfDay) +
                                  meetingRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            long fileActivity = fileUploadRepository.countByUploadedAtBetween(startOfDay, endOfDay);
            
            Map<String, Object> dayActivity = new HashMap<>();
            dayActivity.put("date", date.toString());
            dayActivity.put("userActivity", userActivity);
            dayActivity.put("contentActivity", contentActivity);
            dayActivity.put("fileActivity", fileActivity);
            dayActivity.put("totalActivity", userActivity + contentActivity + fileActivity);
            weeklyActivity.add(dayActivity);
        }
        
        activityStats.put("weeklyActivity", weeklyActivity);
        
        return activityStats;
    }

    /**
     * 获取分类统计
     */
    @Timed(value = "service.stats.category", description = "Time taken to get category statistics")
    @Cacheable(value = "statistics", key = "'categoryStats'")
    public Map<String, Object> getCategoryStats() {
        Map<String, Object> categoryStats = new HashMap<>();
        
        // 博客分类统计
        try {
            List<Object[]> postCategories = postRepository.countByCategory();
            Map<String, Long> postCategoryCount = new HashMap<>();
            for (Object[] result : postCategories) {
                Long categoryId = (Long) result[0];
                Long count = (Long) result[1];
                postCategoryCount.put(String.valueOf(categoryId), count);
            }
            categoryStats.put("postCategories", postCategoryCount);
        } catch (Exception e) {
            log.warn("博客分类统计失败", e);
        }
        
        // 文件分类统计
        try {
            List<Object[]> fileCategories = fileUploadRepository.countByCategory();
            Map<String, Long> fileCategoryCount = new HashMap<>();
            for (Object[] result : fileCategories) {
                String category = (String) result[0];
                Long count = (Long) result[1];
                fileCategoryCount.put(category, count);
            }
            categoryStats.put("fileCategories", fileCategoryCount);
        } catch (Exception e) {
            log.warn("文件分类统计失败", e);
        }
        
        return categoryStats;
    }

    /**
     * 获取文件统计
     */
    public Map<String, Object> getFileStats() {
        Map<String, Object> fileStats = new HashMap<>();
        
        // 总文件数
        long totalFiles = fileUploadRepository.count();
        fileStats.put("totalFiles", totalFiles);
        
        // 总文件大小
        long totalSize = fileUploadRepository.sumFileSize();
        fileStats.put("totalSize", totalSize);
        fileStats.put("totalSizeFormatted", formatFileSize(totalSize));
        
        // 总下载次数
        long totalDownloads = fileUploadRepository.sumDownloadCount();
        fileStats.put("totalDownloads", totalDownloads);
        
        // 平均文件大小
        double avgFileSize = totalFiles > 0 ? (double) totalSize / totalFiles : 0;
        fileStats.put("avgFileSize", avgFileSize);
        fileStats.put("avgFileSizeFormatted", formatFileSize((long) avgFileSize));
        
        return fileStats;
    }

    /**
     * 获取指定时间范围的统计
     */
    public Map<String, Object> getStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> rangeStats = new HashMap<>();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        // 用户统计
        long usersInRange = userRepository.countByCreatedAtBetween(startDateTime, endDateTime);
        rangeStats.put("usersInRange", usersInRange);
        
        // 内容统计
        long postsInRange = postRepository.countByCreatedAtBetween(startDateTime, endDateTime);
        long projectsInRange = projectRepository.countByCreatedAtBetween(startDateTime, endDateTime);
        long meetingsInRange = meetingRepository.countByCreatedAtBetween(startDateTime, endDateTime);
        long membersInRange = memberRepository.countByCreatedAtBetween(startDateTime, endDateTime);
        
        rangeStats.put("postsInRange", postsInRange);
        rangeStats.put("projectsInRange", projectsInRange);
        rangeStats.put("meetingsInRange", meetingsInRange);
        rangeStats.put("membersInRange", membersInRange);
        
        // 文件统计
        long filesInRange = fileUploadRepository.countByUploadedAtBetween(startDateTime, endDateTime);
        rangeStats.put("filesInRange", filesInRange);
        
        rangeStats.put("startDate", startDate.toString());
        rangeStats.put("endDate", endDate.toString());
        
        return rangeStats;
    }

    /**
     * 获取实时统计
     */
    public Map<String, Object> getRealtimeStats() {
        Map<String, Object> realtimeStats = new HashMap<>();
        
        // 当前在线用户数（简化处理）
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        long onlineUsers = userRepository.countByLastLoginAtAfter(fiveMinutesAgo);
        realtimeStats.put("onlineUsers", onlineUsers);
        
        // 今日统计
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        
        long todayUsers = userRepository.countByCreatedAtAfter(startOfToday);
        long todayPosts = postRepository.countByCreatedAtAfter(startOfToday);
        long todayProjects = projectRepository.countByCreatedAtAfter(startOfToday);
        long todayMeetings = meetingRepository.countByCreatedAtAfter(startOfToday);
        long todayFiles = fileUploadRepository.countByUploadedAtAfter(startOfToday);
        
        realtimeStats.put("todayUsers", todayUsers);
        realtimeStats.put("todayPosts", todayPosts);
        realtimeStats.put("todayProjects", todayProjects);
        realtimeStats.put("todayMeetings", todayMeetings);
        realtimeStats.put("todayFiles", todayFiles);
        
        // 统计更新时间
        realtimeStats.put("lastUpdated", LocalDateTime.now().toString());
        
        return realtimeStats;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}