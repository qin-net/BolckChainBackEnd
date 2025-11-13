package com.dlut.blockchain.service;

import com.dlut.blockchain.entity.User;
import com.dlut.blockchain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务 - 极简版本
 * 仅用于管理员登录验证，无需JWT认证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 验证管理员身份 - 极简版本
     * 仅验证用户名密码是否正确
     */
    public boolean validateAdmin(String username, String password) {
        try {
            // 查找用户
            User user = userRepository.findByUsernameOrEmail(username)
                    .orElse(null);
            
            if (user == null) {
                log.warn("管理员验证失败 - 用户不存在: {}", username);
                return false;
            }
            
            // 检查是否是管理员
            if (user.getRole() != User.Role.ADMIN) {
                log.warn("管理员验证失败 - 非管理员用户: {}", username);
                return false;
            }
            
            // 检查用户状态
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("管理员验证失败 - 用户状态异常: {} - {}", username, user.getStatus());
                return false;
            }
            
            // 验证密码
            boolean passwordValid = passwordEncoder.matches(password, user.getPassword());
            if (!passwordValid) {
                log.warn("管理员验证失败 - 密码错误: {}", username);
                return false;
            }
            
            log.info("管理员验证成功: {}", username);
            return true;
            
        } catch (Exception e) {
            log.error("管理员验证异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否存在（用于隐藏入口）
     */
    public boolean checkUserExists(String username) {
        return userRepository.findByUsernameOrEmail(username).isPresent();
    }
}