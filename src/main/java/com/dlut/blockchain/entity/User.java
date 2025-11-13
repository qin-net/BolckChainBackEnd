package com.dlut.blockchain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_status", columnList = "status")
})
public class User extends BaseEntity {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50位之间")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100位之间")
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20位")
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 50, message = "真实姓名长度不能超过50位")
    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_count", nullable = false)
    private Integer loginCount = 0;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 获取刷新令牌过期日期
     */
    public LocalDateTime getRefreshTokenExpiryDate() {
        return refreshTokenExpiresAt;
    }

    /**
     * 设置刷新令牌过期日期
     */
    public void setRefreshTokenExpiryDate(LocalDateTime refreshTokenExpiryDate) {
        this.refreshTokenExpiresAt = refreshTokenExpiryDate;
    }

    /**
     * 用户角色枚举
     */
    public enum Role {
        ADMIN, USER
    }

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE, INACTIVE, LOCKED
    }
}