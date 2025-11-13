package com.dlut.blockchain.repository;

import com.dlut.blockchain.entity.User;
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
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);



    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据状态查找用户
     */
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    /**
     * 根据角色查找用户
     */
    Page<User> findByRole(User.Role role, Pageable pageable);

    /**
     * 查找锁定用户
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil > :now")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);

    /**
     * 根据刷新令牌查找用户
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * 统计指定时间后创建的用户数
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 统计指定时间后登录的用户数
     */
    long countByLastLoginAtAfter(LocalDateTime lastLoginAt);

    /**
     * 统计指定时间范围内创建的用户数
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查找活跃用户（最近登录时间在一周内）
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt > :oneWeekAgo AND u.status = 'ACTIVE'")
    Page<User> findActiveUsers(@Param("oneWeekAgo") LocalDateTime oneWeekAgo, Pageable pageable);

    /**
     * 模糊查询用户
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.username LIKE %:keyword% OR " +
           "u.email LIKE %:keyword% OR " +
           "u.realName LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}