package com.dlut.blockchain.controller;

import com.dlut.blockchain.dto.AuthRequest;
import com.dlut.blockchain.dto.AuthResponse;
import com.dlut.blockchain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 极简版本
 * 仅用于管理员隐藏入口登录，使用Session保存登录状态
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "管理员登录相关接口")
public class AuthController {

    private final AuthService authService;
    private static final String ADMIN_SESSION_KEY = "admin_logged_in";

    /**
     * 管理员登录 - 极简版本
     * 仅验证用户名密码，使用Session保存登录状态
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员登录接口，验证用户名密码")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request, HttpSession session) {
        log.info("管理员登录请求: {}", request.getUsername());
        try {
            // 验证用户名密码
            boolean isValid = authService.validateAdmin(request.getUsername(), request.getPassword());
            
            if (isValid) {
                // 登录成功，保存到Session
                session.setAttribute(ADMIN_SESSION_KEY, true);
                session.setAttribute("admin_username", request.getUsername());
                
                AuthResponse response = new AuthResponse(
                    "admin_session",  // 简单的session标识
                    "",  // 无需刷新令牌
                    3600000L,  // 1小时有效期
                    request.getUsername(),
                    "ADMIN"
                );
                
                log.info("管理员登录成功: {}", request.getUsername());
                return ResponseEntity.ok(response);
            } else {
                log.warn("管理员登录失败: {} - 用户名或密码错误", request.getUsername());
                throw new RuntimeException("用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("管理员登录失败: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 检查管理员登录状态
     */
    @GetMapping("/validate")
    @Operation(summary = "检查登录状态", description = "检查管理员是否已登录")
    public ResponseEntity<Boolean> validateLogin(HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute(ADMIN_SESSION_KEY);
        boolean valid = isLoggedIn != null && isLoggedIn;
        log.info("检查管理员登录状态: {}", valid);
        return ResponseEntity.ok(valid);
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    @Operation(summary = "管理员登出", description = "管理员登出接口")
    public ResponseEntity<String> logout(HttpSession session) {
        String username = (String) session.getAttribute("admin_username");
        session.invalidate(); // 清除session
        log.info("管理员登出: {}", username != null ? username : "unknown");
        return ResponseEntity.ok("登出成功");
    }
}