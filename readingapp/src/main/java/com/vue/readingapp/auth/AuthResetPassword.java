package com.vue.readingapp.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthResetPassword {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到重置密码请求 ===");
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
    public static class ResetPasswordRequest {
        private String token;
        private String email;
        private String password;
        private String password_confirmation;

        // Getters and Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getPassword_confirmation() { return password_confirmation; }
        public void setPassword_confirmation(String password_confirmation) { this.password_confirmation = password_confirmation; }

        @Override
        public String toString() {
            // 重写 toString 方法，隐藏密码信息，防止日志泄露
            return "ResetPasswordRequest{" +
                    "token='" + token + '\'' +
                    ", email='" + email + '\'' +
                    ", password='[PROTECTED]'" +
                    ", password_confirmation='[PROTECTED]'" +
                    '}';
        }
    }

    // 响应DTO
    public static class ResetPasswordResponse {
        private boolean success;
        private String message;

        public ResetPasswordResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResetPasswordResponse(false, "重置令牌不能为空")
                );
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResetPasswordResponse(false, "邮箱不能为空")
                );
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResetPasswordResponse(false, "新密码不能为空")
                );
            }

            if (request.getPassword_confirmation() == null || request.getPassword_confirmation().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResetPasswordResponse(false, "确认密码不能为空")
                );
            }

            // 2. 验证密码和确认密码是否一致
            if (!request.getPassword().equals(request.getPassword_confirmation())) {
                return ResponseEntity.badRequest().body(
                        new ResetPasswordResponse(false, "两次输入的密码不一致")
                );
            }

            // 3. 验证密码长度 (可以增加更复杂的密码策略)
            if (request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(
                        new ResetPasswordResponse(false, "密码长度不能少于6位")
                );
            }

            // 4. 检查邮箱是否存在
            String checkEmailSql = "SELECT user_id FROM users WHERE email = ?";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(checkEmailSql, request.getEmail());
            printQueryResult(users);

            if (users.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResetPasswordResponse(false, "此邮箱未注册")
                );
            }

            Map<String, Object> user = users.get(0);
            Integer userId = (Integer) user.get("user_id");

            // 5. 验证重置令牌
            String checkTokenSql = "SELECT token_id, expires_at FROM password_reset_tokens WHERE user_id = ? AND token = ?";
            List<Map<String, Object>> tokens = jdbcTemplate.queryForList(checkTokenSql, userId, request.getToken());
            printQueryResult(tokens);

            if (tokens.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ResetPasswordResponse(false, "重置令牌无效或已使用")
                );
            }

            Map<String, Object> tokenInfo = tokens.get(0);

            // 修改：使用更稳健的方式将数据库的 TIMESTAMP 转换为 LocalDateTime
            LocalDateTime expiresAt = ((Timestamp) tokenInfo.get("expires_at")).toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();

            // 6. 检查令牌是否过期
            if (now.isAfter(expiresAt)) {
                // 删除过期的令牌
                String deleteTokenSql = "DELETE FROM password_reset_tokens WHERE token_id = ?";
                jdbcTemplate.update(deleteTokenSql, tokenInfo.get("token_id"));

                return ResponseEntity.badRequest().body(
                        new ResetPasswordResponse(false, "重置令牌已过期，请重新申请")
                );
            }

            // 7. 更新用户密码 (已移除加密)
            String updatePasswordSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
            jdbcTemplate.update(updatePasswordSql, request.getPassword(), userId); // 直接使用明文密码

            // 8. 删除已使用的重置令牌
            String deleteUsedTokenSql = "DELETE FROM password_reset_tokens WHERE token_id = ?";
            jdbcTemplate.update(deleteUsedTokenSql, tokenInfo.get("token_id"));

            // 9. 删除用户的所有会话（安全考虑，强制重新登录）
            String deleteSessionsSql = "DELETE FROM user_sessions WHERE user_id = ?";
            jdbcTemplate.update(deleteSessionsSql, userId);

            // 10. 返回成功响应
            ResetPasswordResponse response = new ResetPasswordResponse(true, "密码重置成功，请使用新密码登录");
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("重置密码过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResetPasswordResponse(false, "服务器内部错误，请稍后重试")
            );
        }
    }
}