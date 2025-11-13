package com.dlut.blockchain.repository;

import com.dlut.blockchain.entity.VisitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 访问日志数据访问层
 */
@Repository
public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {
    
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
}