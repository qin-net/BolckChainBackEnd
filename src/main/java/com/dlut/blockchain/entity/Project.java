package com.dlut.blockchain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 项目实体类
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_name", columnList = "name"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_start_date", columnList = "start_date"),
    @Index(name = "idx_end_date", columnList = "end_date")
})
public class Project extends BaseEntity {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 200, message = "项目名称长度不能超过200位")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotBlank(message = "项目描述不能为空")
    @Size(max = 2000, message = "项目描述长度不能超过2000位")
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ProjectCategory category;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Size(max = 100, message = "技术栈长度不能超过100位")
    @Column(name = "tech_stack", length = 100)
    private String techStack;

    @Size(max = 100, message = "项目链接长度不能超过100位")
    @Column(name = "project_url", length = 100)
    private String projectUrl;

    @Size(max = 100, message = "GitHub链接长度不能超过100位")
    @Column(name = "github_url", length = 100)
    private String githubUrl;

    @Column(name = "progress", nullable = false)
    private Integer progress = 0;

    @Column(name = "budget")
    private Double budget;

    @Column(name = "actual_cost")
    private Double actualCost;

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(name = "leader_name", length = 50)
    private String leaderName;

    @Column(name = "goals", columnDefinition = "TEXT")
    private String goals;

    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "featured", nullable = false)
    private Boolean featured = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    /**
     * 项目状态枚举
     */
    public enum ProjectStatus {
        PLANNING, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD, ONGOING
    }

    /**
     * 项目分类枚举
     */
    public enum ProjectCategory {
        RESEARCH, DEVELOPMENT, COMPETITION, COLLABORATION, EDUCATION
    }
}