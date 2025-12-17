package com.vue.readingapp.system;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/system")
public class SystemMarkAnnouncementAsRead {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到标记公告为已读请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
        System.out.println("===========================");
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

    // 请求DTO
    public static class MarkAsReadRequest {
        private String announcementId;

        public String getAnnouncementId() { return announcementId; }
        public void setAnnouncementId(String announcementId) { this.announcementId = announcementId; }
    }

    // 响应DTO
    public static class MarkAsReadResponse {
        private boolean success;
        private String message;

        public MarkAsReadResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PutMapping("/announcements/{announcementId}/read")
    public ResponseEntity<MarkAsReadResponse> markAnnouncementAsRead(
            @PathVariable String announcementId,
            @RequestBody(required = false) MarkAsReadRequest requestBody) {

        // 处理请求数据
        String actualAnnouncementId = announcementId;
        if (requestBody != null && requestBody.getAnnouncementId() != null) {
            actualAnnouncementId = requestBody.getAnnouncementId();
        }

        // 创建请求对象
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("announcementId", actualAnnouncementId);
        requestInfo.put("pathVariable", announcementId);
        requestInfo.put("requestBody", requestBody);

        // 打印接收到的请求
        printRequest(requestInfo);

        try {
            // 1. 验证公告ID
            if (actualAnnouncementId == null || actualAnnouncementId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new MarkAsReadResponse(false, "公告ID不能为空")
                );
            }

            // 2. 从公告ID中提取通知ID
            // 假设公告ID格式为 "ann_123" 或直接是数字
            String notificationIdStr = actualAnnouncementId;
            if (actualAnnouncementId.startsWith("ann_")) {
                notificationIdStr = actualAnnouncementId.substring(4);
            }

            int notificationId;
            try {
                notificationId = Integer.parseInt(notificationIdStr);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(
                        new MarkAsReadResponse(false, "无效的公告ID格式")
                );
            }

            // 3. 检查公告是否存在
            String checkSql = "SELECT notification_id FROM notifications WHERE notification_id = ? AND type = 'announcement'";
            Integer existingId = jdbcTemplate.queryForObject(checkSql, Integer.class, notificationId);

            if (existingId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new MarkAsReadResponse(false, "未找到指定的公告")
                );
            }

            // 4. 更新公告为已读状态
            LocalDateTime now = LocalDateTime.now();
            String updateSql = "UPDATE notifications SET is_read = TRUE, updated_at = ? WHERE notification_id = ?";

            int rowsUpdated = jdbcTemplate.update(updateSql, now, notificationId);
            printQueryResult("更新了 " + rowsUpdated + " 条记录，公告ID: " + notificationId);

            if (rowsUpdated > 0) {
                MarkAsReadResponse response = new MarkAsReadResponse(true, "公告已标记为已读");
                printResponse(response);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new MarkAsReadResponse(false, "更新公告状态失败")
                );
            }

        } catch (Exception e) {
            System.err.println("标记公告为已读过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new MarkAsReadResponse(false, "标记公告为已读失败: " + e.getMessage())
            );
        }
    }
}
