package com.dlut.blockchain.controller;

import com.dlut.blockchain.dto.ProjectDto;
import com.dlut.blockchain.entity.Project;
import com.dlut.blockchain.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "项目相关接口")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 获取所有项目（分页）
     */
    @GetMapping
    @Operation(summary = "获取所有项目", description = "分页获取所有项目")
    public ResponseEntity<Page<ProjectDto>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        // 如果提供了pageSize参数，优先使用它
        int pageSizeToUse = (pageSize != null) ? pageSize : size;
        
        // 修正排序字段映射，将updateTime映射到实际的实体字段updatedAt
        String actualSortBy = sortBy;
        if ("updateTime".equals(sortBy)) {
            actualSortBy = "updatedAt";
        } else if ("createTime".equals(sortBy)) {
            actualSortBy = "createdAt";
        }
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, pageSizeToUse, Sort.by(direction, actualSortBy));
        Page<ProjectDto> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * 获取公开项目
     */
    @GetMapping("/public")
    @Operation(summary = "获取公开项目", description = "获取所有公开的项目")
    public ResponseEntity<List<ProjectDto>> getPublicProjects() {
        List<ProjectDto> projects = projectService.getPublicProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * 获取特色项目
     */
    @GetMapping("/featured")
    @Operation(summary = "获取特色项目", description = "获取特色项目")
    public ResponseEntity<List<ProjectDto>> getFeaturedProjects() {
        List<ProjectDto> projects = projectService.getFeaturedProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * 获取进行中的项目
     */
    @GetMapping("/ongoing")
    @Operation(summary = "获取进行中的项目", description = "获取进行中的项目")
    public ResponseEntity<List<ProjectDto>> getOngoingProjects() {
        List<ProjectDto> projects = projectService.getOngoingProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * 获取已完成的项目
     */
    @GetMapping("/completed")
    @Operation(summary = "获取已完成的项目", description = "获取已完成的项目")
    public ResponseEntity<List<ProjectDto>> getCompletedProjects() {
        List<ProjectDto> projects = projectService.getCompletedProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * 根据ID获取项目
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取项目", description = "根据ID获取项目详细信息")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
        ProjectDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    /**
     * 根据状态获取项目
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取项目", description = "根据项目状态获取项目")
    public ResponseEntity<List<ProjectDto>> getProjectsByStatus(@PathVariable Project.ProjectStatus status) {
        List<ProjectDto> projects = projectService.getProjectsByStatus(status);
        return ResponseEntity.ok(projects);
    }

    /**
     * 根据分类获取项目
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "根据分类获取项目", description = "根据项目分类获取项目")
    public ResponseEntity<List<ProjectDto>> getProjectsByCategory(@PathVariable Project.ProjectCategory category) {
        List<ProjectDto> projects = projectService.getProjectsByCategory(category);
        return ResponseEntity.ok(projects);
    }

    /**
     * 搜索项目
     */
    @GetMapping("/search")
    @Operation(summary = "搜索项目", description = "根据关键词搜索项目")
    public ResponseEntity<List<ProjectDto>> searchProjects(@RequestParam String keyword) {
        List<ProjectDto> projects = projectService.searchProjects(keyword);
        return ResponseEntity.ok(projects);
    }

    /**
     * 根据日期范围获取项目
     */
    @GetMapping("/date-range")
    @Operation(summary = "根据日期范围获取项目", description = "根据项目开始日期范围获取项目")
    public ResponseEntity<List<ProjectDto>> getProjectsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ProjectDto> projects = projectService.getProjectsByDateRange(startDate, endDate);
        return ResponseEntity.ok(projects);
    }

    /**
     * 根据预算范围获取项目
     */
    @GetMapping("/budget-range")
    @Operation(summary = "根据预算范围获取项目", description = "根据项目预算范围获取项目")
    public ResponseEntity<List<ProjectDto>> getProjectsByBudgetRange(
            @RequestParam Integer minBudget,
            @RequestParam Integer maxBudget) {
        List<ProjectDto> projects = projectService.getProjectsByBudgetRange(minBudget, maxBudget);
        return ResponseEntity.ok(projects);
    }

    /**
     * 根据进度范围获取项目
     */
    @GetMapping("/progress-range")
    @Operation(summary = "根据进度范围获取项目", description = "根据项目进度范围获取项目")
    public ResponseEntity<List<ProjectDto>> getProjectsByProgressRange(
            @RequestParam Integer minProgress,
            @RequestParam Integer maxProgress) {
        List<ProjectDto> projects = projectService.getProjectsByProgressRange(minProgress, maxProgress);
        return ResponseEntity.ok(projects);
    }

    /**
     * 创建项目（需要管理员权限）
     */
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解
    @Operation(summary = "创建项目", description = "创建新项目（隐藏入口访问）")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectDto projectDto) {
        log.info("创建项目请求: {}", projectDto.getName());
        ProjectDto createdProject = projectService.createProject(projectDto);
        return ResponseEntity.ok(createdProject);
    }

    /**
     * 更新项目（需要管理员权限）
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解
    @Operation(summary = "更新项目", description = "更新项目信息（隐藏入口访问）")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto projectDto) {
        log.info("更新项目请求: {}", id);
        ProjectDto updatedProject = projectService.updateProject(id, projectDto);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * 删除项目（需要管理员权限）
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解
    @Operation(summary = "删除项目", description = "删除项目（隐藏入口访问）")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("删除项目请求: {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新项目状态（需要管理员权限）
     */
    @PatchMapping("/{id}/status")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解
    @Operation(summary = "更新项目状态", description = "更新项目状态（隐藏入口访问）")
    public ResponseEntity<ProjectDto> updateProjectStatus(
            @PathVariable Long id,
            @RequestParam Project.ProjectStatus status) {
        log.info("更新项目状态请求: {} -> {}", id, status);
        ProjectDto updatedProject = projectService.updateProjectStatus(id, status);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * 更新项目进度（需要管理员权限）
     */
    @PatchMapping("/{id}/progress")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解
    @Operation(summary = "更新项目进度", description = "更新项目进度（隐藏入口访问）")
    public ResponseEntity<ProjectDto> updateProjectProgress(
            @PathVariable Long id,
            @RequestParam Integer progress) {
        log.info("更新项目进度请求: {} -> {}", id, progress);
        ProjectDto updatedProject = projectService.updateProjectProgress(id, progress);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * 更新项目显示顺序（需要管理员权限）
     */
    @PatchMapping("/{id}/display-order")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解
    @Operation(summary = "更新项目显示顺序", description = "更新项目显示顺序（隐藏入口访问）")
    public ResponseEntity<Void> updateDisplayOrder(
            @PathVariable Long id,
            @RequestParam Integer displayOrder) {
        log.info("更新项目显示顺序请求: {} -> {}", id, displayOrder);
        projectService.updateDisplayOrder(id, displayOrder);
        return ResponseEntity.ok().build();
    }
}