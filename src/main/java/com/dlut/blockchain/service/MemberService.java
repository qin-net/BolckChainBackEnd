package com.dlut.blockchain.service;

import com.dlut.blockchain.dto.MemberDto;
import com.dlut.blockchain.entity.Member;
import com.dlut.blockchain.repository.MemberRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实验室成员服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 获取所有成员（分页）
     */
    public Page<MemberDto> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * 获取所有活跃成员
     */
    public List<MemberDto> getAllActiveMembers() {
        return memberRepository.findByStatusOrderByDisplayOrderAsc(Member.MemberStatus.ACTIVE)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取特色成员
     */
    public List<MemberDto> getFeaturedMembers() {
        return memberRepository.findByFeaturedTrue(Pageable.unpaged())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取成员
     */
    public MemberDto getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        return convertToDto(member);
    }

    /**
     * 根据学号获取成员
     */
    public MemberDto getMemberByStudentId(String studentId) {
        Member member = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        return convertToDto(member);
    }

    /**
     * 根据角色获取成员
     */
    public List<MemberDto> getMembersByRole(Member.MemberRole role) {
        return memberRepository.findByRole(role, Pageable.unpaged())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据年级获取成员
     */
    @Timed(value = "service.members.byGrade", description = "Time taken to get members by grade")
    @Cacheable(value = "members", key = "'grade:' + #grade")
    public List<MemberDto> getMembersByGrade(String grade) {
        return memberRepository.findByGrade(grade, Pageable.unpaged())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 搜索成员
     */
    @Timed(value = "service.members.search", description = "Time taken to search members")
    @Cacheable(value = "members", key = "'search:' + #keyword")
    public List<MemberDto> searchMembers(String keyword) {
        return memberRepository.searchMembers(keyword, Pageable.unpaged())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 创建成员
     */
    @Transactional
    public MemberDto createMember(MemberDto memberDto) {
        // 检查学号是否已存在
        if (memberRepository.existsByStudentId(memberDto.getStudentId())) {
            throw new RuntimeException("学号已存在");
        }

        Member member = convertToEntity(memberDto);
        Member savedMember = memberRepository.save(member);
        log.info("创建成员成功: {} - {}", savedMember.getStudentId(), savedMember.getName());
        return convertToDto(savedMember);
    }

    /**
     * 更新成员
     */
    @Transactional
    public MemberDto updateMember(Long id, MemberDto memberDto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成员不存在"));

        // 如果学号发生变化，检查新学号是否已存在
        if (!member.getStudentId().equals(memberDto.getStudentId())) {
            if (memberRepository.existsByStudentId(memberDto.getStudentId())) {
                throw new RuntimeException("学号已存在");
            }
        }

        updateEntityFromDto(member, memberDto);
        Member updatedMember = memberRepository.save(member);
        log.info("更新成员成功: {} - {}", updatedMember.getStudentId(), updatedMember.getName());
        return convertToDto(updatedMember);
    }

    /**
     * 删除成员
     */
    @Transactional
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        memberRepository.delete(member);
        log.info("删除成员成功: {} - {}", member.getStudentId(), member.getName());
    }

    /**
     * 更新成员状态
     */
    @Transactional
    public MemberDto updateMemberStatus(Long id, Member.MemberStatus status) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        member.setStatus(status);
        Member updatedMember = memberRepository.save(member);
        log.info("更新成员状态成功: {} - {} -> {}", updatedMember.getStudentId(), updatedMember.getName(), status);
        return convertToDto(updatedMember);
    }

    /**
     * 更新成员显示顺序
     */
    @Transactional
    public void updateDisplayOrder(Long id, Integer displayOrder) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        member.setDisplayOrder(displayOrder);
        memberRepository.save(member);
        log.info("更新成员显示顺序: {} - {} -> {}", member.getStudentId(), member.getName(), displayOrder);
    }

    /**
     * 实体转换为DTO
     */
    private MemberDto convertToDto(Member member) {
        MemberDto dto = new MemberDto();
        dto.setId(member.getId());
        dto.setStudentId(member.getStudentId());
        dto.setName(member.getName());
        dto.setGender(member.getGender());
        dto.setGrade(String.valueOf(Integer.valueOf(member.getGrade())));
        dto.setMajor(member.getMajor());
        dto.setRole(member.getRole());
        dto.setEmail(member.getEmail());
        dto.setPhone(member.getPhone());
        dto.setResearchDirection(member.getResearchDirection());
        dto.setBio(member.getBio());
        dto.setAvatarUrl(member.getAvatarUrl());
        dto.setStatus(member.getStatus());
        dto.setFeatured(member.getFeatured());
        dto.setDisplayOrder(member.getDisplayOrder());
        dto.setGithubUrl(member.getGithubUrl());
        dto.setLinkedinUrl(member.getLinkedinUrl());
        dto.setPersonalWebsite(member.getPersonalWebsite());
        return dto;
    }

    /**
     * DTO转换为实体
     */
    private Member convertToEntity(MemberDto dto) {
        Member member = new Member();
        member.setStudentId(dto.getStudentId());
        member.setName(dto.getName());
        member.setGender(dto.getGender());
        member.setGrade(String.valueOf(dto.getGrade()));
        member.setMajor(dto.getMajor());
        member.setRole(dto.getRole());
        member.setEmail(dto.getEmail());
        member.setPhone(dto.getPhone());
        member.setResearchDirection(dto.getResearchDirection());
        member.setBio(dto.getBio());
        member.setAvatarUrl(dto.getAvatarUrl());
        member.setStatus(dto.getStatus());
        member.setFeatured(dto.getFeatured());
        member.setDisplayOrder(dto.getDisplayOrder());
        member.setGithubUrl(dto.getGithubUrl());
        member.setLinkedinUrl(dto.getLinkedinUrl());
        member.setPersonalWebsite(dto.getPersonalWebsite());
        return member;
    }

    /**
     * 更新实体
     */
    private void updateEntityFromDto(Member member, MemberDto dto) {
        member.setStudentId(dto.getStudentId());
        member.setName(dto.getName());
        member.setGender(dto.getGender());
        member.setGrade(String.valueOf(dto.getGrade()));
        member.setMajor(dto.getMajor());
        member.setRole(dto.getRole());
        member.setEmail(dto.getEmail());
        member.setPhone(dto.getPhone());
        member.setResearchDirection(dto.getResearchDirection());
        member.setBio(dto.getBio());
        member.setAvatarUrl(dto.getAvatarUrl());
        member.setStatus(dto.getStatus());
        member.setFeatured(dto.getFeatured());
        member.setDisplayOrder(dto.getDisplayOrder());
        member.setGithubUrl(dto.getGithubUrl());
        member.setLinkedinUrl(dto.getLinkedinUrl());
        member.setPersonalWebsite(dto.getPersonalWebsite());
    }
}