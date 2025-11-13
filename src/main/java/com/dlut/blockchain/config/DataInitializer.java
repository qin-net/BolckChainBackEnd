package com.dlut.blockchain.config;

import com.dlut.blockchain.entity.User;
import com.dlut.blockchain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 测试数据初始化器
 * 在测试环境下自动创建初始用户数据
 */
@Slf4j
@Configuration
@Profile("test")
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeTestData() {
        return args -> {
            log.info("初始化测试数据...");
            
            // 检查是否已存在用户
            if (userRepository.count() == 0) {
                // 创建管理员用户
                User admin = new User();
                admin.setUsername("blockchain_blue");
                admin.setPassword(passwordEncoder.encode("bk1314217"));
                admin.setEmail("admin@blockchain.dlut.edu.cn");
                admin.setFullName("区块链组管理员");
                admin.setRole(User.Role.ADMIN);
                admin.setStatus(User.UserStatus.ACTIVE);
                admin.setFailedLoginAttempts(0);
                
                userRepository.save(admin);
                log.info("创建管理员用户: blockchain_blue");
                
                // 创建普通用户
                User user = new User();
                user.setUsername("testuser");
                user.setPassword(passwordEncoder.encode("testpass123"));
                user.setEmail("test@example.com");
                user.setFullName("测试用户");
                user.setRole(User.Role.USER);
                user.setStatus(User.UserStatus.ACTIVE);
                user.setFailedLoginAttempts(0);
                
                userRepository.save(user);
                log.info("创建测试用户: testuser");
                
                log.info("测试数据初始化完成！");
            } else {
                log.info("数据库中已存在用户数据，跳过初始化");
            }
        };
    }
}