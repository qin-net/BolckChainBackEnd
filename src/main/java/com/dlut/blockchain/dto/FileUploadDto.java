package com.dlut.blockchain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件上传DTO
 */
@Data
public class FileUploadDto {
    
    private Long id;
    
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过255个字符")
    private String fileName;
    
    @NotBlank(message = "文件路径不能为空")
    private String filePath;
    
    @NotBlank(message = "文件类型不能为空")
    private String fileType;
    
    private Long fileSize;
    
    @NotBlank(message = "文件原始名称不能为空")
    @Size(max = 255, message = "文件原始名称不能超过255个字符")
    private String originalName;
    
    private String description;
    
    private String category;
    
    private String uploadedBy;
    
    private LocalDateTime uploadedAt;
    
    private String downloadUrl;
    
    private Integer downloadCount;
}