package com.dlut.blockchain.repository;

import com.dlut.blockchain.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目数据访问层
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据状态查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = :status AND p.isPublic = true")
    Page<Project> findByStatus(@Param("status") Project.ProjectStatus status, Pageable pageable);

    /**
     * 根据分类查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.category = :category AND p.isPublic = true")
    Page<Project> findByCategory(@Param("category") Project.ProjectCategory category, Pageable pageable);

    /**
     * 查找公开项目 - 优化分页查询
     */
    @Query("SELECT p FROM Project p WHERE p.isPublic = true")
    Page<Project> findByIsPublicTrue(Pageable pageable);

    /**
     * 统计公开项目总数 - 用于高效分页
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.isPublic = true")
    long countPublicProjects();

    /**
     * 查找特色项目 - 优化查询
     */
    @Query("SELECT p FROM Project p WHERE p.featured = true AND p.isPublic = true")
    Page<Project> findByFeaturedTrue(Pageable pageable);

    /**
     * 统计特色项目总数
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.featured = true AND p.isPublic = true")
    long countFeaturedProjects();

    /**
     * 查找进行中的项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'IN_PROGRESS'")
    Page<Project> findOngoingProjects(Pageable pageable);

    /**
     * 查找已完成的项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'COMPLETED'")
    Page<Project> findCompletedProjects(Pageable pageable);

    /**
     * 根据开始日期范围查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.startDate BETWEEN :startDate AND :endDate")
    List<Project> findProjectsByStartDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 模糊查询项目 - 优化搜索性能
     */
    @Query("SELECT p FROM Project p WHERE " +
           "(p.name LIKE %:keyword% OR " +
           "p.description LIKE %:keyword% OR " +
           "p.techStack LIKE %:keyword% OR " +
           "p.leaderName LIKE %:keyword%) AND " +
           "p.isPublic = true")
    Page<Project> searchProjects(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计搜索结果总数 - 用于高效分页
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE " +
           "(p.name LIKE %:keyword% OR " +
           "p.description LIKE %:keyword% OR " +
           "p.techStack LIKE %:keyword% OR " +
           "p.leaderName LIKE %:keyword%) AND " +
           "p.isPublic = true")
    long countSearchResults(@Param("keyword") String keyword);

    /**
     * 查找预算范围内的项目
     */
    @Query("SELECT p FROM Project p WHERE p.budget BETWEEN :minBudget AND :maxBudget")
    List<Project> findProjectsByBudgetRange(@Param("minBudget") Double minBudget, @Param("maxBudget") Double maxBudget);

    /**
     * 查找进度范围内的项目
     */
    @Query("SELECT p FROM Project p WHERE p.progress BETWEEN :minProgress AND :maxProgress")
    List<Project> findProjectsByProgressRange(@Param("minProgress") Integer minProgress, @Param("maxProgress") Integer maxProgress);

    /**
     * 根据显示顺序排序查找所有项目
     */
    List<Project> findAllByOrderByDisplayOrderAsc();

    /**
     * 根据显示顺序排序查找公开项目
     */
    @Query("SELECT p FROM Project p WHERE p.isPublic = true ORDER BY p.displayOrder ASC")
    List<Project> findPublicProjectsOrderByDisplayOrder();

    /**
     * 根据分类查询公开项目并按显示顺序排序
     */
    @Query("SELECT p FROM Project p WHERE p.category = :category AND p.isPublic = true ORDER BY p.displayOrder ASC")
    List<Project> findByCategoryAndIsPublicTrueOrderByDisplayOrderAsc(@Param("category") Project.ProjectCategory category);

    /**
     * 获取特色项目
     */
    @Query("SELECT p FROM Project p WHERE p.featured = true AND p.isPublic = true ORDER BY p.displayOrder ASC")
    List<Project> findByFeaturedTrueAndIsPublicTrueOrderByDisplayOrderAsc();

    /**
     * 根据状态和公开状态查询项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = :status AND p.isPublic = true ORDER BY p.displayOrder ASC")
    List<Project> findByStatusAndIsPublicTrueOrderByDisplayOrderAsc(@Param("status") Project.ProjectStatus status);

    /**
     * 根据状态查询项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = :status ORDER BY p.displayOrder ASC")
    List<Project> findByStatusOrderByDisplayOrderAsc(@Param("status") Project.ProjectStatus status);

    /**
     * 获取所有公开项目并按显示顺序排序
     */
    @Query("SELECT p FROM Project p WHERE p.isPublic = true ORDER BY p.displayOrder ASC")
    List<Project> findByIsPublicTrueOrderByDisplayOrderAsc();

    /**
     * 统计指定时间后创建的项目数
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 统计指定时间范围内创建的项目数
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 根据项目名称模糊查询（忽略大小写）
     */
    Page<Project> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    
    /**
     * 根据项目名称和状态联合查询
     */
    Page<Project> findByNameContainingIgnoreCaseAndStatus(@Param("keyword") String keyword, @Param("status") Project.ProjectStatus status, Pageable pageable);
}