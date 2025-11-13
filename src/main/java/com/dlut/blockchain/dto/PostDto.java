package com.dlut.blockchain.dto;

import com.dlut.blockchain.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 博客文章DTO
 */
@Data
public class PostDto {
    
    private Long id;
    
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200个字符")
    private String title;
    
    @NotBlank(message = "文章内容不能为空")
    @Size(max = 50000, message = "文章内容长度不能超过50000个字符")
    private String content;
    
    @Size(max = 500, message = "文章摘要长度不能超过500个字符")
    private String summary;
    
    @Size(max = 200, message = "文章封面图片URL长度不能超过200个字符")
    private String coverImage;
    
    @NotNull(message = "文章状态不能为空")
    private Post.PostStatus status;
    
    @NotNull(message = "是否允许评论不能为空")
    private Boolean allowComments;
    
    @NotNull(message = "是否特色文章不能为空")
    private Boolean featured;
    
    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;
    
    private String authorName;
    private String authorAvatar;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private java.time.LocalDateTime publishedAt;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}