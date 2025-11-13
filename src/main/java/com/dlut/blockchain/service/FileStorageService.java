package com.dlut.blockchain.service;

import com.dlut.blockchain.dto.FileUploadDto;
import com.dlut.blockchain.entity.FileUpload;
import com.dlut.blockchain.repository.FileUploadRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件存储服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileUploadRepository fileUploadRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-file-size:10485760}") // 10MB
    private long maxFileSize;

    @Value("${file.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar}")
    private String allowedExtensions;

    /**
     * 上传文件
     */
    @Transactional
    public FileUploadDto uploadFile(MultipartFile file, String category, String description, String uploadedBy) {
        log.info("上传文件: {}, 类型: {}, 大小: {} bytes", file.getOriginalFilename(), file.getContentType(), file.getSize());
        
        // 验证文件
        validateFile(file);
        
        // 生成唯一文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 保存文件信息到数据库
            FileUpload fileUpload = new FileUpload();
            fileUpload.setFileName(fileName);
            fileUpload.setFilePath(filePath.toString());
            fileUpload.setFileType(file.getContentType());
            fileUpload.setFileSize(file.getSize());
            fileUpload.setOriginalName(originalFilename);
            fileUpload.setDescription(description);
            fileUpload.setCategory(category);
            fileUpload.setUploadedBy(uploadedBy);
            fileUpload.setUploadedAt(LocalDateTime.now());
            fileUpload.setDownloadCount(0);
            
            FileUpload savedFile = fileUploadRepository.save(fileUpload);
            
            log.info("文件上传成功: {}", savedFile.getId());
            return convertToDto(savedFile);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("文件不存在: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("文件加载失败: " + fileName);
        }
    }

    /**
     * 获取文件信息
     */
    public FileUploadDto getFileInfo(Long id) {
        FileUpload fileUpload = fileUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        return convertToDto(fileUpload);
    }

    /**
     * 根据文件名获取文件信息
     */
    public FileUploadDto getFileInfoByFileName(String fileName) {
        FileUpload fileUpload = fileUploadRepository.findByFileName(fileName)
                .orElse(null);
        return fileUpload != null ? convertToDto(fileUpload) : null;
    }

    /**
     * 获取所有文件（分页）
     */
    public Page<FileUploadDto> getAllFiles(Pageable pageable) {
        log.info("获取所有文件，页码: {}, 每页数量: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<FileUpload> files = fileUploadRepository.findAll(pageable);
        return files.map(this::convertToDto);
    }

    /**
     * 根据分类获取文件
     */
    @Timed(value = "service.files.byCategory", description = "Time taken to get files by category")
    @Cacheable(value = "files", key = "'category:' + #category")
    public List<FileUploadDto> getFilesByCategory(String category) {
        log.info("根据分类获取文件: {}", category);
        List<FileUpload> files = fileUploadRepository.findByCategoryOrderByUploadedAtDesc(category);
        return files.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 搜索文件
     */
    public Page<FileUploadDto> searchFiles(String keyword, Pageable pageable) {
        log.info("搜索文件，关键词: {}", keyword);
        Page<FileUpload> files = fileUploadRepository.findByOriginalNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
        return files.map(this::convertToDto);
    }

    /**
     * 更新文件信息
     */
    @Transactional
    public FileUploadDto updateFileInfo(Long id, String description, String category) {
        log.info("更新文件信息: {}", id);
        FileUpload fileUpload = fileUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        
        fileUpload.setDescription(description);
        fileUpload.setCategory(category);
        fileUpload.setUpdatedAt(LocalDateTime.now());
        
        FileUpload updatedFile = fileUploadRepository.save(fileUpload);
        return convertToDto(updatedFile);
    }

    /**
     * 删除文件
     */
    @Transactional
    public void deleteFile(Long id) {
        log.info("删除文件: {}", id);
        FileUpload fileUpload = fileUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        
        try {
            // 删除物理文件
            Path filePath = Paths.get(fileUpload.getFilePath());
            Files.deleteIfExists(filePath);
            
            // 删除数据库记录
            fileUploadRepository.delete(fileUpload);
            
            log.info("文件删除成功: {}", id);
        } catch (IOException e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取总文件数
     */
    public long getTotalFileCount() {
        return fileUploadRepository.count();
    }

    /**
     * 获取总文件大小
     */
    public long getTotalFileSize() {
        return fileUploadRepository.sumFileSize();
    }

    /**
     * 按分类统计文件数
     */
    @Timed(value = "service.files.countByCategory", description = "Time taken to get file count by category")
    @Cacheable(value = "statistics", key = "'fileCountByCategory'")
    public Map<String, Long> getFileCountByCategory() {
        List<Object[]> results = fileUploadRepository.countByCategory();
        Map<String, Long> categoryCount = new HashMap<>();
        for (Object[] result : results) {
            String category = (String) result[0];
            Long count = (Long) result[1];
            categoryCount.put(category, count);
        }
        return categoryCount;
    }

    /**
     * 获取总下载次数
     */
    public long getTotalDownloadCount() {
        return fileUploadRepository.sumDownloadCount();
    }

    /**
     * 获取最近上传的文件数
     */
    public long getRecentUploadCount(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return fileUploadRepository.countByUploadedAtAfter(since);
    }

    /**
     * 增加下载次数
     */
    @Transactional
    public void incrementDownloadCount(Long id) {
        FileUpload fileUpload = fileUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        
        fileUpload.setDownloadCount(fileUpload.getDownloadCount() + 1);
        fileUploadRepository.save(fileUpload);
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("文件大小超过限制: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
        
        String fileExtension = getFileExtension(originalFilename);
        if (!isAllowedExtension(fileExtension)) {
            throw new RuntimeException("不支持的文件类型: " + fileExtension);
        }
        
        // 检查文件名是否包含路径遍历字符
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new RuntimeException("文件名包含非法字符");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 检查文件扩展名是否允许
     */
    private boolean isAllowedExtension(String extension) {
        String[] allowedExts = allowedExtensions.split(",");
        for (String allowedExt : allowedExts) {
            if (allowedExt.trim().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 实体转换为DTO
     */
    private FileUploadDto convertToDto(FileUpload fileUpload) {
        FileUploadDto dto = new FileUploadDto();
        dto.setId(fileUpload.getId());
        dto.setFileName(fileUpload.getFileName());
        dto.setFilePath(fileUpload.getFilePath());
        dto.setFileType(fileUpload.getFileType());
        dto.setFileSize(fileUpload.getFileSize());
        dto.setOriginalName(fileUpload.getOriginalName());
        dto.setDescription(fileUpload.getDescription());
        dto.setCategory(fileUpload.getCategory());
        dto.setUploadedBy(fileUpload.getUploadedBy());
        dto.setUploadedAt(fileUpload.getUploadedAt());
        dto.setDownloadCount(fileUpload.getDownloadCount());
        dto.setDownloadUrl("/api/files/download/" + fileUpload.getFileName());
        return dto;
    }
}