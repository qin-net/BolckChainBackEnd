package com.dlut.blockchain.controller;

import com.dlut.blockchain.dto.MemberDto;
import com.dlut.blockchain.entity.Member;
import com.dlut.blockchain.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实验室成员控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "实验室成员管理", description = "实验室成员相关接口")
public class MemberController {

    private final MemberService memberService;

    /**
     * 获取所有成员（分页）
     */
    @GetMapping
    @Operation(summary = "获取所有成员", description = "分页获取所有实验室成员")
    public ResponseEntity<Page<MemberDto>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MemberDto> members = memberService.getAllMembers(pageable);
        return ResponseEntity.ok(members);
    }

    /**
     * 获取所有活跃成员
     */
    @GetMapping("/active")
    @Operation(summary = "获取活跃成员", description = "获取所有活跃的实验室成员")
    public ResponseEntity<List<MemberDto>> getAllActiveMembers() {
        List<MemberDto> members = memberService.getAllActiveMembers();
        return ResponseEntity.ok(members);
    }

    /**
     * 获取特色成员
     */
    @GetMapping("/featured")
    @Operation(summary = "获取特色成员", description = "获取特色实验室成员")
    public ResponseEntity<List<MemberDto>> getFeaturedMembers() {
        List<MemberDto> members = memberService.getFeaturedMembers();
        return ResponseEntity.ok(members);
    }

    /**
     * 根据ID获取成员
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取成员", description = "根据ID获取实验室成员详细信息")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable Long id) {
        MemberDto member = memberService.getMemberById(id);
        return ResponseEntity.ok(member);
    }

    /**
     * 根据学号获取成员
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "根据学号获取成员", description = "根据学号获取实验室成员详细信息")
    public ResponseEntity<MemberDto> getMemberByStudentId(@PathVariable String studentId) {
        MemberDto member = memberService.getMemberByStudentId(studentId);
        return ResponseEntity.ok(member);
    }

    /**
     * 根据角色获取成员
     */
    @GetMapping("/role/{role}")
    @Operation(summary = "根据角色获取成员", description = "根据角色获取实验室成员")
    public ResponseEntity<List<MemberDto>> getMembersByRole(@PathVariable Member.MemberRole role) {
        List<MemberDto> members = memberService.getMembersByRole(role);
        return ResponseEntity.ok(members);
    }

    /**
     * 根据年级获取成员
     */
    @GetMapping("/grade/{grade}")
    @Operation(summary = "根据年级获取成员", description = "根据年级获取实验室成员")
    public ResponseEntity<List<MemberDto>> getMembersByGrade(@PathVariable String grade) {
        List<MemberDto> members = memberService.getMembersByGrade(grade);
        return ResponseEntity.ok(members);
    }

    /**
     * 搜索成员
     */
    @GetMapping("/search")
    @Operation(summary = "搜索成员", description = "根据关键词搜索实验室成员")
    public ResponseEntity<List<MemberDto>> searchMembers(@RequestParam String keyword) {
        List<MemberDto> members = memberService.searchMembers(keyword);
        return ResponseEntity.ok(members);
    }

    /**
     * 创建成员（需要管理员权限）
     */
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "创建成员", description = "创建新的实验室成员（隐藏入口访问）")
    public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberDto memberDto) {
        log.info("创建成员请求: {} - {}", memberDto.getStudentId(), memberDto.getName());
        MemberDto createdMember = memberService.createMember(memberDto);
        return ResponseEntity.ok(createdMember);
    }

    /**
     * 更新成员（需要管理员权限）
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "更新成员", description = "更新实验室成员信息（隐藏入口访问）")
    public ResponseEntity<MemberDto> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberDto memberDto) {
        log.info("更新成员请求: {}", id);
        MemberDto updatedMember = memberService.updateMember(id, memberDto);
        return ResponseEntity.ok(updatedMember);
    }

    /**
     * 删除成员（需要管理员权限）
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "删除成员", description = "删除实验室成员（隐藏入口访问）")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        log.info("删除成员请求: {}", id);
        memberService.deleteMember(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新成员状态（需要管理员权限）
     */
    @PatchMapping("/{id}/status")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "更新成员状态", description = "更新实验室成员状态（隐藏入口访问）")
    public ResponseEntity<MemberDto> updateMemberStatus(
            @PathVariable Long id,
            @RequestParam Member.MemberStatus status) {
        log.info("更新成员状态请求: {} -> {}", id, status);
        MemberDto updatedMember = memberService.updateMemberStatus(id, status);
        return ResponseEntity.ok(updatedMember);
    }

    /**
     * 更新成员显示顺序（需要管理员权限）
     */
    @PatchMapping("/{id}/display-order")
    // @PreAuthorize("hasRole('ADMIN')") // 移除权限注解 - 隐藏入口访问
    @Operation(summary = "更新成员显示顺序", description = "更新实验室成员显示顺序（隐藏入口访问）")
    public ResponseEntity<Void> updateDisplayOrder(
            @PathVariable Long id,
            @RequestParam Integer displayOrder) {
        log.info("更新成员显示顺序请求: {} -> {}", id, displayOrder);
        memberService.updateDisplayOrder(id, displayOrder);
        return ResponseEntity.ok().build();
    }
}