package com.dlut.blockchain.controller;

import com.dlut.blockchain.common.Result;
import com.dlut.blockchain.dto.PostDto;
import com.dlut.blockchain.entity.Post;
import com.dlut.blockchain.service.PostService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 博客文章控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "博客文章管理", description = "博客文章相关接口")
public class PostController {

    private final PostService postService;

    /**
     * 获取所有已发布的文章（分页）
     */
    @Timed(value = "controller.posts.getAll", description = "Time taken to get all published posts")
    @GetMapping
    @Operation(summary = "获取所有文章", description = "分页获取所有已发布的博客文章")
    public ResponseEntity<Page<PostDto>> getAllPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PostDto> posts = postService.getAllPublishedPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 获取特色文章
     */
    @GetMapping("/featured")
    @Operation(summary = "获取特色文章", description = "获取特色博客文章")
    public ResponseEntity<List<PostDto>> getFeaturedPosts() {
        List<PostDto> posts = postService.getFeaturedPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * 获取最新文章
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新文章", description = "获取最新的博客文章")
    public ResponseEntity<List<PostDto>> getLatestPosts(
            @RequestParam(defaultValue = "5") int limit) {
        List<PostDto> posts = postService.getLatestPosts(limit);
        return ResponseEntity.ok(posts);
    }

    /**
     * 根据ID获取文章
     */
    @Timed(value = "controller.posts.getById", description = "Time taken to get post by ID")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取文章", description = "根据ID获取博客文章详细信息")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        PostDto post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * 根据状态获取文章（管理员用）
     */
    @GetMapping("/status/{status}")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "根据状态获取文章", description = "根据文章状态获取文章（隐藏入口访问）")
    public ResponseEntity<Page<PostDto>> getPostsByStatus(
            @PathVariable Post.PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PostDto> posts = postService.getPostsByStatus(status, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 搜索文章
     */
    @Timed(value = "controller.posts.search", description = "Time taken to search posts")
    @GetMapping("/search")
    @Operation(summary = "搜索文章", description = "根据关键词搜索博客文章")
    public ResponseEntity<Page<PostDto>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PostDto> posts = postService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 根据标签获取文章
     */
    @GetMapping("/tag/{tag}")
    @Operation(summary = "根据标签获取文章", description = "根据标签获取博客文章")
    public ResponseEntity<List<PostDto>> getPostsByTag(@PathVariable String tag) {
        List<PostDto> posts = postService.getPostsByTag(tag);
        return ResponseEntity.ok(posts);
    }

    /**
     * 创建文章（需要登录）
     */
    @Timed(value = "controller.posts.create", description = "Time taken to create a post")
    @PostMapping
    // @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "创建文章", description = "创建新的博客文章（隐藏入口访问）")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto) {
        log.info("创建文章请求: {}", postDto.getTitle());
        PostDto createdPost = postService.createPost(postDto);
        return ResponseEntity.ok(createdPost);
    }

    /**
     * 更新文章（需要作者或管理员权限）
     */
    @Timed(value = "controller.posts.update", description = "Time taken to update a post")
    @PutMapping("/{id}")
    // @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "更新文章", description = "更新博客文章（隐藏入口访问）")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostDto postDto) {
        log.info("更新文章请求: {}", id);
        PostDto updatedPost = postService.updatePost(id, postDto);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * 删除文章（需要作者或管理员权限）
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "删除文章", description = "删除博客文章（隐藏入口访问）")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("删除文章请求: {}", id);
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新文章状态（需要管理员权限）
     */
    @PatchMapping("/{id}/status")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "更新文章状态", description = "更新博客文章状态（隐藏入口访问）")
    public ResponseEntity<PostDto> updatePostStatus(
            @PathVariable Long id,
            @RequestParam Post.PostStatus status) {
        log.info("更新文章状态请求: {} -> {}", id, status);
        PostDto updatedPost = postService.updatePostStatus(id, status);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * 点赞文章
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞文章", description = "点赞博客文章")
    public ResponseEntity<Void> likePost(@PathVariable Long id) {
        log.info("文章点赞请求: {}", id);
        postService.likePost(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 取消点赞文章
     */
    @DeleteMapping("/{id}/like")
    @Operation(summary = "取消点赞文章", description = "取消点赞博客文章")
    public ResponseEntity<Void> unlikePost(@PathVariable Long id) {
        log.info("文章取消点赞请求: {}", id);
        postService.unlikePost(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新文章显示顺序（需要管理员权限）
     */
    @PatchMapping("/{id}/display-order")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "更新文章显示顺序", description = "更新博客文章显示顺序（隐藏入口访问）")
    public ResponseEntity<Void> updateDisplayOrder(
            @PathVariable Long id,
            @RequestParam Integer displayOrder) {
        log.info("更新文章显示顺序请求: {} -> {}", id, displayOrder);
        postService.updateDisplayOrder(id, displayOrder);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取文章统计信息（需要管理员权限）
     */
    @GetMapping("/statistics")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "获取文章统计", description = "获取博客文章统计信息（隐藏入口访问）")
    public ResponseEntity<Result<Map<String, Object>>> getPostStatistics() {
        log.info("获取文章统计信息");
        Map<String, Object> statistics = new HashMap<>();
        
        // 总文章数
        long totalPosts = postService.getTotalPostCount();
        
        // 各状态文章数
        Map<String, Long> statusCounts = postService.getPostCountByStatus();
        
        // 总浏览量
        long totalViews = postService.getTotalViews();
        
        // 总点赞数
        long totalLikes = postService.getTotalLikes();
        
        // 总评论数
        long totalComments = postService.getTotalComments();
        
        statistics.put("totalPosts", totalPosts);
        statistics.put("statusCounts", statusCounts);
        statistics.put("totalViews", totalViews);
        statistics.put("totalLikes", totalLikes);
        statistics.put("totalComments", totalComments);
        
        Result<Map<String, Object>> result = Result.success(statistics);
        return ResponseEntity.ok(result);
    }
}