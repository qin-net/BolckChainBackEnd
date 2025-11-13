package com.dlut.blockchain.service;

import com.dlut.blockchain.dto.MeetingDto;
import com.dlut.blockchain.entity.Meeting;
import com.dlut.blockchain.exception.ResourceNotFoundException;
import com.dlut.blockchain.repository.MeetingRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 例会服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    /**
     * 获取所有例会（分页）
     */
    @Timed(value = "service.meetings.getAll", description = "Time taken to get all meetings")
//    @Cacheable(value = "meetings", key = "'all:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<MeetingDto> getAllMeetings(Pageable pageable) {
        log.info("获取所有例会，页码: {}, 每页数量: {}", pageable.getPageNumber(), pageable.getPageSize());
        try {
            // First, let's check if we can get a count
            long totalCount = meetingRepository.count();
            log.info("数据库中会议总数: {}", totalCount);
            
            // Use the standard findAll method with better error handling
            Page<Meeting> meetings = meetingRepository.findAll(pageable);
            
            if (meetings == null) {
                log.warn("findAll returned null, creating empty page with count {}", totalCount);
                // If findAll returns null but we have data, create a page manually
                if (totalCount > 0) {
                    List<Meeting> allMeetings = meetingRepository.findAll();
                    List<Meeting> pagedMeetings = applyPagination(allMeetings, pageable);
                    return new PageImpl<>(pagedMeetings.stream().map(this::convertToDto).collect(Collectors.toList()), pageable, totalCount);
                }
                return Page.empty(pageable);
            }
            
            log.info("成功获取 {} 个会议", meetings.getTotalElements());
            return meetings.map(this::convertToDto);
            
        } catch (Exception e) {
            log.error("获取所有例会失败，尝试备用方法", e);
            // Fallback: get all meetings and apply pagination manually
            try {
                List<Meeting> allMeetings = meetingRepository.findAll();
                long totalCount = allMeetings.size();
                List<Meeting> pagedMeetings = applyPagination(allMeetings, pageable);
                List<MeetingDto> dtos = pagedMeetings.stream().map(this::convertToDto).collect(Collectors.toList());
                return new PageImpl<>(dtos, pageable, totalCount);
            } catch (Exception fallbackException) {
                log.error("备用方法也失败，返回空页面", fallbackException);
                return Page.empty(pageable);
            }
        }
    }
    
    /**
     * 手动应用分页
     */
    private List<Meeting> applyPagination(List<Meeting> meetings, Pageable pageable) {
        if (meetings == null || meetings.isEmpty()) {
            return Collections.emptyList();
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), meetings.size());
        
        if (start >= meetings.size()) {
            return Collections.emptyList();
        }
        
        return meetings.subList(start, end);
    }

    /**
     * 获取所有已完成的例会
     */
    public List<MeetingDto> getCompletedMeetings() {
        log.info("获取所有已完成的例会");
        Page<Meeting> meetings = meetingRepository.findCompletedMeetings(Pageable.unpaged());
        return meetings.getContent().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 获取即将举行的例会
     */
    public List<MeetingDto> getUpcomingMeetings() {
        log.info("获取即将举行的例会");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(7);
        List<Meeting> meetings = meetingRepository.findUpcomingMeetings(now, futureDate);
        return meetings.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 根据ID获取例会
     */
    public MeetingDto getMeetingById(Long id) {
        log.info("根据ID获取例会: {}", id);
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("例会不存在"));
        return convertToDto(meeting);
    }

    /**
     * 根据状态获取例会
     */
    public Page<MeetingDto> getMeetingsByStatus(Meeting.MeetingStatus status, Pageable pageable) {
        log.info("根据状态获取例会: {}", status);
        Page<Meeting> meetings = meetingRepository.findByStatus(status, pageable);
        return meetings.map(this::convertToDto);
    }

    /**
     * 根据类型获取例会
     */
    @Timed(value = "service.meetings.byType", description = "Time taken to get meetings by type")
//    @Cacheable(value = "meetings", key = "'type:' + #type.name()")
    public List<MeetingDto> getMeetingsByType(Meeting.MeetingType type) {
        log.info("根据类型获取例会: {}", type);
        Page<Meeting> meetings = meetingRepository.findByType(type, Pageable.unpaged());
        return meetings.getContent().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 搜索例会
     */
    @Timed(value = "service.meetings.search", description = "Time taken to search meetings")
//    @Cacheable(value = "meetings", key = "'search:' + #keyword + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<MeetingDto> searchMeetings(String keyword, Pageable pageable) {
        log.info("搜索例会，关键词: {}", keyword);
        // 查询数据库
        Page<Meeting> meetingPage = meetingRepository.searchMeetings(keyword, pageable);
        
        // 转换DTO列表
        List<MeetingDto> dtoList = meetingPage.getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 手动构造PageImpl，避免缓存Page接口
        return new PageImpl<>(dtoList, pageable, meetingPage.getTotalElements());
    }

    /**
     * 创建例会
     */
    @Timed(value = "service.meetings.create", description = "Time taken to create meeting")
//    @CacheEvict(value = "meetings", allEntries = true)
    @Transactional
    public MeetingDto createMeeting(MeetingDto meetingDto) {
        log.info("创建例会: {}", meetingDto.getTitle());
        Meeting meeting = convertToEntity(meetingDto);
        Meeting savedMeeting = meetingRepository.save(meeting);
        return convertToDto(savedMeeting);
    }

    /**
     * 更新例会
     */
    @Transactional
    public MeetingDto updateMeeting(Long id, MeetingDto meetingDto) {
        log.info("更新例会: {}", id);
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("会议不存在"));
        
        meeting.setTitle(meetingDto.getTitle());
        meeting.setDescription(meetingDto.getContent());
        meeting.setMeetingDate(meetingDto.getMeetingTime());
        meeting.setLocation(meetingDto.getLocation());
        meeting.setType(meetingDto.getMeetingType());
        meeting.setAttendees(String.join(",", meetingDto.getAttendees()));
        meeting.setAbsentees(String.join(",", meetingDto.getAbsentees()));
        meeting.setMinutes(meetingDto.getMeetingNotes());
        meeting.setConclusion(meetingDto.getConclusion());
        meeting.setActionItems(meetingDto.getActionItems());
        meeting.setTags(meetingDto.getTags());
        meeting.setDisplayOrder(meetingDto.getDisplayOrder());
        meeting.setUpdatedAt(LocalDateTime.now());
        
        Meeting updatedMeeting = meetingRepository.save(meeting);
        return convertToDto(updatedMeeting);
    }

    /**
     * 删除例会
     */
    @Timed(value = "service.meetings.delete", description = "Time taken to delete meeting")
