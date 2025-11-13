package com.dlut.blockchain.repository;

import com.dlut.blockchain.entity.FileUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件上传数据访问层
 */
@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {

    /**
     * 根据文件名查找文件
     */
    Optional<FileUpload> findByFileName(String fileName);

    /**
     * 根据分类查找文件
     */
    List<FileUpload> findByCategoryOrderByUploadedAtDesc(String category);

    /**
     * 根据文件名或描述模糊搜索
     */
    Page<FileUpload> findByOriginalNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String originalName, String description, Pageable pageable);

    /**
     * 根据上传者查找文件
     */
    List<FileUpload> findByUploadedByOrderByUploadedAtDesc(String uploadedBy);

    /**
     * 根据文件类型查找文件
     */
    List<FileUpload> findByFileTypeContainingOrderByUploadedAtDesc(String fileType);

    /**
     * 统计文件数量
     */
    long countByCategory(String category);

    /**
     * 增加下载次数
     */
    @Modifying
    @Query("UPDATE FileUpload f SET f.downloadCount = f.downloadCount + 1 WHERE f.id = :id")
    void incrementDownloadCount(@Param("id") Long id);

    /**
     * 查找下载次数最多的文件
     */
    List<FileUpload> findTop10ByOrderByDownloadCountDesc();

    /**
     * 根据日期范围查找文件
     */
    @Query("SELECT f FROM FileUpload f WHERE f.uploadedAt BETWEEN :startDate AND :endDate")
    List<FileUpload> findByUploadedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 统计总文件大小
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileUpload f")
    long sumFileSize();

    /**
     * 按分类统计文件数量
     */
    @Query("SELECT f.category, COUNT(f) FROM FileUpload f WHERE f.category IS NOT NULL GROUP BY f.category")
    List<Object[]> countByCategory();

    /**
     * 统计总下载次数
     */
    @Query("SELECT COALESCE(SUM(f.downloadCount), 0) FROM FileUpload f")
    long sumDownloadCount();

    /**
     * 统计指定日期之后的上传数量
     */
    @Query("SELECT COUNT(f) FROM FileUpload f WHERE f.uploadedAt > :since")
    long countByUploadedAtAfter(@Param("since") LocalDateTime since);

    /**
     * 统计指定时间范围内的上传数量
     */
    long countByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}