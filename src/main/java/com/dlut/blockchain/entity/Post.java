package com.dlut.blockchain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 博客文章实体类
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "posts", indexes = {
    @Index(name = "idx_title", columnList = "title"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_publish_date", columnList = "publish_date"),
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_author_id", columnList = "author_id"),
    @Index(name = "idx_featured_status", columnList = "featured, status"),
    @Index(name = "idx_status_public", columnList = "status, is_public")
})
public class Post extends BaseEntity {

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200位")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "文章内容不能为空")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Size(max = 500, message = "文章摘要长度不能超过500位")
    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @NotBlank(message = "文章作者不能为空")
    @Size(max = 100, message = "作者长度不能超过100位")
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "author_id")
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatus status = PostStatus.DRAFT;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Size(max = 200, message = "标签长度不能超过200位")
    @Column(name = "tags", length = 200)
    private String tags;

    @Column(name = "featured", nullable = false)
    private Boolean featured = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "allow_comment", nullable = false)
    private Boolean allowComment = true;

    @Column(name = "allow_comments", nullable = false)
    private Boolean allowComments = true;

    @Size(max = 200, message = "SEO标题长度不能超过200位")
    @Column(name = "seo_title", length = 200)
    private String seoTitle;

    @Size(max = 500, message = "SEO描述长度不能超过500位")
    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Size(max = 200, message = "SEO关键词长度不能超过200位")
    @Column(name = "seo_keywords", length = 200)
    private String seoKeywords;

    /**
     * 文章状态枚举
     */
    public enum PostStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}