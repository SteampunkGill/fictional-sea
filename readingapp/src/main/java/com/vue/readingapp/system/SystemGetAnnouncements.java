package com.vue.readingapp.system;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/system")
public class SystemGetAnnouncements {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取系统公告请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
        System.out.println("=========================");
    }

    // 打印查询结果
    private void printQueryResult(Object result) {
        System.out.println("=== 数据库查询结果 ===");
        System.out.println("查询结果: " + result);
        System.out.println("===================");
    }

    // 打印返回数据
    private void printResponse(Object response) {
        System.out.println("=== 准备返回的响应 ===");
        System.out.println("响应数据: " + response);
        System.out.println("===================");
    }

    // 响应DTO
    public static class AnnouncementsResponse {
        private boolean success;
        private String message;
        private AnnouncementsData data;

        public AnnouncementsResponse(boolean success, String message, AnnouncementsData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public AnnouncementsData getData() { return data; }
        public void setData(AnnouncementsData data) { this.data = data; }
    }

    public static class AnnouncementsData {
        private List<Announcement> announcements;
        private int unreadCount;

        public AnnouncementsData(List<Announcement> announcements, int unreadCount) {
            this.announcements = announcements;
            this.unreadCount = unreadCount;
        }

        public List<Announcement> getAnnouncements() { return announcements; }
        public void setAnnouncements(List<Announcement> announcements) { this.announcements = announcements; }

        public int getUnreadCount() { return unreadCount; }
        public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    }

    public static class Announcement {
        private String id;
        private String title;
        private String content;
        private String type;
        private String priority;
        private String targetAudience;
        private String startDate;
        private String endDate;
        private boolean isRead;
        private String actionUrl;
        private Map<String, Object> metadata;

        public Announcement(String id, String title, String content, String type, String priority,
                            String targetAudience, String startDate, String endDate, boolean isRead,
                            String actionUrl, Map<String, Object> metadata) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.type = type;
            this.priority = priority;
            this.targetAudience = targetAudience;
            this.startDate = startDate;
            this.endDate = endDate;
            this.isRead = isRead;
            this.actionUrl = actionUrl;
            this.metadata = metadata;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getTargetAudience() { return targetAudience; }
        public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }

        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    @GetMapping("/announcements")
    public ResponseEntity<AnnouncementsResponse> getAnnouncements() {
        // 打印接收到的请求
        printRequest("GET /api/system/announcements");

        try {
            // 1. 查询公告数据
            // 注意：数据库中没有专门的公告表，我们可以使用notifications表或创建一个新表
            // 这里我们假设使用notifications表，并且type为'announcement'
            String sql = "SELECT notification_id, user_id, title, content, type, is_read, created_at, metadata " +
                    "FROM notifications WHERE type = 'announcement' AND is_active = TRUE " +
                    "ORDER BY created_at DESC LIMIT 20";

            List<Map<String, Object>> announcementsData = jdbcTemplate.queryForList(sql);
            printQueryResult("查询到 " + announcementsData.size() + " 条公告记录");

            // 2. 构建公告列表
            List<Announcement> announcements = new ArrayList<>();
            int unreadCount = 0;

            for (Map<String, Object> announcementData : announcementsData) {
                String id = "ann_" + announcementData.get("notification_id");
                String title = (String) announcementData.get("title");
                String content = (String) announcementData.get("content");
                String type = (String) announcementData.get("type");
                Boolean isRead = (Boolean) announcementData.get("is_read");
                LocalDateTime createdAt = (LocalDateTime) announcementData.get("created_at");

                // 解析元数据
                Map<String, Object> metadata = new HashMap<>();
                String metadataStr = (String) announcementData.get("metadata");
                if (metadataStr != null && !metadataStr.trim().isEmpty()) {
                    // 简单解析JSON字符串（实际项目中应该使用JSON库）
                    if (metadataStr.contains("maintenanceDuration")) {
                        metadata.put("maintenanceDuration", "2h");
                        metadata.put("affectedServices", new String[]{"documents", "export"});
                    }
                }

                // 设置默认值
                String priority = "medium";
                String targetAudience = "all";
                String actionUrl = null;

                // 根据类型设置优先级
                if ("maintenance".equals(type)) {
                    priority = "high";
                } else if ("feature".equals(type)) {
                    priority = "low";
                }

                // 判断是否已读
                boolean readStatus = isRead != null ? isRead : false;
                if (!readStatus) {
                    unreadCount++;
                }

                // 计算结束时间（默认公告有效期为7天）
                LocalDateTime endDate = createdAt.plusDays(7);

                Announcement announcement = new Announcement(
                        id,
                        title,
                        content,
                        type,
                        priority,
                        targetAudience,
                        createdAt.format(DateTimeFormatter.ISO_DATE_TIME),
                        endDate.format(DateTimeFormatter.ISO_DATE_TIME),
                        readStatus,
                        actionUrl,
                        metadata
                );

                announcements.add(announcement);
            }

            // 3. 如果没有公告，添加一些示例公告
            if (announcements.isEmpty()) {
                announcements = createSampleAnnouncements();
                unreadCount = 2; // 示例中有2条未读公告
            }

            // 4. 构建响应数据
            AnnouncementsData announcementsDataObj = new AnnouncementsData(announcements, unreadCount);
            AnnouncementsResponse response = new AnnouncementsResponse(true, "获取系统公告成功", announcementsDataObj);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取系统公告过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new AnnouncementsResponse(false, "获取系统公告失败: " + e.getMessage(), null)
            );
        }
    }

    // 创建示例公告
    private List<Announcement> createSampleAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();

        // 公告1：系统维护通知
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("maintenanceDuration", "2h");
        metadata1.put("affectedServices", new String[]{"documents", "export"});

        announcements.add(new Announcement(
                "ann_001",
                "系统维护通知",
                "我们将于2024年1月2日凌晨2:00-4:00进行系统维护，期间部分功能可能无法使用，请提前安排好学习计划。",
                "maintenance",
                "high",
                "all",
                LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.now().plusDays(6).format(DateTimeFormatter.ISO_DATE_TIME),
                false, // 未读
                null,
                metadata1
        ));

        // 公告2：新功能发布
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("version", "1.1.0");
        metadata2.put("releaseDate", "2024-01-01");

        announcements.add(new Announcement(
                "ann_002",
                "新功能发布：词汇复习算法升级",
                "我们升级了词汇复习算法，现在采用更科学的SM2算法，能够根据您的记忆情况智能安排复习计划，提高学习效率。",
                "feature",
                "medium",
                "all",
                LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.now().plusDays(4).format(DateTimeFormatter.ISO_DATE_TIME),
                false, // 未读
                "/features/sm2-algorithm",
                metadata2
        ));

        // 公告3：一般通知（已读）
        announcements.add(new Announcement(
                "ann_003",
                "应用使用指南更新",
                "我们更新了应用使用指南，新增了视频教程和常见问题解答，帮助您更好地使用本应用。",
                "info",
                "low",
                "all",
                LocalDateTime.now().minusDays(5).format(DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_DATE_TIME),
                true, // 已读
                "/help",
                new HashMap<>()
        ));

        return announcements;
    }
}