//    @CacheEvict(value = "meetings", allEntries = true)
    @Transactional
    public void deleteMeeting(Long id) {
        log.info("删除例会: {}", id);
        if (!meetingRepository.existsById(id)) {
            throw new ResourceNotFoundException("例会不存在");
        }
        meetingRepository.deleteById(id);
    }

    /**
     * 更新例会状态
     */
    @Transactional
    public MeetingDto updateMeetingStatus(Long id, Meeting.MeetingStatus status) {
        log.info("更新例会状态: {} -> {}", id, status);
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("会议不存在"));
        
        meeting.setStatus(status);
        meeting.setUpdatedAt(LocalDateTime.now());
        
        Meeting updatedMeeting = meetingRepository.save(meeting);
        return convertToDto(updatedMeeting);
    }

    /**
     * 更新例会显示顺序
     */
    @Transactional
    public void updateDisplayOrder(Long id, Integer displayOrder) {
        log.info("更新例会显示顺序: {} -> {}", id, displayOrder);
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("会议不存在"));
        
        meeting.setDisplayOrder(displayOrder);
        meeting.setUpdatedAt(LocalDateTime.now());
        
        meetingRepository.save(meeting);
    }

    /**
     * 获取例会统计信息
     */
    @Timed(value = "service.meetings.statistics", description = "Time taken to get meeting statistics")
