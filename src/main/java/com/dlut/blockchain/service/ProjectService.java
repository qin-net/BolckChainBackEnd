package com.dlut.blockchain.service;

import com.dlut.blockchain.dto.ProjectDto;
import com.dlut.blockchain.entity.Project;
import com.dlut.blockchain.repository.ProjectRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    /**
     * 获取所有项目（分页）
     */
    @Timed(value = "service.projects.getAll", description = "Time taken to get all projects")
//    @Cacheable(value = "projects", key = "'all:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        // 查询数据库
        Page<Project> projectPage = projectRepository.findAll(pageable);
        
        // 转换DTO列表
        List<ProjectDto> dtoList = projectPage.getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 手动构造PageImpl，避免缓存Page接口
        return new PageImpl<>(dtoList, pageable, projectPage.getTotalElements());
    }

    /**
     * 获取公开项目
     */
    public List<ProjectDto> getPublicProjects() {
        return projectRepository.findByIsPublicTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取特色项目
     */
//    @Cacheable(value = "projects", key = "'featured'")
    public List<ProjectDto> getFeaturedProjects() {
        return projectRepository.findByFeaturedTrueAndIsPublicTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取进行中的项目
     */
    public List<ProjectDto> getOngoingProjects() {
        return projectRepository.findByStatusAndIsPublicTrueOrderByDisplayOrderAsc(Project.ProjectStatus.ONGOING)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取已完成的项目
     */
    public List<ProjectDto> getCompletedProjects() {
        return projectRepository.findByStatusAndIsPublicTrueOrderByDisplayOrderAsc(Project.ProjectStatus.COMPLETED)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取项目
     */
//    @Cacheable(value = "projects", key = "'id:' + #id")
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        return convertToDto(project);
    }

    /**
     * 根据状态获取项目
     */
    public List<ProjectDto> getProjectsByStatus(Project.ProjectStatus status) {
        return projectRepository.findByStatusOrderByDisplayOrderAsc(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据分类获取项目
     */
    @Timed(value = "service.projects.byCategory", description = "Time taken to get projects by category")
//    @Cacheable(value = "projects", key = "'category:' + #category.name()")
    public List<ProjectDto> getProjectsByCategory(Project.ProjectCategory category) {
        return projectRepository.findByCategoryAndIsPublicTrueOrderByDisplayOrderAsc(category)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 搜索项目
     */
    public List<ProjectDto> searchProjects(String keyword) {
        return projectRepository.searchProjects(keyword, Pageable.unpaged())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 创建项目
     */
    @Transactional
//    @CacheEvict(value = "projects", allEntries = true)
    public ProjectDto createProject(ProjectDto projectDto) {
        Project project = convertToEntity(projectDto);
        Project savedProject = projectRepository.save(project);
        log.info("创建项目成功: {} - {}", savedProject.getId(), savedProject.getName());
        return convertToDto(savedProject);
    }

    /**
     * 更新项目
     */
    @Transactional
//    @CacheEvict(value = "projects", allEntries = true)
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        updateEntityFromDto(project, projectDto);
        Project updatedProject = projectRepository.save(project);
        log.info("更新项目成功: {} - {}", updatedProject.getId(), updatedProject.getName());
        return convertToDto(updatedProject);
    }

    /**
     * 删除项目
     */
    @Transactional
//    @CacheEvict(value = "projects", allEntries = true)
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        projectRepository.delete(project);
        log.info("删除项目成功: {} - {}", project.getId(), project.getName());
    }

    /**
     * 更新项目状态
     */
    @Transactional
    public ProjectDto updateProjectStatus(Long id, Project.ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        project.setStatus(status);
        Project updatedProject = projectRepository.save(project);
        log.info("更新项目状态成功: {} - {} -> {}", updatedProject.getId(), updatedProject.getName(), status);
        return convertToDto(updatedProject);
    }

    /**
     * 更新项目进度
     */
    @Transactional
    public ProjectDto updateProjectProgress(Long id, Integer progress) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        project.setProgress(progress);
        
        // 如果进度达到100%，自动设置为已完成状态
        if (progress >= 100) {
            project.setStatus(Project.ProjectStatus.COMPLETED);
        }
        
        Project updatedProject = projectRepository.save(project);
        log.info("更新项目进度成功: {} - {} -> {}", updatedProject.getId(), updatedProject.getName(), progress);
        return convertToDto(updatedProject);
    }

    /**
     * 更新项目显示顺序
     */
    @Transactional
    public void updateDisplayOrder(Long id, Integer displayOrder) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        project.setDisplayOrder(displayOrder);
        projectRepository.save(project);
        log.info("更新项目显示顺序: {} - {} -> {}", project.getId(), project.getName(), displayOrder);
    }

    /**
     * 获取指定日期范围内的项目
     */
    public List<ProjectDto> getProjectsByDateRange(LocalDate startDate, LocalDate endDate) {
        return projectRepository.findProjectsByStartDateRange(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定预算范围内的项目
     */
    public List<ProjectDto> getProjectsByBudgetRange(Integer minBudget, Integer maxBudget) {
        return projectRepository.findProjectsByBudgetRange(minBudget.doubleValue(), maxBudget.doubleValue())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定进度范围内的项目
     */
    public List<ProjectDto> getProjectsByProgressRange(Integer minProgress, Integer maxProgress) {
        return projectRepository.findProjectsByProgressRange(minProgress, maxProgress)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 实体转换为DTO
     */
    private ProjectDto convertToDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus());
        dto.setCategory(project.getCategory());
        dto.setIsPublic(project.getIsPublic());
        dto.setFeatured(project.getFeatured());
        dto.setGoals(project.getGoals());
        dto.setTechStack(project.getTechStack());
        dto.setAchievements(project.getAchievements());
        // 修复潜在的null指针问题
        if (project.getBudget() != null) {
            dto.setBudget(project.getBudget().intValue());
        } else {
            dto.setBudget(0);
        }
        dto.setProgress(project.getProgress());
        dto.setImageUrl(project.getImageUrl());
        dto.setRepositoryUrl(project.getGithubUrl());
        dto.setDemoUrl(project.getProjectUrl());
        dto.setDocumentationUrl(project.getProjectUrl());
        dto.setDisplayOrder(project.getDisplayOrder());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        return dto;
    }

    /**
     * DTO转换为实体
     */
    private Project convertToEntity(ProjectDto dto) {
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStatus(dto.getStatus());
        project.setCategory(dto.getCategory());
        project.setIsPublic(dto.getIsPublic());
        project.setFeatured(dto.getFeatured());
        project.setGoals(dto.getGoals());
        project.setTechStack(dto.getTechStack());
        project.setAchievements(dto.getAchievements());
        project.setBudget(dto.getBudget().doubleValue());
        project.setProgress(dto.getProgress());
        project.setImageUrl(dto.getImageUrl());
        project.setGithubUrl(dto.getRepositoryUrl());
        project.setProjectUrl(dto.getDemoUrl());
        project.setDisplayOrder(dto.getDisplayOrder());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        return project;
    }

    /**
     * 更新实体
     */
    private void updateEntityFromDto(Project project, ProjectDto dto) {
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStatus(dto.getStatus());
        project.setCategory(dto.getCategory());
        project.setIsPublic(dto.getIsPublic());
        project.setFeatured(dto.getFeatured());
        project.setGoals(dto.getGoals());
        project.setTechStack(dto.getTechStack());
        project.setAchievements(dto.getAchievements());
        project.setBudget(dto.getBudget().doubleValue());
        project.setProgress(dto.getProgress());
        project.setImageUrl(dto.getImageUrl());
        project.setGithubUrl(dto.getRepositoryUrl());
        project.setProjectUrl(dto.getDemoUrl());
        project.setDisplayOrder(dto.getDisplayOrder());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
    }
}