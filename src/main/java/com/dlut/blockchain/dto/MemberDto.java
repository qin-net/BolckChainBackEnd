package com.dlut.blockchain.dto;

import com.dlut.blockchain.entity.Member;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 实验室成员DTO
 */
@Data
public class MemberDto {
    
    private Long id;
    
    @NotBlank(message = "学号不能为空")
    @Size(min = 8, max = 20, message = "学号长度必须在8-20个字符之间")
    private String studentId;
    
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;
    
    @NotNull(message = "性别不能为空")
    private Member.Gender gender;
    
    @NotNull(message = "年级不能为空")
    private String grade;
    
    @NotBlank(message = "专业不能为空")
    @Size(max = 100, message = "专业长度不能超过100个字符")
    private String major;
    
    @NotNull(message = "角色不能为空")
    private Member.MemberRole role;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Size(max = 200, message = "研究方向长度不能超过200个字符")
    private String researchDirection;
    
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;
    
    @Size(max = 200, message = "头像URL长度不能超过200个字符")
    private String avatarUrl;
    
    @NotNull(message = "状态不能为空")
    private Member.MemberStatus status;
    
    @NotNull(message = "是否特色成员不能为空")
    private Boolean featured;
    
    private Integer displayOrder;
    
    private String githubUrl;
    private String linkedinUrl;
    private String personalWebsite;
}