package com.dlut.blockchain.service;

import com.dlut.blockchain.entity.VisitLog;
import com.dlut.blockchain.repository.VisitLogRepository;
import com.dlut.blockchain.repository.VisitLogRepositoryCustom;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 访问日志服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitLogService {
    
    private final VisitLogRepositoryCustom visitLogRepository;
    private final VisitLogRepository visitLogRepositoryJpa;
    
    /**
     * 记录访问日志
     */
    @Transactional
    public VisitLog recordVisit(HttpServletRequest request, String url, Long userId) {
        try {
            VisitLog visitLog = new VisitLog();
            
            // 基础信息
            visitLog.setIpAddress(getClientIpAddress(request));
            visitLog.setUrl(url);
            visitLog.setHttpMethod(request.getMethod());
            visitLog.setUserAgent(request.getHeader("User-Agent"));
            visitLog.setReferer(request.getHeader("Referer"));
            visitLog.setUserId(userId);
            
            // 生成会话ID
            visitLog.setSessionId(generateSessionId(visitLog.getIpAddress()));
            
            // 解析用户代理信息
            parseUserAgent(visitLog);
            
            // 判断是否为新的访客
            visitLog.setIsNewVisitor(isNewVisitor(visitLog.getIpAddress()));
            
            // 设置访问时间
            visitLog.setVisitTime(LocalDateTime.now());
            
            // 保存访问日志
            VisitLog savedLog = visitLogRepository.save(visitLog);
            log.info("访问日志记录成功: IP={}, URL={}", visitLog.getIpAddress(), url);
            
            return savedLog;
        } catch (Exception e) {
            log.error("记录访问日志失败", e);
            return null;
        }
    }
    
    /**
     * 更新访问日志的响应信息
     */
    @Transactional
    public void updateVisitResponse(VisitLog visitLog, Integer statusCode, Long responseTime) {
        if (visitLog != null) {
            visitLog.setStatusCode(statusCode);
            visitLog.setResponseTime(responseTime);
            visitLogRepository.save(visitLog);
        }
    }
    
    /**
     * 获取访问统计概览
     */
    public Map<String, Object> getVisitOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 今日统计
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);
        
        long todayVisits = visitLogRepository.countVisitsByTimeRange(startOfToday, endOfToday);
        long todayUniqueVisitors = visitLogRepository.countUniqueVisitorsByTimeRange(startOfToday, endOfToday);
        long todaySessions = visitLogRepository.countSessionsByTimeRange(startOfToday, endOfToday);
        long todayNewVisitors = visitLogRepository.countNewVisitorsByTimeRange(startOfToday, endOfToday);
        
        overview.put("todayVisits", todayVisits);
        overview.put("todayUniqueVisitors", todayUniqueVisitors);
        overview.put("todaySessions", todaySessions);
        overview.put("todayNewVisitors", todayNewVisitors);
        
        // 昨日统计
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime startOfYesterday = yesterday.atStartOfDay();
        LocalDateTime endOfYesterday = yesterday.atTime(LocalTime.MAX);
        
        long yesterdayVisits = visitLogRepository.countVisitsByTimeRange(startOfYesterday, endOfYesterday);
        long yesterdayUniqueVisitors = visitLogRepository.countUniqueVisitorsByTimeRange(startOfYesterday, endOfYesterday);
        
        // 计算增长率
        double visitGrowthRate = yesterdayVisits > 0 ? 
            ((double) (todayVisits - yesterdayVisits) / yesterdayVisits * 100) : 0;
        double visitorGrowthRate = yesterdayUniqueVisitors > 0 ? 
            ((double) (todayUniqueVisitors - yesterdayUniqueVisitors) / yesterdayUniqueVisitors * 100) : 0;
        
        overview.put("visitGrowthRate", visitGrowthRate);
        overview.put("visitorGrowthRate", visitorGrowthRate);
        
        // 本周统计
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endOfWeekDateTime = today.atTime(LocalTime.MAX);
        
        long weekVisits = visitLogRepository.countVisitsByTimeRange(startOfWeekDateTime, endOfWeekDateTime);
        long weekUniqueVisitors = visitLogRepository.countUniqueVisitorsByTimeRange(startOfWeekDateTime, endOfWeekDateTime);
        
        overview.put("weekVisits", weekVisits);
        overview.put("weekUniqueVisitors", weekUniqueVisitors);
        
        // 本月统计
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthDateTime = today.atTime(LocalTime.MAX);
        
        long monthVisits = visitLogRepository.countVisitsByTimeRange(startOfMonthDateTime, endOfMonthDateTime);
        long monthUniqueVisitors = visitLogRepository.countUniqueVisitorsByTimeRange(startOfMonthDateTime, endOfMonthDateTime);
        
        overview.put("monthVisits", monthVisits);
        overview.put("monthUniqueVisitors", monthUniqueVisitors);
        
        return overview;
    }
    
    /**
     * 获取趋势统计
     */
    public Map<String, Object> getTrendStats(int days) {
        Map<String, Object> trendStats = new HashMap<>();
        
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        List<Map<String, Object>> uniqueVisitorTrend = new ArrayList<>();
        List<Map<String, Object>> sessionTrend = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            long visits = visitLogRepository.countVisitsByTimeRange(startOfDay, endOfDay);
            long uniqueVisitors = visitLogRepository.countUniqueVisitorsByTimeRange(startOfDay, endOfDay);
            long sessions = visitLogRepository.countSessionsByTimeRange(startOfDay, endOfDay);
            long newVisitors = visitLogRepository.countNewVisitorsByTimeRange(startOfDay, endOfDay);
            
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.toString());
            dayStat.put("visits", visits);
            dayStat.put("uniqueVisitors", uniqueVisitors);
            dayStat.put("sessions", sessions);
            dayStat.put("newVisitors", newVisitors);
            dailyStats.add(dayStat);
            
            Map<String, Object> uvTrend = new HashMap<>();
            uvTrend.put("date", date.toString());
            uvTrend.put("count", uniqueVisitors);
            uniqueVisitorTrend.add(uvTrend);
            
            Map<String, Object> sessionTrendItem = new HashMap<>();
            sessionTrendItem.put("date", date.toString());
            sessionTrendItem.put("count", sessions);
            sessionTrend.add(sessionTrendItem);
        }
        
        trendStats.put("dailyStats", dailyStats);
        trendStats.put("uniqueVisitorTrend", uniqueVisitorTrend);
        trendStats.put("sessionTrend", sessionTrend);
        
        return trendStats;
    }
    
    /**
     * 获取设备统计
     */
    public Map<String, Object> getDeviceStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> deviceStats = new HashMap<>();
        
        List<Object[]> deviceData = visitLogRepository.getDeviceStats(startTime, endTime);
        List<Map<String, Object>> deviceList = new ArrayList<>();
        
        for (Object[] data : deviceData) {
            Map<String, Object> item = new HashMap<>();
            item.put("deviceType", data[0]);
            item.put("count", data[1]);
            deviceList.add(item);
        }
        
        deviceStats.put("devices", deviceList);
        
        return deviceStats;
    }
    
    /**
     * 获取浏览器统计
     */
    public Map<String, Object> getBrowserStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> browserStats = new HashMap<>();
        
        List<Object[]> browserData = visitLogRepository.getBrowserStats(startTime, endTime);
        List<Map<String, Object>> browserList = new ArrayList<>();
        
        for (Object[] data : browserData) {
            Map<String, Object> item = new HashMap<>();
            item.put("browser", data[0]);
            item.put("count", data[1]);
            browserList.add(item);
        }
        
        browserStats.put("browsers", browserList);
        
        return browserStats;
    }
    
    /**
     * 获取来源统计
     */
    public Map<String, Object> getTrafficSourceStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> sourceStats = new HashMap<>();
        
        List<Object[]> sourceData = visitLogRepository.getTrafficSourceStats(startTime, endTime);
        List<Map<String, Object>> sourceList = new ArrayList<>();
        
        for (Object[] data : sourceData) {
            Map<String, Object> item = new HashMap<>();
            item.put("source", data[0]);
            item.put("count", data[1]);
            sourceList.add(item);
        }
        
        sourceStats.put("sources", sourceList);
        
        return sourceStats;
    }
    
    /**
     * 获取热门页面统计
     */
    public Map<String, Object> getTopPages(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        Map<String, Object> topPages = new HashMap<>();
        
        List<Object[]> pageData = visitLogRepository.getTopPages(startTime, endTime);
        List<Map<String, Object>> pageList = new ArrayList<>();
        
        for (int i = 0; i < Math.min(limit, pageData.size()); i++) {
            Object[] data = pageData.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("url", data[0]);
            item.put("count", data[1]);
            pageList.add(item);
        }
        
        topPages.put("pages", pageList);
        
        return topPages;
    }
    
    /**
     * 获取性能统计
     */
    public Map<String, Object> getPerformanceStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> performanceStats = new HashMap<>();
        
        // 平均响应时间
        Double avgResponseTime = visitLogRepository.getAverageResponseTime(startTime, endTime);
        performanceStats.put("avgResponseTime", avgResponseTime != null ? avgResponseTime : 0);
        
        // 错误率
        long totalRequests = visitLogRepository.countVisitsByTimeRange(startTime, endTime);
        long errorRequests = visitLogRepository.countErrorRequests(startTime, endTime);
        double errorRate = totalRequests > 0 ? (double) errorRequests / totalRequests * 100 : 0;
        performanceStats.put("errorRate", errorRate);
        
        return performanceStats;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId(String ipAddress) {
        return ipAddress + "_" + System.currentTimeMillis();
    }
    
    /**
     * 解析用户代理信息
     */
    private void parseUserAgent(VisitLog visitLog) {
        String userAgent = visitLog.getUserAgent();
        if (userAgent == null || userAgent.isEmpty()) {
            return;
        }
        
        // 设备类型
        if (userAgent.contains("Mobile")) {
            visitLog.setDeviceType("mobile");
        } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            visitLog.setDeviceType("tablet");
        } else {
            visitLog.setDeviceType("desktop");
        }
        
        // 操作系统
        if (userAgent.contains("Windows NT 10.0")) {
            visitLog.setOperatingSystem("Windows 10");
        } else if (userAgent.contains("Windows NT 6.3")) {
            visitLog.setOperatingSystem("Windows 8.1");
        } else if (userAgent.contains("Windows NT 6.1")) {
            visitLog.setOperatingSystem("Windows 7");
        } else if (userAgent.contains("Mac OS X")) {
            visitLog.setOperatingSystem("Mac OS X");
        } else if (userAgent.contains("Linux")) {
            visitLog.setOperatingSystem("Linux");
        } else if (userAgent.contains("Android")) {
            visitLog.setOperatingSystem("Android");
        } else if (userAgent.contains("iOS")) {
            visitLog.setOperatingSystem("iOS");
        }
        
        // 浏览器
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
            visitLog.setBrowser("Chrome");
        } else if (userAgent.contains("Firefox")) {
            visitLog.setBrowser("Firefox");
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            visitLog.setBrowser("Safari");
        } else if (userAgent.contains("Edg")) {
            visitLog.setBrowser("Edge");
        } else if (userAgent.contains("Trident") || userAgent.contains("MSIE")) {
            visitLog.setBrowser("Internet Explorer");
        }
    }
    
    /**
     * 判断是否为新的访客
     */
    private boolean isNewVisitor(String ipAddress) {
        Optional<VisitLog> lastVisit = visitLogRepository.findFirstByIpAddressOrderByVisitTimeDesc(ipAddress);
        if (lastVisit.isPresent()) {
            LocalDateTime lastVisitTime = lastVisit.get().getVisitTime();
            return lastVisitTime.isBefore(LocalDateTime.now().minusDays(30));
        }
        return true;
    }
}