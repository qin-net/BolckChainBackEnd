package com.dlut.blockchain.controller;

import com.dlut.blockchain.common.Result;
import com.dlut.blockchain.dto.FileUploadDto;
import com.dlut.blockchain.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传下载控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传下载相关接口")
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    // @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // 移除权限注解
    @Operation(summary = "上传文件", description = "上传文件到服务器（隐藏入口访问）")
    public ResponseEntity<FileUploadDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description) {
        
        log.info("文件上传请求: {}, 分类: {}", file.getOriginalFilename(), category);
        String uploadedBy = getCurrentUsername(); // 需要从SecurityContext获取当前用户
        FileUploadDto uploadedFile = fileStorageService.uploadFile(file, category, description, uploadedBy);
        return ResponseEntity.ok(uploadedFile);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileName:.+}")
    @Operation(summary = "下载文件", description = "从服务器下载文件")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        log.info("文件下载请求: {}", fileName);
        
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        
        // Try to determine file's content type
        String contentType = "application/octet-stream";
        try {
            contentType = determineContentType(fileName);
        } catch (Exception ex) {
            log.info("无法确定文件类型: {}", fileName);
        }
        
        // 增加下载次数
        FileUploadDto fileInfo = fileStorageService.getFileInfoByFileName(fileName);
        if (fileInfo != null) {
            fileStorageService.incrementDownloadCount(fileInfo.getId());
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * 获取所有文件（分页）
     */
    @GetMapping
    @Operation(summary = "获取所有文件", description = "分页获取所有上传的文件")
    public ResponseEntity<Page<FileUploadDto>> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<FileUploadDto> files = fileStorageService.getAllFiles(pageable);
        return ResponseEntity.ok(files);
    }

    /**
     * 根据分类获取文件
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "根据分类获取文件", description = "根据文件分类获取文件列表")
    public ResponseEntity<List<FileUploadDto>> getFilesByCategory(@PathVariable String category) {
        List<FileUploadDto> files = fileStorageService.getFilesByCategory(category);
        return ResponseEntity.ok(files);
    }

    /**
     * 搜索文件
     */
    @GetMapping("/search")
    @Operation(summary = "搜索文件", description = "根据关键词搜索文件")
    public ResponseEntity<Page<FileUploadDto>> searchFiles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<FileUploadDto> files = fileStorageService.searchFiles(keyword, pageable);
        return ResponseEntity.ok(files);
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文件信息", description = "根据ID获取文件详细信息")
    public ResponseEntity<Result<FileUploadDto>> getFileInfo(@PathVariable Long id) {
        try {
            FileUploadDto fileInfo = fileStorageService.getFileInfo(id);
            return ResponseEntity.ok(Result.success(fileInfo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 根据文件名获取文件信息
     */
    @GetMapping("/name/{fileName}")
    @Operation(summary = "根据文件名获取文件信息", description = "根据文件名获取文件详细信息")
    public ResponseEntity<Result<FileUploadDto>> getFileInfoByFileName(@PathVariable String fileName) {
        FileUploadDto fileInfo = fileStorageService.getFileInfoByFileName(fileName);
        if (fileInfo != null) {
            return ResponseEntity.ok(Result.success(fileInfo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新文件信息
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // 移除权限注解
    @Operation(summary = "更新文件信息", description = "更新文件描述和分类信息（隐藏入口访问）")
    public ResponseEntity<FileUploadDto> updateFileInfo(
            @PathVariable Long id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category) {
        
        log.info("更新文件信息请求: {}", id);
        FileUploadDto updatedFile = fileStorageService.updateFileInfo(id, description, category);
        return ResponseEntity.ok(updatedFile);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解
    @Operation(summary = "删除文件", description = "删除文件（隐藏入口访问）")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        log.info("删除文件请求: {}", id);
        fileStorageService.deleteFile(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取文件统计信息
     */
    @GetMapping("/statistics")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解
    @Operation(summary = "获取文件统计", description = "获取文件上传和下载统计信息（隐藏入口访问）")
    public ResponseEntity<Result<Map<String, Object>>> getFileStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总文件数
        long totalFiles = fileStorageService.getTotalFileCount();
        statistics.put("totalFiles", totalFiles);
        
        // 总文件大小
        long totalSize = fileStorageService.getTotalFileSize();
        statistics.put("totalSize", totalSize);
        statistics.put("totalSizeFormatted", formatFileSize(totalSize));
        
        // 按分类统计
        Map<String, Long> filesByCategory = fileStorageService.getFileCountByCategory();
        statistics.put("filesByCategory", filesByCategory);
        
        // 总下载次数
        long totalDownloads = fileStorageService.getTotalDownloadCount();
        statistics.put("totalDownloads", totalDownloads);
        
        // 最近上传的文件数（最近30天）
        long recentUploads = fileStorageService.getRecentUploadCount(30);
        statistics.put("recentUploads", recentUploads);
        
        return ResponseEntity.ok(Result.success(statistics));
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 确定文件内容类型
     */
    private String determineContentType(String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            default:
                return "application/octet-stream";
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
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 获取当前用户名（需要从SecurityContext获取）
     */
    private String getCurrentUsername() {
        // 这里需要从SecurityContext获取当前登录用户
        // 暂时返回一个默认值
        return "system";
    }
}