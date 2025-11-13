package com.dlut.blockchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 区块链实验室网站后端应用主类
 * 
 * @author 区块链实验室
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.dlut.blockchain")
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class BlockchainWebsiteBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainWebsiteBackendApplication.class, args);
    }

}