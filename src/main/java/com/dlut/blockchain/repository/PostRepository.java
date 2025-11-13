package com.dlut.blockchain.repository;

import com.dlut.blockchain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 博客文章数据访问层
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 根据状态查找文章
     */
    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.isPublic = true")
    Page<Post> findByStatus(@Param("status") Post.PostStatus status, Pageable pageable);

    /**
     * 根据分类查找文章
     */
    @Query("SELECT p FROM Post p WHERE p.categoryId = :categoryId AND p.status = 'PUBLISHED' AND p.isPublic = true")
    Page<Post> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 查找公开文章 - 优化分页查询
     */
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND p.isPublic = true")
    Page<Post> findPublicPosts(Pageable pageable);

    /**
     * 统计公开文章总数 - 用于高效分页
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.status = 'PUBLISHED' AND p.isPublic = true")
    long countPublicPosts();

    /**
     * 查找特色文章 - 优化查询
     */
    @Query("SELECT p FROM Post p WHERE p.featured = true AND p.status = 'PUBLISHED' AND p.isPublic = true")
    Page<Post> findByFeaturedTrue(Pageable pageable);

    /**
     * 统计特色文章总数
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.featured = true AND p.status = 'PUBLISHED' AND p.isPublic = true")
    long countFeaturedPosts();

    /**
     * 查找作者的文章
     */
    @Query("SELECT p FROM Post p WHERE p.authorId = :authorId AND p.status = 'PUBLISHED' AND p.isPublic = true")
    Page<Post> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    /**
     * 根据发布日期范围查找文章
     */
    @Query("SELECT p FROM Post p WHERE p.publishDate BETWEEN :startDate AND :endDate AND p.status = 'PUBLISHED'")
    List<Post> findPostsByPublishDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 模糊查询文章 - 优化搜索性能，使用全文索引
     */
    @Query("SELECT p FROM Post p WHERE " +
           "p.status = 'PUBLISHED' AND p.isPublic = true AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchPublicPosts(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计搜索结果总数 - 用于高效分页
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE " +
           "p.status = 'PUBLISHED' AND p.isPublic = true AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    long countSearchResults(@Param("keyword") String keyword);

    /**
     * 增加浏览量
     */
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    /**
     * 增加点赞数
     */
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    /**
     * 减少点赞数
     */
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
    void decrementLikeCount(@Param("postId") Long postId);

    /**
     * 更新评论数
     */
    @Modifying
    @Query(value = "UPDATE posts SET comment_count = (SELECT COUNT(*) FROM comments WHERE post_id = :postId) WHERE id = :postId", nativeQuery = true)
    void updateCommentCount(@Param("postId") Long postId);

    /**
     * 查找相关文章（基于标签）- 优化性能
     */
    @Query("SELECT p FROM Post p WHERE p.id != :postId AND p.status = 'PUBLISHED' AND p.isPublic = true AND " +
           "LOWER(p.tags) LIKE LOWER(CONCAT('%', :tag, '%')) ORDER BY p.publishedAt DESC")
    List<Post> findRelatedPosts(@Param("postId") Long postId, @Param("tag") String tag, Pageable pageable);

    /**
     * 查找最新文章 - 优化性能
     */
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND p.isPublic = true ORDER BY p.publishedAt DESC")
    List<Post> findLatestPosts(Pageable pageable);

    /**
     * 查找热门文章（按浏览量排序）- 优化性能
     */
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND p.isPublic = true ORDER BY p.viewCount DESC, p.publishedAt DESC")
    List<Post> findPopularPosts(Pageable pageable);

    /**
     * 按月份统计文章数量 - 优化性能
     */
    @Query("SELECT YEAR(p.publishedAt) as year, MONTH(p.publishedAt) as month, COUNT(p) as count " +
           "FROM Post p WHERE p.status = 'PUBLISHED' AND p.isPublic = true GROUP BY YEAR(p.publishedAt), MONTH(p.publishedAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> countPostsByMonth();

    /**
     * 获取文章统计信息
     */
    @Query("SELECT " +
           "COUNT(p) as totalPosts, " +
           "SUM(p.viewCount) as totalViews, " +
           "SUM(p.likeCount) as totalLikes, " +
           "SUM(p.commentCount) as totalComments " +
           "FROM Post p")
    Object getPostStatistics();

    /**
     * 按状态统计文章数量
     */
    @Query("SELECT p.status, COUNT(p) FROM Post p GROUP BY p.status")
    List<Object[]> countPostsByStatus();

    /**
     * 统计总浏览量
     */
    @Query("SELECT SUM(p.viewCount) FROM Post p")
    Long sumViewCount();

    /**
     * 统计总点赞数
     */
    @Query("SELECT SUM(p.likeCount) FROM Post p")
    Long sumLikeCount();

    /**
     * 统计总评论数
     */
    @Query("SELECT SUM(p.commentCount) FROM Post p")
    Long sumCommentCount();

    /**
     * 统计指定时间后创建的文章数
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 统计指定时间范围内创建的文章数
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计指定状态的文章数
     */
    long countByStatus(Post.PostStatus status);

    /**
     * 按分类统计文章数量
     */
    @Query("SELECT p.categoryId, COUNT(p) FROM Post p WHERE p.categoryId IS NOT NULL GROUP BY p.categoryId")
    List<Object[]> countByCategory();
}