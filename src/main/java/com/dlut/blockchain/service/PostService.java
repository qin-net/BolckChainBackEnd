package com.dlut.blockchain.service;

import com.dlut.blockchain.dto.PostDto;
import com.dlut.blockchain.entity.Post;
import com.dlut.blockchain.entity.User;
import com.dlut.blockchain.repository.PostRepository;
import com.dlut.blockchain.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 博客文章服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 获取所有已发布的文章（分页）
     */
    @Timed(value = "service.posts.getAllPublished", description = "Time taken to get all published posts")
//    @Cacheable(value = "posts", key = "'published:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<PostDto> getAllPublishedPosts(Pageable pageable) {
        // 查询数据库
        Page<Post> postPage = postRepository.findByStatus(Post.PostStatus.PUBLISHED, pageable);
        
        // 转换DTO列表
        List<PostDto> dtoList = postPage.getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 手动构造PageImpl，避免缓存Page接口
        return new PageImpl<>(dtoList, pageable, postPage.getTotalElements());
    }

    /**
     * 获取特色文章
     */
    @Timed(value = "service.posts.featured", description = "Time taken to get featured posts")
//    @Cacheable(value = "posts", key = "'featured'")
    public List<PostDto> getFeaturedPosts() {
        return postRepository.findByFeaturedTrue(Pageable.unpaged())
                .stream()
                .filter(post -> post.getStatus() == Post.PostStatus.PUBLISHED)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取最新文章
     */
    @Timed(value = "service.posts.latest", description = "Time taken to get latest posts")
//    @Cacheable(value = "posts", key = "'latest:' + #limit")
    public List<PostDto> getLatestPosts(int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return postRepository.findLatestPosts(pageable)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取文章详情
     */
    @Timed(value = "service.posts.getById", description = "Time taken to get post by ID")
//    @Cacheable(value = "posts", key = "'id:' + #id")
    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        if (post.getStatus() != Post.PostStatus.PUBLISHED) {
            throw new RuntimeException("文章未发布");
        }
        
        return convertToDto(post);
    }

    /**
     * 根据状态获取文章（管理员用）
     */
    public Page<PostDto> getPostsByStatus(Post.PostStatus status, Pageable pageable) {
        return postRepository.findByStatus(status, pageable)
                .map(this::convertToDto);
    }

    /**
     * 搜索文章
     */
    @Timed(value = "service.posts.search", description = "Time taken to search posts")
//    @Cacheable(value = "posts", key = "'search:' + #keyword + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<PostDto> searchPosts(String keyword, Pageable pageable) {
        // 查询数据库
        Page<Post> postPage = postRepository.searchPublicPosts(keyword, pageable);
        
        // 转换DTO列表
        List<PostDto> dtoList = postPage.getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 手动构造PageImpl，避免缓存Page接口
        return new PageImpl<>(dtoList, pageable, postPage.getTotalElements());
    }

    /**
     * 根据标签获取文章
     */
    @Timed(value = "service.posts.byTag", description = "Time taken to get posts by tag")
//    @Cacheable(value = "posts", key = "'tag:' + #tag")
    public List<PostDto> getPostsByTag(String tag) {
        return postRepository.findByStatus(Post.PostStatus.PUBLISHED, Pageable.unpaged())
                .stream()
                .filter(post -> post.getTags() != null && post.getTags().contains(tag))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 创建文章
     */
    @Timed(value = "service.posts.create", description = "Time taken to create a post")
    @Transactional
//    @CacheEvict(value = "posts", allEntries = true)
    public PostDto createPost(PostDto postDto) {
        User author = getCurrentUser();
        
        Post post = convertToEntity(postDto);
        post.setAuthorId(author.getId());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        
        if (post.getStatus() == Post.PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }
        
        Post savedPost = postRepository.save(post);
        log.info("创建文章成功: {} - {}", savedPost.getId(), savedPost.getTitle());
        return convertToDto(savedPost);
    }

    /**
     * 更新文章
     */
    @Timed(value = "service.posts.update", description = "Time taken to update a post")
    @Transactional
//    @CacheEvict(value = "posts", allEntries = true)
    public PostDto updatePost(Long id, PostDto postDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        // 检查权限
        User currentUser = getCurrentUser();
        if (!post.getAuthorId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("没有权限更新此文章");
        }
        
        updateEntityFromDto(post, postDto);
        
        if (post.getStatus() == Post.PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        
        Post updatedPost = postRepository.save(post);
        log.info("更新文章成功: {} - {}", updatedPost.getId(), updatedPost.getTitle());
        return convertToDto(updatedPost);
    }

    /**
     * 删除文章
     */
    @Transactional
//    @CacheEvict(value = "posts", allEntries = true)
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        // 检查权限
        User currentUser = getCurrentUser();
        if (!post.getAuthorId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("没有权限删除此文章");
        }
        
        postRepository.delete(post);
        log.info("删除文章成功: {} - {}", post.getId(), post.getTitle());
    }

    /**
     * 更新文章状态
     */
    @Timed(value = "service.posts.updateStatus", description = "Time taken to update post status")
    @Transactional
//    @CacheEvict(value = "posts", allEntries = true)
    public PostDto updatePostStatus(Long id, Post.PostStatus status) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        post.setStatus(status);
        
        if (status == Post.PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        
        Post updatedPost = postRepository.save(post);
        log.info("更新文章状态成功: {} - {} -> {}", updatedPost.getId(), updatedPost.getTitle(), status);
        return convertToDto(updatedPost);
    }

    /**
     * 点赞文章
     */
    @Transactional
    public void likePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
        log.info("文章点赞: {} - {}", post.getId(), post.getTitle());
    }

    /**
     * 取消点赞文章
     */
    @Transactional
    public void unlikePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
            log.info("文章取消点赞: {} - {}", post.getId(), post.getTitle());
        }
    }

    /**
     * 更新文章显示顺序
     */
    @Transactional
    public void updateDisplayOrder(Long id, Integer displayOrder) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        post.setDisplayOrder(displayOrder);
        postRepository.save(post);
        log.info("更新文章显示顺序: {} - {} -> {}", post.getId(), post.getTitle(), displayOrder);
    }

    /**
     * 增加浏览量
     */
    @Timed(value = "service.posts.incrementView", description = "Time taken to increment view count")
    @Transactional
    public void incrementViewCount(Long id) {
        postRepository.incrementViewCount(id);
    }

    /**
     * 获取文章统计信息
     */
    @Timed(value = "service.posts.statistics", description = "Time taken to get post statistics")
