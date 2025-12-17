package com.vue.readingapp.user;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserDeleteAccount {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到删除账户请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("=================");
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
    public static class DeleteAccountRequest {
        private String password;

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // 响应DTO
    public static class DeleteAccountResponse {
        private boolean success;
        private String message;

        public DeleteAccountResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @DeleteMapping("/account")
    public ResponseEntity<DeleteAccountResponse> deleteAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody DeleteAccountRequest request) {

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new DeleteAccountResponse(false, "请先登录")
                );
            }

            String accessToken = authHeader.substring(7);

            // 2. 验证token有效性
            String tokenSql = "SELECT user_id, expires_at FROM user_sessions WHERE access_token = ? AND expires_at > NOW()";
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(tokenSql, accessToken);

            if (sessions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new DeleteAccountResponse(false, "登录已过期，请重新登录")
                );
            }

            Map<String, Object> session = sessions.get(0);
            int userId = (int) session.get("user_id");

            // 3. 验证密码
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new DeleteAccountResponse(false, "密码不能为空")
                );
            }

            // 查询用户密码
            String passwordSql = "SELECT password_hash FROM users WHERE user_id = ?";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(passwordSql, userId);

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new DeleteAccountResponse(false, "用户不存在")
                );
            }

            Map<String, Object> user = users.get(0);
            String passwordHash = (String) user.get("password_hash");

            // 验证密码（课设简单处理，实际应该使用加密验证）
            if (!passwordHash.equals(request.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new DeleteAccountResponse(false, "密码错误")
                );
            }

            // 4. 开始删除用户数据（注意外键约束，需要按顺序删除）

            // 4.1 删除用户会话
            String deleteSessionsSql = "DELETE FROM user_sessions WHERE user_id = ?";
            jdbcTemplate.update(deleteSessionsSql, userId);

            // 4.2 删除第三方登录
            String deleteThirdPartySql = "DELETE FROM third_party_logins WHERE user_id = ?";
            jdbcTemplate.update(deleteThirdPartySql, userId);

            // 4.3 删除密码重置令牌
            String deleteResetTokensSql = "DELETE FROM password_reset_tokens WHERE user_id = ?";
            jdbcTemplate.update(deleteResetTokensSql, userId);

            // 4.4 删除用户成就
            String deleteUserAchievementsSql = "DELETE FROM user_achievements WHERE user_id = ?";
            jdbcTemplate.update(deleteUserAchievementsSql, userId);

            // 4.5 删除每日学习统计
            String deleteDailyStatsSql = "DELETE FROM daily_learning_stats WHERE user_id = ?";
            jdbcTemplate.update(deleteDailyStatsSql, userId);

            // 4.6 删除词汇掌握统计
            String deleteVocabStatsSql = "DELETE FROM vocabulary_mastery_stats WHERE user_id = ?";
            jdbcTemplate.update(deleteVocabStatsSql, userId);

            // 4.7 删除用户生词本
            String deleteUserVocabularySql = "DELETE FROM user_vocabulary WHERE user_id = ?";
            jdbcTemplate.update(deleteUserVocabularySql, userId);

            // 4.8 删除用户生词标签关系
            String deleteUserVocabTagsSql = "DELETE FROM user_vocabulary_tags WHERE user_id = ?";
            jdbcTemplate.update(deleteUserVocabTagsSql, userId);

            // 4.9 删除复习会话
            String deleteReviewSessionsSql = "DELETE FROM review_sessions WHERE user_id = ?";
            jdbcTemplate.update(deleteReviewSessionsSql, userId);

            // 4.10 删除复习项目
            String deleteReviewItemsSql = "DELETE FROM review_items WHERE user_id = ?";
            jdbcTemplate.update(deleteReviewItemsSql, userId);

            // 4.11 删除间隔重复计划
            String deleteSpacedRepetitionSql = "DELETE FROM spaced_repetition_schedule WHERE user_id = ?";
            jdbcTemplate.update(deleteSpacedRepetitionSql, userId);

            // 4.12 删除测验问题
            String deleteQuizQuestionsSql = "DELETE FROM quiz_questions WHERE user_id = ?";
            jdbcTemplate.update(deleteQuizQuestionsSql, userId);

            // 4.13 删除文档高亮
            String deleteHighlightsSql = "DELETE FROM document_highlights WHERE user_id = ?";
            jdbcTemplate.update(deleteHighlightsSql, userId);

            // 4.14 删除文档笔记
            String deleteNotesSql = "DELETE FROM document_notes WHERE user_id = ?";
            jdbcTemplate.update(deleteNotesSql, userId);

            // 4.15 删除笔记附件
            String deleteNoteAttachmentsSql = "DELETE FROM note_attachments WHERE user_id = ?";
            jdbcTemplate.update(deleteNoteAttachmentsSql, userId);

            // 4.16 删除阅读历史
            String deleteReadingHistorySql = "DELETE FROM reading_history WHERE user_id = ?";
            jdbcTemplate.update(deleteReadingHistorySql, userId);

            // 4.17 删除单词查询历史
            String deleteWordLookupHistorySql = "DELETE FROM word_lookup_history WHERE user_id = ?";
            jdbcTemplate.update(deleteWordLookupHistorySql, userId);

            // 4.18 删除搜索历史
            String deleteSearchHistorySql = "DELETE FROM search_history WHERE user_id = ?";
            jdbcTemplate.update(deleteSearchHistorySql, userId);

            // 4.19 删除通知
            String deleteNotificationsSql = "DELETE FROM notifications WHERE user_id = ?";
            jdbcTemplate.update(deleteNotificationsSql, userId);

            // 4.20 删除反馈消息
            String deleteFeedbackMessagesSql = "DELETE FROM feedback_messages WHERE user_id = ?";
            jdbcTemplate.update(deleteFeedbackMessagesSql, userId);

            // 4.21 删除离线文档
            String deleteOfflineDocumentsSql = "DELETE FROM offline_documents WHERE user_id = ?";
            jdbcTemplate.update(deleteOfflineDocumentsSql, userId);

            // 4.22 删除同步日志
            String deleteSyncLogsSql = "DELETE FROM sync_logs WHERE user_id = ?";
            jdbcTemplate.update(deleteSyncLogsSql, userId);

            // 4.23 删除用户设置
            // 注意：根据数据库设计，可能有多个设置表
            String deleteUserSettingsSql = "DELETE FROM user_settings WHERE user_id = ?";
            jdbcTemplate.update(deleteUserSettingsSql, userId);

            String deleteReadingSettingsSql = "DELETE FROM reading_settings WHERE user_id = ?";
            jdbcTemplate.update(deleteReadingSettingsSql, userId);

            String deleteReviewSettingsSql = "DELETE FROM review_settings WHERE user_id = ?";
            jdbcTemplate.update(deleteReviewSettingsSql, userId);

            String deleteNotificationSettingsSql = "DELETE FROM notification_settings WHERE user_id = ?";
            jdbcTemplate.update(deleteNotificationSettingsSql, userId);

            // 4.24 删除文档标签关系
            String deleteDocTagRelationsSql = "DELETE FROM document_tag_relations WHERE user_id = ?";
            jdbcTemplate.update(deleteDocTagRelationsSql, userId);

            // 4.25 删除用户文档（注意：文档表可能有外键约束，需要先删除相关记录）
            // 先删除文档分页
            String deleteDocPagesSql = "DELETE FROM document_pages WHERE document_id IN (SELECT document_id FROM documents WHERE user_id = ?)";
            jdbcTemplate.update(deleteDocPagesSql, userId);

            // 删除文档处理队列
            String deleteProcessingQueueSql = "DELETE FROM document_processing_queue WHERE document_id IN (SELECT document_id FROM documents WHERE user_id = ?)";
            jdbcTemplate.update(deleteProcessingQueueSql, userId);

            // 删除文档分类关系
            String deleteDocCategoriesSql = "DELETE FROM document_categories WHERE document_id IN (SELECT document_id FROM documents WHERE user_id = ?)";
            jdbcTemplate.update(deleteDocCategoriesSql, userId);

            // 最后删除文档
            String deleteDocumentsSql = "DELETE FROM documents WHERE user_id = ?";
            jdbcTemplate.update(deleteDocumentsSql, userId);

            // 4.26 最后删除用户
            String deleteUserSql = "DELETE FROM users WHERE user_id = ?";
            int rowsDeleted = jdbcTemplate.update(deleteUserSql, userId);

            printQueryResult("删除用户行数: " + rowsDeleted);

            if (rowsDeleted == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new DeleteAccountResponse(false, "用户不存在")
                );
            }

            // 5. 准备响应数据
            DeleteAccountResponse response = new DeleteAccountResponse(true, "账户删除成功");

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("删除账户过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new DeleteAccountResponse(false, "服务器内部错误: " + e.getMessage())
            );
        }
    }
}
