package com.vue.readingapp.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthUpdatePassword {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到更新密码请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("====================");
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
    public static class UpdatePasswordRequest {
        private String current_password;
        private String new_password;
        private String new_password_confirmation;

        public String getCurrent_password() { return current_password; }
        public void setCurrent_password(String current_password) { this.current_password = current_password; }

        public String getNew_password() { return new_password; }
        public void setNew_password(String new_password) { this.new_password = new_password; }

        public String getNew_password_confirmation() { return new_password_confirmation; }
        public void setNew_password_confirmation(String new_password_confirmation) { this.new_password_confirmation = new_password_confirmation; }
    }

    // 响应DTO
    public static class UpdatePasswordResponse {
        private boolean success;
        private String message;

        public UpdatePasswordResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PutMapping("/password")
    public ResponseEntity<UpdatePasswordResponse> updatePassword(@RequestBody UpdatePasswordRequest request,
                                                                 HttpServletRequest httpRequest) {
        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getCurrent_password() == null || request.getCurrent_password().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new UpdatePasswordResponse(false, "当前密码不能为空")
                );
            }

            if (request.getNew_password() == null || request.getNew_password().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new UpdatePasswordResponse(false, "新密码不能为空")
                );
            }

            if (request.getNew_password_confirmation() == null || request.getNew_password_confirmation().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new UpdatePasswordResponse(false, "确认密码不能为空")
                );
            }

            // 2. 验证新密码和确认密码是否一致
            if (!request.getNew_password().equals(request.getNew_password_confirmation())) {
                return ResponseEntity.badRequest().body(
                        new UpdatePasswordResponse(false, "两次输入的新密码不一致")
                );
            }

            // 3. 验证新密码长度
            if (request.getNew_password().length() < 6) {
                return ResponseEntity.badRequest().body(
                        new UpdatePasswordResponse(false, "新密码长度不能少于6位")
                );
            }

            // 4. 验证新密码不能与当前密码相同
            if (request.getCurrent_password().equals(request.getNew_password())) {
                return ResponseEntity.badRequest().body(
                        new UpdatePasswordResponse(false, "新密码不能与当前密码相同")
                );
            }

            // 5. 从请求头获取token并验证用户
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new UpdatePasswordResponse(false, "未提供有效的认证令牌")
                );
            }

            String token = authHeader.substring(7);

            // 6. 查找对应的会话和用户
            String findUserSql = "SELECT s.user_id, u.password_hash FROM user_sessions s " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "WHERE s.access_token = ? AND s.expires_at > ?";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(findUserSql, token, LocalDateTime.now());
            printQueryResult(users);

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new UpdatePasswordResponse(false, "令牌无效或已过期")
                );
            }

            Map<String, Object> user = users.get(0);
            Integer userId = (Integer) user.get("user_id");
            String currentPasswordHash = (String) user.get("password_hash");

            // 7. 验证当前密码是否正确
            if (!currentPasswordHash.equals(request.getCurrent_password())) {
                return ResponseEntity.badRequest().body(
                        new UpdatePasswordResponse(false, "当前密码错误")
                );
            }

            // 8. 更新密码
            String updatePasswordSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
            jdbcTemplate.update(updatePasswordSql, request.getNew_password(), userId);

            // 9. 删除用户的所有会话（安全考虑，强制重新登录）
            String deleteSessionsSql = "DELETE FROM user_sessions WHERE user_id = ?";
            jdbcTemplate.update(deleteSessionsSql, userId);

            System.out.println("用户ID " + userId + " 的密码已更新，所有会话已清除");

            // 10. 返回成功响应
            UpdatePasswordResponse response = new UpdatePasswordResponse(true, "密码更新成功，请重新登录");
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("更新密码过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new UpdatePasswordResponse(false, "服务器内部错误: " + e.getMessage())
            );
        }
    }
}