//    @Cacheable(value = "statistics", key = "'postStats'")
    public Object getPostStatistics() {
        return postRepository.getPostStatistics();
    }

    /**
     * 获取总文章数
     */
    @Timed(value = "service.posts.totalCount", description = "Time taken to get total post count")
//    @Cacheable(value = "statistics", key = "'totalCount'")
    public long getTotalPostCount() {
        return postRepository.count();
    }

    /**
     * 按状态获取文章数量统计
     */
    @Timed(value = "service.posts.countByStatus", description = "Time taken to get post count by status")
//    @Cacheable(value = "statistics", key = "'countByStatus'")
    public Map<String, Long> getPostCountByStatus() {
        List<Object[]> statusCounts = postRepository.countPostsByStatus();
        Map<String, Long> result = new HashMap<>();
        
        for (Object[] statusCount : statusCounts) {
            Post.PostStatus status = (Post.PostStatus) statusCount[0];
            Long count = (Long) statusCount[1];
            result.put(status.name(), count);
        }
        
        return result;
    }

    /**
     * 获取总浏览量
     */
    @Timed(value = "service.posts.totalViews", description = "Time taken to get total views")
//    @Cacheable(value = "statistics", key = "'totalViews'")
    public long getTotalViews() {
        return postRepository.sumViewCount();
    }

    /**
     * 获取总点赞数
     */
    @Timed(value = "service.posts.totalLikes", description = "Time taken to get total likes")
//    @Cacheable(value = "statistics", key = "'totalLikes'")
    public long getTotalLikes() {
        return postRepository.sumLikeCount();
    }

    /**
     * 获取总评论数
     */
    @Timed(value = "service.posts.totalComments", description = "Time taken to get total comments")
//    @Cacheable(value = "statistics", key = "'totalComments'")
    public long getTotalComments() {
        return postRepository.sumCommentCount();
    }

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 实体转换为DTO
     */
    private PostDto convertToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setSummary(post.getSummary());
        dto.setCoverImage(post.getCoverImage());
        dto.setStatus(post.getStatus());
        dto.setAllowComments(post.getAllowComments());
        dto.setFeatured(post.getFeatured());
        dto.setTags(post.getTags());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setPublishedAt(post.getPublishedAt());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        
        // 设置作者信息
        User author = userRepository.findById(post.getAuthorId())
                .orElse(null);
        if (author != null) {
            dto.setAuthorName(author.getFullName() != null ? author.getFullName() : author.getUsername());
            dto.setAuthorAvatar(author.getAvatarUrl());
        }
        
        return dto;
    }

    /**
     * DTO转换为实体
     */
    private Post convertToEntity(PostDto dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setSummary(dto.getSummary());
        post.setCoverImage(dto.getCoverImage());
        post.setStatus(dto.getStatus());
        post.setAllowComments(dto.getAllowComments());
        post.setFeatured(dto.getFeatured());
        post.setTags(dto.getTags());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        return post;
    }

    /**
     * 更新实体
     */
    private void updateEntityFromDto(Post post, PostDto dto) {
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setSummary(dto.getSummary());
        post.setCoverImage(dto.getCoverImage());
        post.setStatus(dto.getStatus());
        post.setAllowComments(dto.getAllowComments());
        post.setFeatured(dto.getFeatured());
        post.setTags(dto.getTags());
    }
}