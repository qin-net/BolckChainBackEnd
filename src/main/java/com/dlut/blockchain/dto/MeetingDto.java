package com.dlut.blockchain.dto;

import com.dlut.blockchain.entity.Meeting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 例会DTO
 */
@Data
public class MeetingDto {
    
    private Long id;
    
    @NotBlank(message = "例会标题不能为空")
    @Size(max = 200, message = "例会标题不能超过200个字符")
    private String title;
    
    @Size(max = 5000, message = "例会内容不能超过5000个字符")
    private String content;
    
    private LocalDateTime meetingTime;
    
    @Size(max = 200, message = "例会地点不能超过200个字符")
    private String location;
    
    private Meeting.MeetingType meetingType;
    
    private Meeting.MeetingStatus status;
    
    private List<String> attendees;
    
    private List<String> absentees;
    
    private String meetingNotes;
    
    private String conclusion;
    
    private String actionItems;
    
    private String tags;
    
    private Integer displayOrder;
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}