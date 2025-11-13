package com.dlut.blockchain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存管理控制器
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "缓存管理", description = "缓存管理相关接口")
public class CacheController {

    private final CacheManager cacheManager;

    /**
     * 清除所有缓存
     */
    @DeleteMapping("/clear-all")
    @Operation(summary = "清除所有缓存", description = "清除系统中所有缓存")
    public ResponseEntity<String> clearAllCaches() {
        log.info("开始清除所有缓存");
        try {
            List<String> clearedCaches = new ArrayList<>();
            
            // 获取所有缓存名称并清除
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    clearedCaches.add(cacheName);
                    log.info("缓存已清除: {}", cacheName);
                }
            });
            
            String message = String.format("所有缓存清除成功，共清除 %d 个缓存: %s", 
                clearedCaches.size(), String.join(", ", clearedCaches));
            log.info(message);
            
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("清除缓存失败", e);
            return ResponseEntity.internalServerError().body("清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定缓存
     */
    @DeleteMapping("/clear/{cacheName}")
    @Operation(summary = "清除指定缓存", description = "清除指定名称的缓存")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        log.info("开始清除缓存: {}", cacheName);
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                String message = String.format("缓存 '%s' 清除成功", cacheName);
                log.info(message);
                return ResponseEntity.ok(message);
            } else {
                String message = String.format("缓存 '%s' 不存在", cacheName);
                log.warn(message);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("清除缓存 '{}' 失败", cacheName, e);
            return ResponseEntity.internalServerError().body("清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有缓存名称
     */
    @GetMapping("/names")
    @Operation(summary = "获取缓存名称列表", description = "获取系统中所有缓存的名称")
    public ResponseEntity<List<String>> getCacheNames() {
        try {
            List<String> cacheNames = new ArrayList<>();
            cacheManager.getCacheNames().forEach(cacheNames::add);
            log.info("获取缓存名称列表: {}", cacheNames);
            return ResponseEntity.ok(cacheNames);
        } catch (Exception e) {
            log.error("获取缓存名称列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}