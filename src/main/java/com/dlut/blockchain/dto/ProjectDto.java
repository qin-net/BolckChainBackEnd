package com.dlut.blockchain.dto;

import com.dlut.blockchain.entity.Project;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 项目DTO
 */
@Data
public class ProjectDto {
    
    private Long id;
    
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 200, message = "项目名称长度不能超过200个字符")
    private String name;
    
    @NotBlank(message = "项目描述不能为空")
    @Size(max = 2000, message = "项目描述长度不能超过2000个字符")
    private String description;
    
    @NotNull(message = "项目状态不能为空")
    private Project.ProjectStatus status;
    
    @NotNull(message = "项目分类不能为空")
    private Project.ProjectCategory category;
    
    @NotNull(message = "是否公开不能为空")
    private Boolean isPublic;
    
    @NotNull(message = "是否特色项目不能为空")
    private Boolean featured;
    
    @Size(max = 500, message = "项目目标长度不能超过500个字符")
    private String goals;
    
    @Size(max = 1000, message = "技术栈长度不能超过1000个字符")
    private String techStack;
    
    @Size(max = 500, message = "项目成果长度不能超过500个字符")
    private String achievements;
    
    @Min(value = 0, message = "项目预算不能小于0")
    private Integer budget;
    
    @Min(value = 0, message = "项目进度不能小于0")
    @Max(value = 100, message = "项目进度不能大于100")
    private Integer progress;
    
    @Size(max = 200, message = "项目图片URL长度不能超过200个字符")
    private String imageUrl;
    
    @Size(max = 200, message = "项目仓库URL长度不能超过200个字符")
    private String repositoryUrl;
    
    @Size(max = 200, message = "演示URL长度不能超过200个字符")
    private String demoUrl;
    
    @Size(max = 200, message = "文档URL长度不能超过200个字符")
    private String documentationUrl;
    
    private Integer displayOrder;
    
    @Future(message = "项目开始日期必须是未来日期")
    private java.time.LocalDate startDate;
    
    @Future(message = "项目结束日期必须是未来日期")
    private java.time.LocalDate endDate;
}