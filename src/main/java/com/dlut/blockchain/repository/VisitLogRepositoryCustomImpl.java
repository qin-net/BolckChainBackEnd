package com.dlut.blockchain.repository;

import com.dlut.blockchain.entity.VisitLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 访问日志数据访问层 - 自定义实现
 */
@Repository
@Transactional
public class VisitLogRepositoryCustomImpl implements VisitLogRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public VisitLog save(VisitLog visitLog) {
        if (visitLog.getId() == null) {
            entityManager.persist(visitLog);
            return visitLog;
        } else {
            return entityManager.merge(visitLog);
        }
    }
    
    @Override
    public Optional<VisitLog> findById(Long id) {
        VisitLog visitLog = entityManager.find(VisitLog.class, id);
        return Optional.ofNullable(visitLog);
    }
    
    @Override
    public List<VisitLog> findAll() {
        TypedQuery<VisitLog> query = entityManager.createQuery("SELECT v FROM VisitLog v", VisitLog.class);
        return query.getResultList();
    }
    
    @Override
    public long countByIpAddress(String ipAddress) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(v) FROM VisitLog v WHERE v.ipAddress = :ipAddress", Long.class);
        query.setParameter("ipAddress", ipAddress);
        return query.getSingleResult();
    }
    
    @Override
    public long countByIpAddressAndVisitTimeBetween(String ipAddress, LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(v) FROM VisitLog v WHERE v.ipAddress = :ipAddress AND v.visitTime BETWEEN :startTime AND :endTime", Long.class);
        query.setParameter("ipAddress", ipAddress);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getSingleResult();
    }
    
    @Override
    public long countByVisitTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(v) FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime", Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getSingleResult();
    }
    
    @Override
    public long countUniqueVisitors(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT v.ipAddress) FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime", Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getSingleResult();
    }
    
    @Override
    public long countPageViews(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(v) FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime", Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getSingleResult();
    }
    
    @Override
    public long countSessions(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT v.sessionId) FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime", Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getSingleResult();
    }
    
    @Override
    public long countNewVisitors(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT v.ipAddress) FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime AND v.isNewVisitor = true", Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getSingleResult();
    }
    
    @Override
    public List<Object[]> getDailyStats(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT DATE(v.visitTime) as visitDate, COUNT(v) as visitCount, COUNT(DISTINCT v.ipAddress) as uniqueVisitors " +
            "FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime " +
            "GROUP BY DATE(v.visitTime) ORDER BY visitDate", Object[].class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getResultList();
    }
    
    @Override
    public List<Object[]> getDeviceStats(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT v.deviceType, COUNT(v) as count FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime " +
            "GROUP BY v.deviceType ORDER BY count DESC", Object[].class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getResultList();
    }
    
    @Override
    public List<Object[]> getBrowserStats(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT v.browser, COUNT(v) as count FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime " +
            "GROUP BY v.browser ORDER BY count DESC", Object[].class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getResultList();
    }
    
    @Override
    public List<Object[]> getOperatingSystemStats(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT v.operatingSystem, COUNT(v) as count FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime " +
            "GROUP BY v.operatingSystem ORDER BY count DESC", Object[].class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getResultList();
    }
    
    @Override
    public List<Object[]> getTrafficSourceStats(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT v.trafficSource, COUNT(v) as count FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime " +
            "GROUP BY v.trafficSource ORDER BY count DESC", Object[].class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getResultList();
    }
    
    @Override
    public List<Object[]> getTopPages(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT v.url, COUNT(v) as count FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime " +
            "GROUP BY v.url ORDER BY count DESC", Object[].class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getResultList();
    }
    
    @Override
    public Double getAverageResponseTime(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Double> query = entityManager.createQuery(
            "SELECT AVG(v.responseTime) FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime AND v.responseTime IS NOT NULL", Double.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getSingleResult();
    }
    
    @Override
    public long countErrorRequests(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(v) FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime AND v.statusCode >= 400", Long.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getSingleResult();
    }
    
    @Override
    public Optional<VisitLog> findFirstByIpAddressOrderByVisitTimeDesc(String ipAddress) {
        TypedQuery<VisitLog> query = entityManager.createQuery(
            "SELECT v FROM VisitLog v WHERE v.ipAddress = :ipAddress ORDER BY v.visitTime DESC", VisitLog.class);
        query.setParameter("ipAddress", ipAddress);
        query.setMaxResults(1);
        List<VisitLog> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    
    @Override
    public List<VisitLog> findByVisitTimeBetweenOrderByVisitTimeDesc(LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<VisitLog> query = entityManager.createQuery(
            "SELECT v FROM VisitLog v WHERE v.visitTime BETWEEN :startTime AND :endTime ORDER BY v.visitTime DESC", VisitLog.class);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getResultList();
    }
    
    @Override
    public List<VisitLog> findByIpAddressAndVisitTimeBetweenOrderByVisitTimeDesc(String ipAddress, LocalDateTime startTime, LocalDateTime endTime) {
        TypedQuery<VisitLog> query = entityManager.createQuery(
            "SELECT v FROM VisitLog v WHERE v.ipAddress = :ipAddress AND v.visitTime BETWEEN :startTime AND :endTime ORDER BY v.visitTime DESC", VisitLog.class);
        query.setParameter("ipAddress", ipAddress);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        return query.getResultList();
    }
    
    @Override
    public long countVisitsByTimeRange(LocalDateTime start, LocalDateTime end) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(v) FROM VisitLog v WHERE v.visitTime >= :start AND v.visitTime < :end", Long.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getSingleResult();
    }
    
    @Override
    public long countUniqueVisitorsByTimeRange(LocalDateTime start, LocalDateTime end) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT v.ipAddress) FROM VisitLog v WHERE v.visitTime >= :start AND v.visitTime < :end", Long.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getSingleResult();
    }
    
    @Override
    public long countSessionsByTimeRange(LocalDateTime start, LocalDateTime end) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(DISTINCT v.sessionId) FROM VisitLog v WHERE v.visitTime >= :start AND v.visitTime < :end", Long.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getSingleResult();
    }
    
    @Override
    public long countNewVisitorsByTimeRange(LocalDateTime start, LocalDateTime end) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(v) FROM VisitLog v WHERE v.isNewVisitor = true AND v.visitTime >= :start AND v.visitTime < :end", Long.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getSingleResult();
    }
    
    @Override
    public List<VisitLog> findTop100ByOrderByVisitTimeDesc() {
        TypedQuery<VisitLog> query = entityManager.createQuery(
            "SELECT v FROM VisitLog v ORDER BY v.visitTime DESC", VisitLog.class);
        query.setMaxResults(100);
        return query.getResultList();
    }
    
    @Override
    public List<VisitLog> findBySessionIdOrderByVisitTimeDesc(String sessionId) {
        TypedQuery<VisitLog> query = entityManager.createQuery(
            "SELECT v FROM VisitLog v WHERE v.sessionId = :sessionId ORDER BY v.visitTime DESC", VisitLog.class);
        query.setParameter("sessionId", sessionId);
        return query.getResultList();
    }
}