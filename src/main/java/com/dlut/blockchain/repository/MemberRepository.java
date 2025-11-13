package com.dlut.blockchain.repository;

import com.dlut.blockchain.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 实验室成员数据访问层
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 根据学号查找成员
     */
    Optional<Member> findByStudentId(String studentId);

    /**
     * 检查学号是否存在
     */
    boolean existsByStudentId(String studentId);

    /**
     * 根据状态查找成员
     */
    Page<Member> findByStatus(Member.MemberStatus status, Pageable pageable);

    /**
     * 根据角色查找成员
     */
    Page<Member> findByRole(Member.MemberRole role, Pageable pageable);

    /**
     * 根据性别查找成员
     */
    Page<Member> findByGender(Member.Gender gender, Pageable pageable);

    /**
     * 查找活跃成员
     */
    Page<Member> findByStatusAndRole(Member.MemberStatus status, Member.MemberRole role, Pageable pageable);

    /**
     * 根据年级查找成员
     */
    Page<Member> findByGrade(String grade, Pageable pageable);

    /**
     * 查找特色成员
     */
    Page<Member> findByFeaturedTrue(Pageable pageable);

    /**
     * 模糊查询成员
     */
    @Query("SELECT m FROM Member m WHERE " +
           "m.name LIKE %:keyword% OR " +
           "m.studentId LIKE %:keyword% OR " +
           "m.major LIKE %:keyword% OR " +
           "m.researchDirection LIKE %:keyword%")
    Page<Member> searchMembers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查找在指定日期之后加入的成员
     */
    @Query("SELECT m FROM Member m WHERE m.joinDate > :date")
    List<Member> findMembersJoinedAfter(@Param("date") java.time.LocalDate date);

    /**
     * 查找尚未毕业的成员
     */
    @Query("SELECT m FROM Member m WHERE m.status = 'ACTIVE' AND (m.graduationDate IS NULL OR m.graduationDate > CURRENT_DATE)")
    List<Member> findActiveMembers();

    /**
     * 根据显示顺序排序查找所有成员
     */
    List<Member> findAllByOrderByDisplayOrderAsc();

    /**
     * 根据显示顺序排序查找活跃成员
     */
    List<Member> findByStatusOrderByDisplayOrderAsc(Member.MemberStatus status);

    /**
     * 统计指定时间后创建的成员数
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 统计指定时间范围内创建的成员数
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}