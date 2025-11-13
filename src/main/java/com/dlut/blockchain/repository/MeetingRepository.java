package com.dlut.blockchain.repository;

import com.dlut.blockchain.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 例会数据访问层
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /**
     * 根据状态查找会议
     */
    Page<Meeting> findByStatus(Meeting.MeetingStatus status, Pageable pageable);

    /**
     * 根据类型查找会议
     */
    Page<Meeting> findByType(Meeting.MeetingType type, Pageable pageable);

    /**
     * 根据会议日期范围查找会议
     */
    @Query("SELECT m FROM Meeting m WHERE m.meetingDate BETWEEN :startDate AND :endDate")
    List<Meeting> findMeetingsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 查找即将开始的会议（未来7天内）
     */
    @Query("SELECT m FROM Meeting m WHERE m.meetingDate BETWEEN :now AND :futureDate AND m.status = 'SCHEDULED'")
    List<Meeting> findUpcomingMeetings(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);

    /**
     * 查找需要发送提醒的会议（会议前1天）
     */
    @Query("SELECT m FROM Meeting m WHERE m.meetingDate BETWEEN :tomorrowStart AND :tomorrowEnd AND m.reminderSent = false")
    List<Meeting> findMeetingsNeedingReminder(@Param("tomorrowStart") LocalDateTime tomorrowStart, @Param("tomorrowEnd") LocalDateTime tomorrowEnd);

    /**
     * 模糊查询会议 - 优化搜索性能
     */
    @Query("SELECT m FROM Meeting m WHERE " +
           "m.title LIKE %:keyword% OR " +
           "m.description LIKE %:keyword% OR " +
           "m.location LIKE %:keyword% OR " +
           "m.host LIKE %:keyword% OR " +
           "m.agenda LIKE %:keyword%")
    Page<Meeting> searchMeetings(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计搜索结果总数 - 用于高效分页
     */
    @Query("SELECT COUNT(m) FROM Meeting m WHERE " +
           "m.title LIKE %:keyword% OR " +
           "m.description LIKE %:keyword% OR " +
           "m.location LIKE %:keyword% OR " +
           "m.host LIKE %:keyword% OR " +
           "m.agenda LIKE %:keyword%")
    long countSearchResults(@Param("keyword") String keyword);

    /**
     * 根据主持人查找会议
     */
    Page<Meeting> findByHost(String host, Pageable pageable);

    /**
     * 根据地点查找会议
     */
    Page<Meeting> findByLocationContaining(String location, Pageable pageable);

    /**
     * 查找已完成的会议
     */
    @Query("SELECT m FROM Meeting m WHERE m.status = 'COMPLETED' ORDER BY m.meetingDate DESC")
    Page<Meeting> findCompletedMeetings(Pageable pageable);

    /**
     * 查找进行中的会议
     */
    @Query("SELECT m FROM Meeting m WHERE m.status = 'IN_PROGRESS'")
    Optional<Meeting> findOngoingMeeting();

    /**
     * 统计某月的会议数量
     */
    @Query("SELECT COUNT(m) FROM Meeting m WHERE YEAR(m.meetingDate) = :year AND MONTH(m.meetingDate) = :month")
    Long countMeetingsByMonth(@Param("year") int year, @Param("month") int month);

    /**
     * 按类型统计会议数量
     */
    @Query("SELECT m.type, COUNT(m) FROM Meeting m GROUP BY m.type")
    List<Object[]> countMeetingsByType();

    /**
     * 按状态统计会议数量
     */
    @Query("SELECT m.status, COUNT(m) FROM Meeting m GROUP BY m.status")
    List<Object[]> countMeetingsByStatus();

    /**
     * 统计指定时间后创建的会议数
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 统计指定时间范围内创建的会议数
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);


}