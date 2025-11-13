package com.dlut.blockchain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文件上传实体类
 */
@Data
@Entity
@Table(name = "file_uploads")
@EqualsAndHashCode(callSuper = true)
public class FileUpload extends BaseEntity {

    /**
     * 文件名称（存储的文件名）
     */
    @Column(name = "file_name", nullable = false, unique = true)
    private String fileName;

    /**
     * 原始文件名
     */
    @Column(name = "original_name", nullable = false)
    private String originalName;

    /**
     * 文件路径
     */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    /**
     * 文件类型
     */
    @Column(name = "file_type")
    private String fileType;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件分类
     */
    @Column(name = "category")
    private String category;

    /**
     * 文件描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 上传者
     */
    @Column(name = "uploaded_by")
    private String uploadedBy;

    /**
     * 上传时间
     */
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 下载次数
     */
    @Column(name = "download_count", columnDefinition = "int default 0")
    private Integer downloadCount = 0;

    /**
     * 文件状态（ACTIVE, DELETED）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "varchar(20) default 'ACTIVE'")
    private FileStatus status = FileStatus.ACTIVE;

    /**
     * 文件状态枚举
     */
    public enum FileStatus {
        ACTIVE,     // 活跃
        DELETED     // 已删除
    }

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = FileStatus.ACTIVE;
        }
        if (downloadCount == null) {
            downloadCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}