//    @Cacheable(value = "statistics", key = "'meetingStats'")
    public Map<String, Object> getMeetingStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总例会数
        statistics.put("totalMeetings", meetingRepository.count());
        
        // 按状态统计 - 从统计结果中提取具体数值
        List<Object[]> statusStats = meetingRepository.countMeetingsByStatus();
        for (Object[] stat : statusStats) {
            Meeting.MeetingStatus status = (Meeting.MeetingStatus) stat[0];
            Long count = (Long) stat[1];
            switch (status) {
                case SCHEDULED:
                    statistics.put("scheduledMeetings", count);
                    break;
                case COMPLETED:
                    statistics.put("completedMeetings", count);
                    break;
                case CANCELLED:
                    statistics.put("cancelledMeetings", count);
                    break;
            }
        }
        
        // 按类型统计 - 从统计结果中提取具体数值
        List<Object[]> typeStats = meetingRepository.countMeetingsByType();
        for (Object[] stat : typeStats) {
            Meeting.MeetingType type = (Meeting.MeetingType) stat[0];
            Long count = (Long) stat[1];
            switch (type) {
                case REGULAR:
                    statistics.put("typeRegular", count);
                    break;
                case EMERGENCY:
                    statistics.put("typeEmergency", count);
                    break;
                case PLANNING:
                    statistics.put("typePlanning", count);
                    break;
                case REVIEW:
                    statistics.put("typeReview", count);
                    break;
                case TRAINING:
                    statistics.put("typeTraining", count);
                    break;
            }
        }
        
        return statistics;
    }

    /**
     * 获取例会总数
     */
    public long getMeetingCount() {
        return meetingRepository.count();
    }

    /**
     * 按状态获取例会数量统计
     */
    public Map<String, Long> getMeetingCountByStatus() {
        List<Object[]> statusCounts = meetingRepository.countMeetingsByStatus();
        Map<String, Long> result = new HashMap<>();
        
        for (Object[] statusCount : statusCounts) {
            Meeting.MeetingStatus status = (Meeting.MeetingStatus) statusCount[0];
            Long count = (Long) statusCount[1];
            result.put(status.name(), count);
        }
        
        return result;
    }

    /**
     * 实体转换为DTO
     */
    private MeetingDto convertToDto(Meeting meeting) {
        if (meeting == null) {
            log.warn("Attempting to convert null meeting to DTO");
            return null;
        }
        
        try {
            MeetingDto dto = new MeetingDto();
            dto.setId(meeting.getId());
            dto.setTitle(meeting.getTitle());
            dto.setContent(meeting.getDescription());
            dto.setMeetingTime(meeting.getMeetingDate());
            dto.setLocation(meeting.getLocation());
            dto.setMeetingType(meeting.getType());
            dto.setStatus(meeting.getStatus());
            dto.setAttendees(meeting.getAttendees() != null && !meeting.getAttendees().trim().isEmpty() ? 
                List.of(meeting.getAttendees().split(",")) : List.of());
            dto.setAbsentees(meeting.getAbsentees() != null && !meeting.getAbsentees().trim().isEmpty() ? 
                List.of(meeting.getAbsentees().split(",")) : List.of());
            dto.setMeetingNotes(meeting.getMinutes());
            dto.setConclusion(meeting.getConclusion());
            dto.setActionItems(meeting.getActionItems());
            dto.setTags(meeting.getTags());
            dto.setDisplayOrder(meeting.getDisplayOrder() != null ? meeting.getDisplayOrder() : 0);
            dto.setCreatedBy(meeting.getCreatedBy() != null ? meeting.getCreatedBy().toString() : "0");
            dto.setCreatedAt(meeting.getCreatedAt());
            dto.setUpdatedAt(meeting.getUpdatedAt());
            return dto;
        } catch (Exception e) {
            log.error("转换会议实体到DTO失败，会议ID: {}", meeting.getId(), e);
            throw new RuntimeException("转换会议数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * DTO转换为实体
     */
    private Meeting convertToEntity(MeetingDto dto) {
        Meeting meeting = new Meeting();
        meeting.setTitle(dto.getTitle());
        meeting.setDescription(dto.getContent());
        meeting.setMeetingDate(dto.getMeetingTime());
        meeting.setLocation(dto.getLocation());
        meeting.setType(dto.getMeetingType());
        meeting.setAttendees(dto.getAttendees() != null ? String.join(",", dto.getAttendees()) : "");
        meeting.setAbsentees(dto.getAbsentees() != null ? String.join(",", dto.getAbsentees()) : "");
        meeting.setMinutes(dto.getMeetingNotes());
        meeting.setConclusion(dto.getConclusion());
        meeting.setActionItems(dto.getActionItems());
        meeting.setTags(dto.getTags());
        meeting.setDisplayOrder(dto.getDisplayOrder());
        meeting.setCreatedBy(Long.parseLong(dto.getCreatedBy()));
        return meeting;
    }
}