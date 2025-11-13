package com.dlut.blockchain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 访问日志实体类
 */
@Data
@Entity
@Table(name = "visit_logs")
@EqualsAndHashCode(callSuper = true)
public class VisitLog extends BaseEntity {
    
    /**
     * 用户ID（可选）
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 访问者IP地址
     */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;
    
    /**
     * 用户代理（浏览器信息）
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    /**
     * 访问的URL
     */
    @Column(name = "url", nullable = false, length = 500)
    private String url;
    
    /**
     * HTTP方法（GET、POST等）
     */
    @Column(name = "http_method", length = 10)
    private String httpMethod;
    
    /**
     * 响应状态码
     */
    @Column(name = "status_code")
    private Integer statusCode;
    
    /**
     * 请求处理时间（毫秒）
     */
    @Column(name = "response_time")
    private Long responseTime;
    
    /**
     * 来源页面URL
     */
    @Column(name = "referer", length = 500)
    private String referer;
    
    /**
     * 访问来源（直接访问、搜索引擎、社交媒体等）
     */
    @Column(name = "traffic_source", length = 50)
    private String trafficSource;
    
    /**
     * 页面停留时间（秒）
     */
    @Column(name = "page_stay_time")
    private Long pageStayTime;
    
    /**
     * 会话ID
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    /**
     * 设备类型（desktop、mobile、tablet）
     */
    @Column(name = "device_type", length = 20)
    private String deviceType;
    
    /**
     * 操作系统
     */
    @Column(name = "operating_system", length = 50)
    private String operatingSystem;
    
    /**
     * 浏览器
     */
    @Column(name = "browser", length = 50)
    private String browser;
    
    /**
     * 国家
     */
    @Column(name = "country", length = 50)
    private String country;
    
    /**
     * 城市
     */
    @Column(name = "city", length = 50)
    private String city;
    
    /**
     * 是否新访客
     */
    @Column(name = "is_new_visitor")
    private Boolean isNewVisitor;
    
    /**
     * 访问时间
     */
    @Column(name = "visit_time", nullable = false)
    private LocalDateTime visitTime;
    
    /**
     * 构造函数
     */
    public VisitLog() {
        this.visitTime = LocalDateTime.now();
        this.isNewVisitor = false;
    }
    
    /**
     * 构造函数
     */
    public VisitLog(String ipAddress, String url) {
        this();
        this.ipAddress = ipAddress;
        this.url = url;
    }
}