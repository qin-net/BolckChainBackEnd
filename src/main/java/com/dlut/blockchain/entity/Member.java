package com.dlut.blockchain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实验室成员实体类
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "members", indexes = {
    @Index(name = "idx_student_id", columnList = "student_id"),
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_grade", columnList = "grade"),
    @Index(name = "idx_status", columnList = "status")
})
public class Member extends BaseEntity {

    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20位")
    @Column(name = "student_id", unique = true, nullable = false, length = 20)
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50位")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Size(max = 100, message = "专业长度不能超过100位")
    @Column(name = "major", length = 100)
    private String major;

    @Size(max = 50, message = "年级长度不能超过50位")
    @Column(name = "grade", length = 50)
    private String grade;

    @Size(max = 100, message = "邮箱长度不能超过100位")
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20位")
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role = MemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "research_direction", length = 500)
    private String researchDirection;

    @Column(name = "personal_profile", columnDefinition = "TEXT")
    private String personalProfile;

    @Column(name = "join_date")
    private java.time.LocalDate joinDate;

    @Column(name = "graduation_date")
    private java.time.LocalDate graduationDate;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "personal_website", length = 500)
    private String personalWebsite;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "featured")
    private Boolean featured = false;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    /**
     * 性别枚举
     */
    public enum Gender {
        MALE, FEMALE, OTHER
    }

    /**
     * 成员角色枚举
     */
    public enum MemberRole {
        LEADER, VICE_LEADER, SECRETARY, MEMBER
    }

    /**
     * 成员状态枚举
     */
    public enum MemberStatus {
        ACTIVE, INACTIVE, GRADUATED
    }
}