package com.dlut.blockchain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 例会实体类
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "meetings", indexes = {
    @Index(name = "idx_title", columnList = "title"),
    @Index(name = "idx_meeting_date", columnList = "meeting_date"),
    @Index(name = "idx_status", columnList = "status")
})
public class Meeting extends BaseEntity {

    @NotBlank(message = "会议标题不能为空")
    @Size(max = 200, message = "会议标题长度不能超过200位")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 2000, message = "会议描述长度不能超过2000位")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "meeting_date", nullable = false)
    private LocalDateTime meetingDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Size(max = 200, message = "会议地点长度不能超过200位")
    @Column(name = "location", length = 200)
    private String location;

    @Size(max = 50, message = "主持人长度不能超过50位")
    @Column(name = "host", length = 50)
    private String host;

    @Size(max = 500, message = "参与人员长度不能超过500位")
    @Column(name = "participants", length = 500)
    private String participants;

    @Size(max = 500, message = "参会者长度不能超过500位")
    @Column(name = "attendees", length = 500)
    private String attendees;

    @Size(max = 500, message = "缺席者长度不能超过500位")
    @Column(name = "absentees", length = 500)
    private String absentees;

    @Size(max = 2000, message = "会议议程长度不能超过2000位")
    @Column(name = "agenda", columnDefinition = "TEXT")
    private String agenda;

    @Size(max = 4000, message = "会议纪要长度不能超过4000位")
    @Column(name = "minutes", columnDefinition = "TEXT")
    private String minutes;

    @Size(max = 2000, message = "结论长度不能超过2000位")
    @Column(name = "conclusion", columnDefinition = "TEXT")
    private String conclusion;

    @Size(max = 2000, message = "行动项长度不能超过2000位")
    @Column(name = "action_items", columnDefinition = "TEXT")
    private String actionItems;

    @Size(max = 500, message = "标签长度不能超过500位")
    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "attachment_name", length = 200)
    private String attachmentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private MeetingType type = MeetingType.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @Column(name = "attendee_count")
    private Integer attendeeCount = 0;

    /**
     * 会议类型枚举
     */
    public enum MeetingType {
        REGULAR, EMERGENCY, PLANNING, REVIEW, TRAINING
    }

    /**
     * 会议状态枚举
     */
    public enum MeetingStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED
    }
}