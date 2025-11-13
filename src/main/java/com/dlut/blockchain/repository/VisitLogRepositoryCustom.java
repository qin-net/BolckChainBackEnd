package com.dlut.blockchain.repository;

import com.dlut.blockchain.entity.VisitLog;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 访问日志数据访问层 - 自定义接口
 * 避免Spring Data JPA查询派生冲突
 */
@Repository
public interface VisitLogRepositoryCustom {
    
    /**
     * 保存访问日志
     */
    VisitLog save(VisitLog visitLog);
    
    /**
     * 根据ID查找访问日志
     */
    Optional<VisitLog> findById(Long id);
    
    /**
     * 查找所有访问日志
     */
    List<VisitLog> findAll();
    
    /**
     * 根据IP地址统计访问次数
     */
    long countByIpAddress(String ipAddress);
    
    /**
     * 根据IP地址和日期统计访问次数
     */
    long countByIpAddressAndVisitTimeBetween(String ipAddress, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的访问次数
     */
    long countByVisitTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的独立访客数
     */
    long countUniqueVisitors(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的页面浏览量
     */
    long countPageViews(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的会话数
     */
    long countSessions(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的新访客数
     */
    long countNewVisitors(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 按日期统计访问数据
     */
    List<Object[]> getDailyStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 按设备类型统计访问数据
     */
    List<Object[]> getDeviceStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 按浏览器统计访问数据
     */
    List<Object[]> getBrowserStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 按操作系统统计访问数据
     */
    List<Object[]> getOperatingSystemStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 按来源统计访问数据
     */
    List<Object[]> getTrafficSourceStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取最热门的页面
     */
    List<Object[]> getTopPages(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取平均响应时间
     */
    Double getAverageResponseTime(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取错误率统计
     */
    long countErrorRequests(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据IP地址查找最新的访问记录
     */
    Optional<VisitLog> findFirstByIpAddressOrderByVisitTimeDesc(String ipAddress);
    
    /**
     * 查找指定时间范围内的访问记录
     */
    List<VisitLog> findByVisitTimeBetweenOrderByVisitTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找指定IP地址在指定时间范围内的访问记录
     */
    List<VisitLog> findByIpAddressAndVisitTimeBetweenOrderByVisitTimeDesc(String ipAddress, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的访问数据
     */
    long countVisitsByTimeRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计指定时间范围内的独立访客数
     */
    long countUniqueVisitorsByTimeRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计指定时间范围内的会话数
     */
    long countSessionsByTimeRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计指定时间范围内的新访客数
     */
    long countNewVisitorsByTimeRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * 查找最近指定数量的访问记录
     */
    List<VisitLog> findTop100ByOrderByVisitTimeDesc();
    
    /**
     * 根据会话ID查找访问记录
     */
    List<VisitLog> findBySessionIdOrderByVisitTimeDesc(String sessionId);
}